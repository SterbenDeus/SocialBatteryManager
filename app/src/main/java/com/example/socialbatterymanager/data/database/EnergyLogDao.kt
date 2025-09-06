package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.socialbatterymanager.data.model.EnergyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface EnergyLogDao {
    @Insert
    suspend fun insertEnergyLog(energyLog: EnergyLog)

    @Query("SELECT * FROM energy_logs ORDER BY timestamp DESC")
    fun getAllEnergyLogs(): Flow<List<EnergyLog>>

    @Query("SELECT * FROM energy_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestEnergyLog(): EnergyLog?

    @Query("SELECT * FROM energy_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    fun getEnergyLogsAfter(startTime: Long): Flow<List<EnergyLog>>

    @Query("SELECT * FROM energy_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getEnergyLogsByDateRangeSync(startTime: Long, endTime: Long): List<EnergyLog>
}
