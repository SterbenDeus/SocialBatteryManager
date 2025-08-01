package com.example.socialbatterymanager.features.people.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.features.people.data.PeopleRepository
import com.example.socialbatterymanager.features.people.data.PersonWithStats
import com.example.socialbatterymanager.features.people.data.WeeklyStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SortOption {
    NAME, ENERGY_DRAIN, INTERACTIONS
}

class PeopleViewModel(private val peopleRepository: PeopleRepository) : ViewModel() {
    
    private val _people = MutableStateFlow<List<PersonWithStats>>(emptyList())
    val people: StateFlow<List<PersonWithStats>> = _people.asStateFlow()
    
    private val _weeklyStats = MutableStateFlow<WeeklyStats?>(null)
    val weeklyStats: StateFlow<WeeklyStats?> = _weeklyStats.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _sortOption = MutableStateFlow(SortOption.NAME)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()
    
    init {
        loadPeople()
        loadWeeklyStats()
    }
    
    private fun loadPeople() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Combine people with sort option to automatically update when sort changes
                combine(
                    peopleRepository.getAllPeople(),
                    _sortOption
                ) { peopleList, sort ->
                    // Enrich people with stats
                    val peopleWithStats = peopleList.map { person ->
                        PersonWithStats(
                            person = person,
                            totalInteractions = peopleRepository.getTotalInteractions(person.name),
                            totalEnergyUsed = peopleRepository.getTotalEnergyUsed(person.name),
                            interactionsThisWeek = peopleRepository.getInteractionsThisWeek(person.name)
                        )
                    }
                    
                    // Sort based on current sort option
                    when (sort) {
                        SortOption.NAME -> peopleWithStats.sortedBy { it.person.name }
                        SortOption.ENERGY_DRAIN -> peopleWithStats.sortedByDescending { it.totalEnergyUsed }
                        SortOption.INTERACTIONS -> peopleWithStats.sortedByDescending { it.totalInteractions }
                    }
                }.collect { sortedPeopleWithStats ->
                    _people.value = sortedPeopleWithStats
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadWeeklyStats() {
        viewModelScope.launch {
            try {
                val stats = peopleRepository.getWeeklyStats()
                _weeklyStats.value = stats
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun setSortOption(option: SortOption) {
        _sortOption.value = option
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
                peopleRepository.searchPeople(query).collect { results ->
                    val peopleWithStats = results.map { person ->
                        PersonWithStats(
                            person = person,
                            totalInteractions = peopleRepository.getTotalInteractions(person.name),
                            totalEnergyUsed = peopleRepository.getTotalEnergyUsed(person.name),
                            interactionsThisWeek = peopleRepository.getInteractionsThisWeek(person.name)
                        )
                    }
                    _people.value = peopleWithStats
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }

    fun refreshData() {
        loadPeople()
        loadWeeklyStats()
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