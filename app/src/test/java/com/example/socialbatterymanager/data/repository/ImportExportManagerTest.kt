package com.example.socialbatterymanager.data.repository

import android.content.Context
import com.example.socialbatterymanager.data.model.ActivityEntity
import io.mockk.coEvery
import io.mockk.coAnswers
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import com.opencsv.CSVWriter
import java.io.FileWriter

class ImportExportManagerTest {

    private fun tempFileWithContent(content: String): File {
        val file = File.createTempFile("import_test", ".csv")
        file.writeText(content.trimIndent())
        file.deleteOnExit()
        return file
    }

    @Test
    fun importHandlesCommas() = runBlocking {
        val repository = mockk<ActivityRepository>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val inserted = mutableListOf<ActivityEntity>()
        coEvery { repository.insertActivity(any(), any()) } coAnswers {
            inserted.add(firstArg())
        }

        val manager = ImportExportManager(context, repository)
        val csvContent = """
            ID , Name , Type , Energy , People , Mood , Notes
            1,"Lunch, Meeting",Work,5,"John, Doe",Happy,"Note, with comma"
        """
        val file = tempFileWithContent(csvContent)

        val result = manager.importFromCsv(file)

        assertEquals(1, result.successCount)
        assertEquals(0, result.errorCount)
        assertEquals("Lunch, Meeting", inserted[0].name)
        assertEquals("John, Doe", inserted[0].people)
        assertEquals("Note, with comma", inserted[0].notes)
    }

    @Test
    fun importHandlesQuotes() = runBlocking {
        val repository = mockk<ActivityRepository>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val inserted = mutableListOf<ActivityEntity>()
        coEvery { repository.insertActivity(any(), any()) } coAnswers {
            inserted.add(firstArg())
        }

        val file = File.createTempFile("import_quotes", ".csv")
        file.deleteOnExit()
        CSVWriter(FileWriter(file)).use { writer ->
            writer.writeNext(arrayOf("ID", "Name", "Type", "Energy", "People", "Mood", "Notes"))
            writer.writeNext(
                arrayOf(
                    "1",
                    "Meeting \"Important\"",
                    "Work",
                    "5",
                    "Group",
                    "Excited",
                    "He said \"hello\""
                )
            )
        }

        val manager = ImportExportManager(context, repository)
        val result = manager.importFromCsv(file)

        assertEquals(1, result.successCount)
        assertEquals(0, result.errorCount)
        assertEquals("Meeting \"Important\"", inserted[0].name)
        assertEquals("He said \"hello\"", inserted[0].notes)
    }

    @Test
    fun importSkipsMalformedRows() = runBlocking {
        val repository = mockk<ActivityRepository>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        val inserted = mutableListOf<ActivityEntity>()
        coEvery { repository.insertActivity(any(), any()) } coAnswers {
            inserted.add(firstArg())
        }

        val csvContent = """
            ID,Name,Type,Energy,People,Mood,Notes
            1,Meeting,Work,5,Group,Happy,Note
            2,BadRow,OnlyThree
        """
        val file = tempFileWithContent(csvContent)

        val manager = ImportExportManager(context, repository)
        val result = manager.importFromCsv(file)

        assertEquals(1, result.successCount)
        assertEquals(1, result.errorCount)
        assertEquals("Meeting", inserted[0].name)
    }
}

