package com.example.socialbatterymanager.model

data class Activity(
    val id: Int = 0,
    val name: String,
    val type: String,
    val energyImpact: Int, // positive for energy gain, negative for energy loss
    val people: String,
    val mood: String,
    val notes: String,
    val date: Long = System.currentTimeMillis()
)