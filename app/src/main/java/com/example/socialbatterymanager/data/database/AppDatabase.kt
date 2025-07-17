package com.example.socialbatterymanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.example.socialbatterymanager.data.model.BackupMetadataEntity
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Database(
    entities = [
        ActivityEntity::class, 
        AuditLogEntity::class, 
        BackupMetadataEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun backupMetadataDao(): BackupMetadataDao

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
            
            return builder
                .addMigrations(MIGRATION_1_2)
                .build()
        }
        
        fun clearInstance() {
            INSTANCE = null
        }
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to activities table
                database.execSQL("ALTER TABLE activities ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                
                // Create audit_logs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS audit_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entityType TEXT NOT NULL,
                        entityId TEXT NOT NULL,
                        action TEXT NOT NULL,
                        oldValues TEXT,
                        newValues TEXT,
                        timestamp INTEGER NOT NULL,
                        userId TEXT
                    )
                """.trimIndent())
                
                // Create backup_metadata table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS backup_metadata (
                        id TEXT PRIMARY KEY NOT NULL,
                        timestamp INTEGER NOT NULL,
                        version INTEGER NOT NULL,
                        dataCount INTEGER NOT NULL,
                        checksum TEXT NOT NULL,
                        cloudBackupId TEXT,
                        isRestored INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())
            }
        }
    }
}
