package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.AppDatabase
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuditRepository(
        database: AppDatabase,
        gson: Gson
    ): AuditRepository = AuditRepository(database.auditLogDao(), gson)

    @Provides
    @Singleton
    fun provideActivityRepository(
        database: AppDatabase,
        auditRepository: AuditRepository
    ): ActivityRepository = ActivityRepository(database.activityDao(), auditRepository)

    @Provides
    @Singleton
    fun provideBackupRepository(
        database: AppDatabase,
        gson: Gson
    ): BackupRepository = BackupRepository(
        database.activityDao(),
        database.auditLogDao(),
        database.backupMetadataDao(),
        gson
    )
}
