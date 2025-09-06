package com.example.socialbatterymanager.features.people.data

import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.Person
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeopleRepository @Inject constructor(private val database: AppDatabase) {
    
    fun getAllPeople(): Flow<List<Person>> {
        return database.personDao().getAllPeople()
    }

    fun getAllPeopleSortedByName(): Flow<List<Person>> {
        return database.personDao().getAllPeopleSortedByName()
    }

    fun getAllPeopleSortedByEnergyLevel(): Flow<List<Person>> {
        return database.personDao().getAllPeopleSortedByEnergyLevel()
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

    suspend fun getTotalPeopleCount(): Int {
        return database.personDao().getTotalPeopleCount()
    }

    suspend fun getInteractionsThisWeek(personName: String): Int {
        val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        return database.personDao().getInteractionsThisWeek(personName, weekStart)
    }

    suspend fun getTotalInteractions(personName: String): Int {
        return database.personDao().getTotalInteractions(personName)
    }

    suspend fun getTotalEnergyUsed(personName: String): Int {
        return database.personDao().getTotalEnergyUsed(personName)
    }

    suspend fun getWeeklyStats(): WeeklyStats {
        val totalPeople = getTotalPeopleCount()
        val weekStart = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        
        // Get all activities from this week
        val thisWeekInteractions = database.activityDao().getActivitiesCountFromDate(weekStart)
        
        // Calculate average energy drain (simplified calculation)
        val avgDrain = if (totalPeople > 0) {
            val totalEnergyUsed = database.activityDao().getTotalEnergyUsedFromDate(weekStart)
            val hours = totalEnergyUsed / 60.0 // Convert minutes to hours (rough estimation)
            hours / totalPeople
        } else 0.0

        return WeeklyStats(
            totalPeople = totalPeople,
            avgDrain = avgDrain,
            thisWeekInteractions = thisWeekInteractions
        )
    }
}

data class WeeklyStats(
    val totalPeople: Int,
    val avgDrain: Double, // in hours
    val thisWeekInteractions: Int
)

data class PersonWithStats(
    val person: Person,
    val totalInteractions: Int,
    val totalEnergyUsed: Int,
    val interactionsThisWeek: Int
)
