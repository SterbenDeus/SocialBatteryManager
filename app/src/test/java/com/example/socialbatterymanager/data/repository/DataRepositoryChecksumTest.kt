package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.*
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.google.gson.Gson
import java.nio.charset.Charset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeAppDatabase : AppDatabase() {
    override fun activityDao(): ActivityDao = throw NotImplementedError()
    override fun auditLogDao(): AuditLogDao = throw NotImplementedError()
    override fun backupMetadataDao(): BackupMetadataDao = throw NotImplementedError()
    override fun energyLogDao(): EnergyLogDao = throw NotImplementedError()
    override fun userDao(): UserDao = throw NotImplementedError()
    override fun personDao(): PersonDao = throw NotImplementedError()
    override fun calendarEventDao(): CalendarEventDao = throw NotImplementedError()
    override fun notificationDao(): NotificationDao = throw NotImplementedError()
}

class DataRepositoryChecksumTest {
    private fun createRepository(): DataRepository {
        val constructor = DataRepository::class.java.getDeclaredConstructor(AppDatabase::class.java, Gson::class.java)
        constructor.isAccessible = true
        return constructor.newInstance(FakeAppDatabase(), Gson())
    }

    private fun invokeChecksum(
        repo: DataRepository,
        activities: List<ActivityEntity>,
        auditLogs: List<AuditLogEntity>
    ): String {
        val method = DataRepository::class.java.getDeclaredMethod(
            "generateChecksum",
            List::class.java,
            List::class.java
        )
        method.isAccessible = true
        return method.invoke(repo, activities, auditLogs) as String
    }

    @Test
    fun checksum_isConsistentAcrossLocales() {
        val repo = createRepository()
        val activities = listOf(
            ActivityEntity(
                name = "Ångström",
                type = "Social",
                energy = 1,
                people = "Før",
                mood = "Ok",
                notes = "ñandú",
                date = 0
            )
        )
        val auditLogs = emptyList<AuditLogEntity>()

        val defaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
        val checksumUs = invokeChecksum(repo, activities, auditLogs)

        Locale.setDefault(Locale("tr", "TR"))
        val checksumTr = invokeChecksum(repo, activities, auditLogs)
        Locale.setDefault(defaultLocale)

        assertEquals(checksumUs, checksumTr)
    }

    @Test
    fun checksum_isConsistentAcrossEncodings() {
        val repo = createRepository()
        val activities = listOf(
            ActivityEntity(
                name = "Ångström",
                type = "Social",
                energy = 1,
                people = "Før",
                mood = "Ok",
                notes = "ñandú",
                date = 0
            )
        )
        val auditLogs = emptyList<AuditLogEntity>()

        val baseline = invokeChecksum(repo, activities, auditLogs)

        val originalCharset = Charset.defaultCharset()
        val charsetField = Charset::class.java.getDeclaredField("defaultCharset")
        charsetField.isAccessible = true
        try {
            System.setProperty("file.encoding", "ISO-8859-1")
            charsetField.set(null, null)
            val changed = invokeChecksum(repo, activities, auditLogs)
            assertEquals(baseline, changed)
        } finally {
            System.setProperty("file.encoding", originalCharset.name())
            charsetField.set(null, null)
        }
    }
}

