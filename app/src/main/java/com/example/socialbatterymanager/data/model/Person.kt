package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val avatarPath: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)