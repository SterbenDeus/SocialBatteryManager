package com.example.socialbatterymanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.socialbatterymanager.model.Person
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

    @Query("SELECT * FROM people WHERE id = :id")
    suspend fun getPersonById(id: Int): Person?

    @Query("SELECT * FROM people WHERE name LIKE '%' || :name || '%'")
    fun searchPeople(name: String): Flow<List<Person>>
}