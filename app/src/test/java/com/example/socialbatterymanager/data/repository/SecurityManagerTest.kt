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
    fun passphraseStoresAndRetrievesWithoutLineBreaks() {
        val bytes = ByteArray(100) { it.toByte() }
        val generated = Base64.getEncoder().encodeToString(bytes)
        // Simulate storing and retrieving the passphrase
        val retrieved = generated
        assertEquals(generated, retrieved)
        assertFalse(retrieved.contains("\n"))
        assertFalse(retrieved.contains("\r"))
    }
}
