package com.example.socialbatterymanager.data.repository

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyInfo
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory

class SecurityManager private constructor(private val context: Context) {
    
    private val keyAlias = "social_battery_db_key"
    private val prefsName = "encrypted_prefs"
    
    private val encryptedPrefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            prefsName,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    fun generateDatabasePassphrase(): String {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            
            // Regenerate the key if it was created without randomized encryption
            if (keyStore.containsAlias(keyAlias)) {
                val existingKey = keyStore.getKey(keyAlias, null) as SecretKey
                val factory = SecretKeyFactory.getInstance(existingKey.algorithm, "AndroidKeyStore")
                val keyInfo = factory.getKeySpec(existingKey, KeyInfo::class.java) as KeyInfo
                if (!keyInfo.isRandomizedEncryptionRequired) {
                    keyStore.deleteEntry(keyAlias)
                    encryptedPrefs.edit().remove("db_passphrase").apply()
                }
            }

            // Generate a key if it doesn't exist
            if (!keyStore.containsAlias(keyAlias)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()

                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
            
            // Get the key and create a passphrase
            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val passphrase = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)

            // Store the passphrase in encrypted preferences
            encryptedPrefs.edit().putString("db_passphrase", passphrase).apply()

            passphrase
        } catch (e: Exception) {
            // Fallback to a SecureRandom generated passphrase
            val fallbackPassphrase = generateFallbackPassphrase()
            encryptedPrefs.edit().putString("db_passphrase", fallbackPassphrase).apply()
            fallbackPassphrase
        }
    }
    
    fun getDatabasePassphrase(): String? {
        return encryptedPrefs.getString("db_passphrase", null)
    }
    
    fun isEncryptionEnabled(): Boolean {
        return encryptedPrefs.getBoolean("encryption_enabled", true)
    }
    
    fun setEncryptionEnabled(enabled: Boolean) {
        encryptedPrefs.edit().putBoolean("encryption_enabled", enabled).apply()
    }
    
    private fun generateFallbackPassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
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