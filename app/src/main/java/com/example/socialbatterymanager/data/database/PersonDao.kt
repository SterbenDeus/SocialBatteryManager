package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.socialbatterymanager.data.model.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {
    @Insert
    suspend fun insertPerson(person: Person)

    @Update
    suspend fun updatePerson(person: Person)

    @Delete
    suspend fun deletePerson(person: Person)

    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeople(): Flow<List<Person>>

    @Query("SELECT * FROM people ORDER BY name ASC")
    fun getAllPeopleSortedByName(): Flow<List<Person>>

    @Query("SELECT * FROM people ORDER BY socialEnergyLevel DESC")
    fun getAllPeopleSortedByEnergyLevel(): Flow<List<Person>>

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Int): Person?

    @Query("SELECT * FROM people WHERE name LIKE '%' || :name || '%'")
    fun searchPeople(name: String): Flow<List<Person>>

    @Query("SELECT COUNT(*) FROM people")
    suspend fun getTotalPeopleCount(): Int

    @Query("""
        SELECT COUNT(*) FROM activities 
        WHERE people LIKE '%' || :personName || '%' 
        AND date >= :weekStart
    """)
    suspend fun getInteractionsThisWeek(personName: String, weekStart: Long): Int

    @Query("""
        SELECT COUNT(*) FROM activities 
        WHERE people LIKE '%' || :personName || '%'
    """)
    suspend fun getTotalInteractions(personName: String): Int

    @Query("""
        SELECT COALESCE(SUM(ABS(energy)), 0) FROM activities 
        WHERE people LIKE '%' || :personName || '%'
    """)
    suspend fun getTotalEnergyUsed(personName: String): Int
}