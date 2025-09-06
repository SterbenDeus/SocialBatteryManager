package com.example.socialbatterymanager.shared.utils

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.socialbatterymanager.R
import com.google.android.material.snackbar.Snackbar

/**
 * Utility class for handling error messages and user feedback
 */
object ErrorHandler {
    
    /**
     * Show error message using Snackbar with retry option
     */
    fun showErrorSnackbar(
        view: View,
        message: String,
        actionText: String? = null,
        actionCallback: (() -> Unit)? = null
    ) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        
        // Accessibility improvements
        snackbar.view.contentDescription = message
        snackbar.setTextColor(view.context.getColor(R.color.on_error_container_light))
        
        if (actionText != null && actionCallback != null) {
            snackbar.setAction(actionText) { actionCallback.invoke() }
            snackbar.setActionTextColor(view.context.getColor(R.color.primary_light))
        }
        
        snackbar.show()
    }
    
    /**
     * Show error message using Snackbar with predefined retry functionality
     */
    fun showRetryableErrorSnackbar(
        view: View,
        retryCallback: () -> Unit
    ) {
        showErrorSnackbar(
            view = view,
            message = view.context.getString(R.string.error_general),
            actionText = view.context.getString(R.string.retry),
            actionCallback = retryCallback
        )
    }
    
    /**
     * Show network error with retry option
     */
    fun showNetworkErrorSnackbar(
        view: View,
        retryCallback: () -> Unit
    ) {
        showErrorSnackbar(
            view = view,
            message = view.context.getString(R.string.error_network),
            actionText = view.context.getString(R.string.retry),
            actionCallback = retryCallback
        )
    }
    
    /**
     * Show offline status notification
     */
    fun showOfflineSnackbar(view: View) {
        val snackbar = Snackbar.make(
            view, 
            view.context.getString(R.string.error_offline), 
            Snackbar.LENGTH_SHORT
        )
        
        snackbar.view.contentDescription = view.context.getString(R.string.error_offline)
        snackbar.setBackgroundTint(view.context.getColor(R.color.surface_variant_light))
        snackbar.setTextColor(view.context.getColor(R.color.on_surface_variant_light))
        
        snackbar.show()
    }
    
    /**
     * Show simple toast message
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
    
    /**
     * Show toast for Fragment
     */
    fun showToast(fragment: Fragment, message: String, duration: Int = Toast.LENGTH_SHORT) {
        fragment.context?.let { context ->
            showToast(context, message, duration)
        }
    }
    
    /**
     * Handle common exceptions and show appropriate error messages
     */
    fun handleException(view: View, throwable: Throwable, retryCallback: (() -> Unit)? = null) {
        val context = view.context
        val message = when (throwable) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.ConnectException -> context.getString(R.string.error_network)
            is SecurityException -> context.getString(R.string.permission_denied)
            is IllegalArgumentException -> context.getString(R.string.invalid_input)
            else -> context.getString(R.string.error_general)
        }
        
        if (retryCallback != null) {
            showErrorSnackbar(
                view = view,
                message = message,
                actionText = context.getString(R.string.retry),
                actionCallback = retryCallback
            )
        } else {
            showErrorSnackbar(view, message)
        }
    }
}
