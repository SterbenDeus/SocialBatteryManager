package com.example.socialbatterymanager.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.Base64

class SecurityManagerTest {

    @Test
    fun encodingWithoutWrap_producesPassphraseWithoutLineBreaks() {
        val bytes = ByteArray(100) { it.toByte() }
        val encoded = Base64.getEncoder().encodeToString(bytes)
        assertFalse(encoded.contains("\n"))
        assertFalse(encoded.contains("\r"))
    }

    @Test
    fun retrievalSanitizesLineBreaks() {
        val bytes = ByteArray(100) { it.toByte() }
        val encodedWithBreaks = Base64.getMimeEncoder().encodeToString(bytes)
        val sanitized = encodedWithBreaks.replace("\n", "").replace("\r", "")
        assertFalse(sanitized.contains("\n"))
        assertFalse(sanitized.contains("\r"))
        // Sanitation should not alter content other than removing line breaks
        assertEquals(encodedWithBreaks.filter { it != '\n' && it != '\r' }, sanitized)
    }
}
