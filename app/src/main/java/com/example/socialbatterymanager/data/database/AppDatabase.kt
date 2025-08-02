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
import com.example.socialbatterymanager.data.model.EnergyLog
import com.example.socialbatterymanager.data.model.User
import com.example.socialbatterymanager.data.model.Person
import com.example.socialbatterymanager.data.model.CalendarEvent
import com.example.socialbatterymanager.data.model.NotificationEntity

@Database(
    entities = [
        ActivityEntity::class,
        AuditLogEntity::class,
        BackupMetadataEntity::class,
        EnergyLog::class,
        User::class,
        Person::class,
        CalendarEvent::class,
        NotificationEntity::class
    ],
    version = 7, // Bump version to add notifications table
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun backupMetadataDao(): BackupMetadataDao
    abstract fun energyLogDao(): EnergyLogDao
    abstract fun userDao(): UserDao
    abstract fun personDao(): PersonDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun notificationDao(): NotificationDao

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
