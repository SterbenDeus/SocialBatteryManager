package com.example.socialbatterymanager.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.socialbatterymanager.data.model.ActivityEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
}
