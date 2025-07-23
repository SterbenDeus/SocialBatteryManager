package com.example.socialbatterymanager

import hasDuplicates
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilsTest {

    @Test
    fun testHasDuplicates_withDuplicates() {
        val list = listOf(1, 2, 3, 4, 1)
        assertTrue(hasDuplicates(list))
    }

    @Test
    fun testHasDuplicates_withoutDuplicates() {
        val list = listOf(1, 2, 3, 4, 5)
        assertFalse(hasDuplicates(list))
    }

    @Test
    fun testHasDuplicates_emptyList() {
        val list = emptyList<Int>()
        assertFalse(hasDuplicates(list))
    }

    @Test
    fun testHasDuplicates_singleElement() {
        val list = listOf(1)
        assertFalse(hasDuplicates(list))
    }
}