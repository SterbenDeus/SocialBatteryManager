package com.example.socialbatterymanager.data.repository

import com.example.socialbatterymanager.data.database.ActivityDao
import com.example.socialbatterymanager.data.database.AuditLogDao
import com.example.socialbatterymanager.data.database.BackupMetadataDao
import com.example.socialbatterymanager.data.model.ActivityEntity
import com.example.socialbatterymanager.data.model.AuditLogEntity
import com.google.gson.Gson
import java.nio.charset.Charset
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupRepositoryChecksumTest {
    private fun createRepository(): BackupRepository {
        val constructor = BackupRepository::class.java.getDeclaredConstructor(
            ActivityDao::class.java,
            AuditLogDao::class.java,
            BackupMetadataDao::class.java,
            Gson::class.java
        )
        constructor.isAccessible = true
        return constructor.newInstance(null, null, null, Gson())
    }

    private fun invokeChecksum(
        repo: BackupRepository,
        activities: List<ActivityEntity>,
        auditLogs: List<AuditLogEntity>
    ): String {
        val method = BackupRepository::class.java.getDeclaredMethod(
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

