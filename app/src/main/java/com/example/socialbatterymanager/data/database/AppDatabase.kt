package com.example.socialbatterymanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import com.example.socialbatterymanager.model.EnergyLog
import com.example.socialbatterymanager.data.EnergyLogDao

@Database(
    entities = [
        com.example.socialbatterymanager.data.model.ActivityEntity::class,
        com.example.socialbatterymanager.data.model.AuditLogEntity::class,
        com.example.socialbatterymanager.data.model.BackupMetadataEntity::class,
        com.example.socialbatterymanager.model.EnergyLog::class
    ],
    version = 4, // Bump version to include EnergyLog
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun backupMetadataDao(): BackupMetadataDao
    abstract fun energyLogDao(): EnergyLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "social_battery_db"

        fun getDatabase(context: Context, passphrase: String? = null): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context, passphrase)
                INSTANCE = instance
                instance
            }
        }
        
        private fun buildDatabase(context: Context, passphrase: String?): AppDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            // Add encryption if passphrase is provided
            if (passphrase != null) {
                val factory = SupportFactory(SQLiteDatabase.getBytes(passphrase.toCharArray()))
                builder.openHelperFactory(factory)
            }
            // Remove migration to force recreation
            return builder.build()
        }
        
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
