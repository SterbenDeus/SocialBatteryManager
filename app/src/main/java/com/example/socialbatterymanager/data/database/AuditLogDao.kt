package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.socialbatterymanager.data.model.AuditLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertAuditLog(auditLog: AuditLogEntity)
    
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_logs WHERE entityType = :entityType AND entityId = :entityId ORDER BY timestamp DESC")
    fun getAuditLogsForEntity(entityType: String, entityId: String): Flow<List<AuditLogEntity>>
    
    @Query("SELECT * FROM audit_logs WHERE timestamp > :since ORDER BY timestamp DESC")
    fun getAuditLogsSince(since: Long): Flow<List<AuditLogEntity>>
    
    @Query("DELETE FROM audit_logs WHERE timestamp < :cutoff")
    suspend fun deleteAuditLogsOlderThan(cutoff: Long)
    
    @Query("SELECT COUNT(*) FROM audit_logs")
    suspend fun getAuditLogCount(): Int
}