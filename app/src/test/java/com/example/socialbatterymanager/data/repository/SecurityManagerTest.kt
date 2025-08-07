package com.example.socialbatterymanager.data.repository

import android.content.SharedPreferences
import org.robolectric.RobolectricTestRunner
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.io.File
import java.util.Base64

@RunWith(RobolectricTestRunner::class)
class SecurityManagerTest {

    @Test
    fun generatePassphrase_isStoredEncrypted() {
        val context = RuntimeEnvironment.getApplication()
        val manager = SecurityManager.getInstance(context)
        manager.clearEncryptionData()

        val passphrase = manager.generateDatabasePassphrase()
        val retrieved = manager.getDatabasePassphrase()
        assertEquals(passphrase, retrieved)

        val field = SecurityManager::class.java.getDeclaredField("encryptedPrefs")
        field.isAccessible = true
        val prefs = field.get(manager) as SharedPreferences
        val stored = prefs.getString("db_passphrase", null)
        assertNotEquals(passphrase, stored)

        val prefsFile = File(context.filesDir.parent + "/shared_prefs/encrypted_prefs.xml")
        val content = prefsFile.readText()
        assertFalse(content.contains("db_passphrase"))
        assertFalse(content.contains(passphrase))

        manager.clearEncryptionData()
    }

    @Test
    fun fallbackPassphrase_usesSecureRandom() {
        val context = RuntimeEnvironment.getApplication()
        val manager = SecurityManager.getInstance(context)
        val method = SecurityManager::class.java.getDeclaredMethod("generateFallbackPassphrase")
        method.isAccessible = true
        val pass1 = method.invoke(manager) as String
        val pass2 = method.invoke(manager) as String
        assertNotEquals(pass1, pass2)
        assertFalse(pass1.contains("\n"))
    }

    @Test
    fun encodingWithoutWrap_producesPassphraseWithoutLineBreaks() {
        val bytes = ByteArray(100) { it.toByte() }
        val encoded = Base64.getEncoder().encodeToString(bytes)
        assertFalse(encoded.contains("\n"))
        assertFalse(encoded.contains("\r"))
    }
}

