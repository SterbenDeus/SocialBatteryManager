package com.example.socialbatterymanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ActivityEntity::class], 
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for enhanced functionality
                database.execSQL("ALTER TABLE activities ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN location TEXT")
                database.execSQL("ALTER TABLE activities ADD COLUMN socialInteractionLevel INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN stressLevel INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN isManualEntry INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE activities ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
                database.execSQL("ALTER TABLE activities ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN firebaseId TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "social_battery_db"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @androidx.room.TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @androidx.room.TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return SyncStatus.valueOf(status)
    }
}
