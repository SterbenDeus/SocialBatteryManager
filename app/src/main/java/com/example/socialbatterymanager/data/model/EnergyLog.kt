package com.example.socialbatterymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "energy_logs")
data class EnergyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val energyLevel: Int,
    val timestamp: Long,
    val changeAmount: Int = 0, // positive for gain, negative for loss
    val reason: String? = null // optional reason for the change
)