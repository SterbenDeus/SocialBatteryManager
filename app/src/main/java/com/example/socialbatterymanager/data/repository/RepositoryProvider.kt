package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.database.AppDatabase

class RepositoryProvider private constructor(context: Context, passphrase: String? = null) {
    private val database: AppDatabase = AppDatabase.getDatabase(context, passphrase)

    val auditRepository: AuditRepository by lazy {
        AuditRepository(database.auditLogDao())
    }

    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityDao(), auditRepository)
    }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(database.activityDao(), database.auditLogDao(), database.backupMetadataDao())
    }

    companion object {
        @Volatile
        private var INSTANCE: RepositoryProvider? = null

        fun getInstance(context: Context, passphrase: String? = null): RepositoryProvider {
            return INSTANCE ?: synchronized(this) {
                RepositoryProvider(context, passphrase).also { INSTANCE = it }
            }
        }

        fun clearInstance() {
            INSTANCE = null
        }
    }
}

