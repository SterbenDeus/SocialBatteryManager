package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.AuditLogDao
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class AuditRepository(
    private val auditLogDao: AuditLogDao,
    private val gson: Gson = Gson()
) {
    suspend fun logAuditEntry(
        entityType: String,
        entityId: String,
        action: String,
        oldValues: Any? = null,
        newValues: Any? = null,
        userId: String? = null
    ) {
        val auditLog = AuditLogEntity(
            entityType = entityType,
            entityId = entityId,
            action = action,
            oldValues = oldValues?.let { gson.toJson(it) },
            newValues = newValues?.let { gson.toJson(it) },
            timestamp = System.currentTimeMillis(),
            userId = userId
        )
        auditLogDao.insertAuditLog(auditLog)
    }

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditLogDao.getAllAuditLogs()

    fun getAuditLogsForEntity(entityType: String, entityId: String): Flow<List<AuditLogEntity>> =
        auditLogDao.getAuditLogsForEntity(entityType, entityId)

    suspend fun cleanupOldAuditLogs(retentionDays: Int) {
        val cutoff = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L)
        auditLogDao.deleteAuditLogsOlderThan(cutoff)
    }

    suspend fun insertAuditLogRaw(auditLog: AuditLogEntity) {
        auditLogDao.insertAuditLog(auditLog)
    }
}

