package com.example.socialbatterymanager.features.people

import android.content.Context
import androidx.room.Room
import com.example.socialbatterymanager.data.database.AppDatabase
import com.example.socialbatterymanager.data.model.ActivityEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ActivityDaoMemoryTest {
    @Test
    fun filteredQuery_usesLessMemoryThanFetchingAll() = runBlocking {
        val context: Context = RuntimeEnvironment.getApplication()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.activityDao()
        val targetName = "Alice"

        repeat(10000) { index ->
            val people = if (index % 200 == 0) "$targetName,Bob" else "Bob,Charlie"
            dao.insertActivity(
                ActivityEntity(
                    name = "Activity $index",
                    type = "Social",
                    energy = index % 5,
                    people = people,
                    mood = "Happy",
                    notes = "",
                    date = index.toLong()
                )
            )
        }

        val runtime = Runtime.getRuntime()
        System.gc()
        var before = runtime.totalMemory() - runtime.freeMemory()
        var allActivities: List<ActivityEntity>? = dao.getAllActivities().first()
        var after = runtime.totalMemory() - runtime.freeMemory()
        val memoryAll = after - before
        val expectedCount = allActivities!!.count { it.people.contains(targetName) }
        allActivities = null
        System.gc()

        before = runtime.totalMemory() - runtime.freeMemory()
        val filtered = dao.getActivitiesByPersonName(targetName)
        after = runtime.totalMemory() - runtime.freeMemory()
        val memoryFiltered = after - before

        assertEquals(expectedCount, filtered.size)
        assertTrue(memoryFiltered < memoryAll)
        db.close()
    }
}
