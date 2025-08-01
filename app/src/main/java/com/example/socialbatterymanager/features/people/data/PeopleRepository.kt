package com.example.socialbatterymanager.features.people.data

import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.Person
import kotlinx.coroutines.flow.Flow

class PeopleRepository(private val database: AppDatabase) {
    
    fun getAllPeople(): Flow<List<Person>> {
        return database.personDao().getAllPeople()
    }
    
    suspend fun insertPerson(person: Person) {
        database.personDao().insertPerson(person)
    }
    
    suspend fun updatePerson(person: Person) {
        database.personDao().updatePerson(person)
    }
    
    suspend fun deletePerson(person: Person) {
        database.personDao().deletePerson(person)
    }
    
    suspend fun getPersonById(id: Int): Person? {
        return database.personDao().getPersonById(id)
    }
    
    fun searchPeople(query: String): Flow<List<Person>> {
        return database.personDao().searchPeople(query)
    }
}