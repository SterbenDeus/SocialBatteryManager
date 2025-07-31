package com.example.socialbatterymanager.shared.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.socialbatterymanager.R

/**
 * Utility class for handling biometric authentication
 */
class BiometricAuthHelper(private val fragment: Fragment) {
    
    private val context: Context = fragment.requireContext()
    
    /**
     * Check if biometric authentication is available
     */
    fun isBiometricAvailable(): BiometricAvailability {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or 
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HW_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.UNKNOWN
            else -> BiometricAvailability.UNKNOWN
        }
    }
    
    /**
     * Show biometric authentication prompt
     */
    fun showBiometricPrompt(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(fragment, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errString.toString())
            }
            
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        })
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.biometric_title))
            .setSubtitle(context.getString(R.string.biometric_subtitle))
            .setDescription(context.getString(R.string.biometric_description))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or 
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Check if biometric authentication should be shown
     */
    fun shouldShowBiometricPrompt(): Boolean {
        return isBiometricAvailable() == BiometricAvailability.AVAILABLE
    }
}

/**
 * Enum representing biometric availability status
 */
enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    NONE_ENROLLED,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED,
    UNKNOWN
}

/**
 * Extension function to get user-friendly message for biometric availability
 */
fun BiometricAvailability.getMessage(context: Context): String {
    return when (this) {
        BiometricAvailability.AVAILABLE -> "Biometric authentication available"
        BiometricAvailability.NO_HARDWARE -> "No biometric hardware available"
        BiometricAvailability.HW_UNAVAILABLE -> "Biometric hardware unavailable"
        BiometricAvailability.NONE_ENROLLED -> "No biometric credentials enrolled"
        BiometricAvailability.SECURITY_UPDATE_REQUIRED -> "Security update required"
        BiometricAvailability.UNSUPPORTED -> "Biometric authentication not supported"
        BiometricAvailability.UNKNOWN -> "Unknown biometric status"
    }
}