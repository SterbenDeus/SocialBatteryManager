package com.example.socialbatterymanager.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.robolectric.RobolectricTestRunner
import com.example.socialbatterymanager.data.model.ActivityEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.slot
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileWriter

@RunWith(RobolectricTestRunner::class)
class ImportExportManagerTest {

    @Test
    fun exportToPdf_createsMultiplePages() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val activities = (1..120).map {
            ActivityEntity(
                id = it,
                name = "Activity $it",
                type = "Type",
                energy = 5,
                people = "People",
                mood = "Mood",
                notes = "",
                date = System.currentTimeMillis(),
                lastModified = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        }

        val repository = mockk<ActivityRepository>()
        coEvery { repository.getAllActivities() } returns flowOf(activities)

        val manager = ImportExportManager(context, repository)
        val file = manager.exportToPdf()

        assertNotNull(file)
        PDDocument.load(file).use { doc ->
            assertTrue(doc.numberOfPages > 1)
        }
        file?.delete()
    }

    @Test
    fun exportToCsv_handlesSpecialCharacters() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val now = System.currentTimeMillis()
        val activities = listOf(
            ActivityEntity(
                id = 1,
                name = "Meeting, with team",
                type = "Work",
                energy = 5,
                people = "Alice, Bob",
                mood = "Happy \"Excited\"",
                notes = "Line1\nLine2",
                date = now,
                lastModified = now,
                updatedAt = now
            )
        )

        val repository = mockk<ActivityRepository>()
        coEvery { repository.getAllActivities() } returns flowOf(activities)

        val manager = ImportExportManager(context, repository)
        val file = manager.exportToCsv()

        assertNotNull(file)
        CSVReader(file!!.reader()).use { reader ->
            val rows = reader.readAll()
            assertEquals(2, rows.size)
            val row = rows[1]
            assertEquals("Meeting, with team", row[1])
            assertEquals("Alice, Bob", row[4])
            assertEquals("Happy \"Excited\"", row[5])
            assertEquals("Line1\nLine2", row[6])
        }
        file.delete()
    }

    @Test
    fun importFromCsv_handlesSpecialCharacters() = runBlocking {
        val context: Context = ApplicationProvider.getApplicationContext()
        val repository = mockk<ActivityRepository>()
        coEvery { repository.insertActivity(any(), any()) } returns Unit

        val manager = ImportExportManager(context, repository)
        val file = File(context.getExternalFilesDir(null), "import_test.csv")
        CSVWriter(FileWriter(file)).use { writer ->
            writer.writeNext(arrayOf("ID", "Name", "Type", "Energy", "People", "Mood", "Notes"))
            writer.writeNext(
                arrayOf(
                    "1",
                    "Meeting, with team",
                    "Work",
                    "5",
                    "Alice, Bob",
                    "Happy \"Excited\"",
                    "Line1\nLine2"
                )
            )
        }

        val result = manager.importFromCsv(file)
        assertEquals(1, result.successCount)
        val slot = slot<ActivityEntity>()
        coVerify { repository.insertActivity(capture(slot), "import") }
        assertEquals("Meeting, with team", slot.captured.name)
        assertEquals("Alice, Bob", slot.captured.people)
        assertEquals("Happy \"Excited\"", slot.captured.mood)
        assertEquals("Line1\nLine2", slot.captured.notes)
        file.delete()
    }
}
