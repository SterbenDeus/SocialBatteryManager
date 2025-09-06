package com.example.socialbatterymanager.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BackupMetadataDao {
    @Insert
    suspend fun insertBackupMetadata(metadata: BackupMetadataEntity)
    
    @Update
    suspend fun updateBackupMetadata(metadata: BackupMetadataEntity)
    
    @Query("SELECT * FROM backup_metadata ORDER BY timestamp DESC")
    fun getAllBackupMetadata(): Flow<List<BackupMetadataEntity>>
    
    @Query("SELECT * FROM backup_metadata WHERE id = :id")
    suspend fun getBackupMetadataById(id: String): BackupMetadataEntity?
    
    @Query("SELECT * FROM backup_metadata ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestBackupMetadata(): BackupMetadataEntity?
    
    @Query("DELETE FROM backup_metadata WHERE id = :id")
    suspend fun deleteBackupMetadata(id: String)
}
