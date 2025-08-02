package com.example.socialbatterymanager.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    exportSchema = true
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE activities ADD COLUMN syncStatus TEXT NOT NULL DEFAULT 'PENDING_SYNC'")
                db.execSQL("ALTER TABLE activities ADD COLUMN lastModified INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE activities ADD COLUMN firebaseId TEXT")
                db.execSQL("ALTER TABLE activities ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE activities ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE activities ADD COLUMN rating REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE activities ADD COLUMN usageCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS audit_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "entityType TEXT NOT NULL, " +
                        "entityId TEXT NOT NULL, " +
                        "action TEXT NOT NULL, " +
                        "oldValues TEXT, " +
                        "newValues TEXT, " +
                        "timestamp INTEGER NOT NULL, " +
                        "userId TEXT)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS backup_metadata (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "timestamp INTEGER NOT NULL, " +
                        "version INTEGER NOT NULL, " +
                        "dataCount INTEGER NOT NULL, " +
                        "checksum TEXT NOT NULL, " +
                        "cloudBackupId TEXT, " +
                        "isRestored INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS energy_logs (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "energyLevel INTEGER NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "changeAmount INTEGER NOT NULL DEFAULT 0, " +
                        "reason TEXT)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS users (" +
                        "id TEXT NOT NULL PRIMARY KEY, " +
                        "name TEXT NOT NULL, " +
                        "email TEXT NOT NULL, " +
                        "photoUri TEXT, " +
                        "batteryCapacity INTEGER NOT NULL DEFAULT 100, " +
                        "warningLevel INTEGER NOT NULL DEFAULT 30, " +
                        "criticalLevel INTEGER NOT NULL DEFAULT 10, " +
                        "currentMood TEXT NOT NULL DEFAULT 'neutral', " +
                        "notificationsEnabled INTEGER NOT NULL DEFAULT 1, " +
                        "reminderFrequency INTEGER NOT NULL DEFAULT 60, " +
                        "lastUpdated INTEGER NOT NULL)"
                )
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS people (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "name TEXT NOT NULL, " +
                        "email TEXT, " +
                        "phone TEXT, " +
                        "avatarPath TEXT, " +
                        "notes TEXT, " +
                        "createdAt INTEGER NOT NULL, " +
                        "label TEXT NOT NULL DEFAULT 'FRIEND', " +
                        "socialEnergyLevel INTEGER NOT NULL DEFAULT 50, " +
                        "mood TEXT NOT NULL DEFAULT 'NEUTRAL')"
                )
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS calendar_events (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "description TEXT NOT NULL DEFAULT '', " +
                        "startTime INTEGER NOT NULL, " +
                        "endTime INTEGER NOT NULL, " +
                        "location TEXT NOT NULL DEFAULT '', " +
                        "source TEXT NOT NULL DEFAULT '', " +
                        "externalId TEXT NOT NULL DEFAULT '', " +
                        "isImported INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS notifications (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "type TEXT NOT NULL, " +
                        "title TEXT NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "timestamp INTEGER NOT NULL, " +
                        "isRead INTEGER NOT NULL DEFAULT 0, " +
                        "activityId INTEGER, " +
                        "actionData TEXT)"
                )
            }
        }

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
            builder.addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            return builder.build()
        }

        fun clearInstance() {
            INSTANCE = null
        }
    }
}
