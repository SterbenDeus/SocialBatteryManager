package com.example.socialbatterymanager.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class SecurityManager private constructor(private val context: Context) {
    
    private val keyAlias = "social_battery_db_key"
    private val prefsName = "encrypted_prefs"
    
    private val encryptedPrefs by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            prefsName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    fun generateDatabasePassphrase(): String {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            
            // Generate a key if it doesn't exist
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(false)
                    .build()
                
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
            
            // Get the key and create a passphrase
            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val passphrase = android.util.Base64.encodeToString(secretKey.encoded, android.util.Base64.NO_WRAP)

            // Store the passphrase in encrypted preferences
            encryptedPrefs.edit().putString("db_passphrase", passphrase).apply()

            passphrase
        } catch (e: Exception) {
            // Fallback to a simple generated passphrase
            val fallbackPassphrase = generateFallbackPassphrase()
            encryptedPrefs.edit().putString("db_passphrase", fallbackPassphrase).apply()
            fallbackPassphrase
        }
    }
    
    fun getDatabasePassphrase(): String? {
        return encryptedPrefs.getString("db_passphrase", null)
            ?.replace("\n", "")
            ?.replace("\r", "")
    }
    
    fun isEncryptionEnabled(): Boolean {
        return encryptedPrefs.getBoolean("encryption_enabled", true)
    }
    
    fun setEncryptionEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean("encryption_enabled", enabled).apply()
    }
    
    private fun generateFallbackPassphrase(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { chars.random() }
            .joinToString("")
    }
    
    fun clearEncryptionData() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(keyAlias)
        } catch (e: Exception) {
            // Ignore errors when clearing
        }
        
        encryptedPrefs.edit().clear().apply()
    }
    
    companion object {
        @Volatile
        private var INSTANCE: SecurityManager? = null
        
        fun getInstance(context: Context): SecurityManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SecurityManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}