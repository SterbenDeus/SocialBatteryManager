package com.example.socialbatterymanager

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test

class ResourceStringsTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun permissionDeniedStringMatches() {
        assertEquals("Permission denied", context.getString(R.string.permission_denied))
    }

    @Test
    fun addPersonStringMatches() {
        assertEquals("Add Person", context.getString(R.string.add_person))
    }
}
