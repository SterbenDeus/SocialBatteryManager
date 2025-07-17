package com.example.socialbatterymanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.socialbatterymanager.model.Person


@Database(entities = [ActivityEntity::class, CalendarEvent::class], version = 2)

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [ActivityEntity::class], version = 2)

abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun calendarEventDao(): CalendarEventDao


@Database(entities = [ActivityEntity::class, Person::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun personDao(): PersonDao

import com.example.socialbatterymanager.model.User

@Database(entities = [ActivityEntity::class, User::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao


import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [ActivityEntity::class], 
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)

import com.example.socialbatterymanager.model.EnergyLog

@Database(entities = [ActivityEntity::class, EnergyLog::class], version = 2)

abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun energyLogDao(): EnergyLogDao




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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns with default values
                database.execSQL("ALTER TABLE activities ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE activities ADD COLUMN rating REAL NOT NULL DEFAULT 0.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "social_battery_db"


                ).fallbackToDestructiveMigration().build()


                ).fallbackToDestructiveMigration().build()


                ).fallbackToDestructiveMigration()
                .build()


                )
                .addMigrations(MIGRATION_1_2)
                .build()

                ).fallbackToDestructiveMigration()
                 .build()



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
