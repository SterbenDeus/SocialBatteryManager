package com.example.socialbatterymanager.reports

import android.content.Intent
import com.example.socialbatterymanager.data.model.ActivityEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class ReportExportTest {

    @Test
    fun generatePdfReport_createsPdfFile() {
        val context = RuntimeEnvironment.getApplication()
        val activities = listOf(
            ActivityEntity(
                name = "Meeting",
                type = "Work",
                energy = 5,
                people = "Alice",
                mood = "Happy",
                notes = "Discuss project",
                date = System.currentTimeMillis()
            )
        )

        val file = ReportsFragment.generatePdfReport(context, activities)
        assertTrue(file.exists())
        assertTrue(file.name.endsWith(".pdf"))
        val header = file.inputStream().use { input ->
            ByteArray(4).also { input.read(it) }
        }
        assertEquals("%PDF", String(header, Charsets.US_ASCII))
    }

    @Test
    fun createShareIntent_hasPdfMimeType() {
        val context = RuntimeEnvironment.getApplication()
        val file = File(context.getExternalFilesDir(null), "dummy.pdf")
        file.writeText("test")

        val intent = ReportsFragment.createShareIntent(context, file)
        assertEquals(Intent.ACTION_SEND, intent.action)
        assertEquals("application/pdf", intent.type)
        assertTrue(intent.hasExtra(Intent.EXTRA_STREAM))
    }
}
