package com.example.socialbatterymanager.data.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AppDatabaseTest {

    @Test
    fun getDatabase_withHigherVersion_fallbackDestructiveMigration() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("social_battery_db")
        if (dbFile.exists()) dbFile.delete()

        val sqlite = SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        sqlite.version = 99
        sqlite.close()

        AppDatabase.clearInstance()
        val db = AppDatabase.getDatabase(context)
        val version = db.openHelper.writableDatabase.version
        assertEquals(7, version)
        db.close()
    }

    @Test
    fun getDatabase_invalidPassphrase_throws() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbFile = context.getDatabasePath("social_battery_db")
        if (dbFile.exists()) dbFile.delete()

        val passphrase = "correct"
        AppDatabase.clearInstance()
        val db = AppDatabase.getDatabase(context, passphrase)
        db.close()
        AppDatabase.clearInstance()

        assertThrows(IllegalStateException::class.java) {
            AppDatabase.getDatabase(context, "wrong")
        }
    }
}

