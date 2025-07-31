package com.example.socialbatterymanager.features.people.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.features.people.data.PeopleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PeopleViewModel(private val peopleRepository: PeopleRepository) : ViewModel() {
    
    private val _people = MutableStateFlow<List<Person>>(emptyList())
    val people: StateFlow<List<Person>> = _people.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPeople()
    }
    
    private fun loadPeople() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                peopleRepository.getAllPeople().collect { peopleList ->
                    _people.value = peopleList
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addPerson(person: Person) {
        viewModelScope.launch {
            try {
                peopleRepository.insertPerson(person)
                // The Flow will automatically update the UI
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun updatePerson(person: Person) {
        viewModelScope.launch {
            try {
                peopleRepository.updatePerson(person)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun deletePerson(person: Person) {
        viewModelScope.launch {
            try {
                peopleRepository.deletePerson(person)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun searchPeople(query: String) {
        viewModelScope.launch {
            try {
                val results = peopleRepository.searchPeople(query)
                _people.value = results
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

class PeopleViewModelFactory(private val peopleRepository: PeopleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PeopleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PeopleViewModel(peopleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}