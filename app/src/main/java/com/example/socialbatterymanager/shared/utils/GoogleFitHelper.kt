package com.example.socialbatterymanager.shared.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Helper class for Google Fit integration
 */
class GoogleFitHelper(private val context: Context) {
    
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_SLEEP_SEGMENT, FitnessOptions.ACCESS_READ)
        .build()
    
    /**
     * Check if Google Fit permissions are granted
     */
    fun hasPermissions(): Boolean {
        return GoogleSignIn.hasPermissions(getGoogleAccount(), fitnessOptions)
    }
    
    /**
     * Request Google Fit permissions
     */
    fun requestPermissions(fragment: Fragment, requestLauncher: ActivityResultLauncher<Intent>) {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .addExtension(fitnessOptions)
            .build()
        
        val client = GoogleSignIn.getClient(fragment.requireActivity(), signInOptions)
        requestLauncher.launch(client.signInIntent)
    }
    
    /**
     * Get current Google account
     */
    fun getGoogleAccount(): GoogleSignInAccount {
        return GoogleSignIn.getAccountForExtension(context, fitnessOptions)
    }
    
    /**
     * Get step count for the current day
     */
    suspend fun getTodayStepCount(): Int = suspendCancellableCoroutine { continuation ->
        if (!hasPermissions()) {
            continuation.resumeWithException(SecurityException("Google Fit permissions not granted"))
            return@suspendCancellableCoroutine
        }
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)
        
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        
        Fitness.getHistoryApi(context, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val stepCount = response.buckets.firstOrNull()
                    ?.dataSets?.firstOrNull()
                    ?.dataPoints?.firstOrNull()
                    ?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                
                continuation.resume(stepCount)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
    
    /**
     * Get activity data for social battery correlation
     */
    suspend fun getActivityData(): List<ActivityData> = suspendCancellableCoroutine { continuation ->
        if (!hasPermissions()) {
            continuation.resumeWithException(SecurityException("Google Fit permissions not granted"))
            return@suspendCancellableCoroutine
        }
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(7) // Last 7 days
        
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_ACTIVITY_SEGMENT)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        
        Fitness.getHistoryApi(context, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val activities = response.dataSets.flatMap { dataSet ->
                    dataSet.dataPoints.map { dataPoint ->
                        val activityType = dataPoint.getValue(Field.FIELD_ACTIVITY).asInt()
                        val startTime = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                        val endTime = dataPoint.getEndTime(TimeUnit.MILLISECONDS)
                        
                        ActivityData(
                            activityType = activityType,
                            startTime = startTime,
                            endTime = endTime,
                            duration = endTime - startTime
                        )
                    }
                }
                
                continuation.resume(activities)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
    
    /**
     * Get heart rate data for stress level analysis
     */
    suspend fun getHeartRateData(): List<HeartRateData> = suspendCancellableCoroutine { continuation ->
        if (!hasPermissions()) {
            continuation.resumeWithException(SecurityException("Google Fit permissions not granted"))
            return@suspendCancellableCoroutine
        }
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.HOURS.toMillis(24) // Last 24 hours
        
        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_HEART_RATE_BPM)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()
        
        Fitness.getHistoryApi(context, getGoogleAccount())
            .readData(readRequest)
            .addOnSuccessListener { response ->
                val heartRateData = response.dataSets.flatMap { dataSet ->
                    dataSet.dataPoints.map { dataPoint ->
                        val bpm = dataPoint.getValue(Field.FIELD_BPM).asFloat()
                        val timestamp = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                        
                        HeartRateData(
                            bpm = bpm,
                            timestamp = timestamp
                        )
                    }
                }
                
                continuation.resume(heartRateData)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }
}

/**
 * Data class for activity information
 */
data class ActivityData(
    val activityType: Int,
    val startTime: Long,
    val endTime: Long,
    val duration: Long
)

/**
 * Data class for heart rate information
 */
data class HeartRateData(
    val bpm: Float,
    val timestamp: Long
)

/**
 * Extension function to get activity name from type
 */
fun Int.getActivityName(): String {
    return when (this) {
        0 -> "In vehicle"
        1 -> "Biking"
        2 -> "On foot"
        3 -> "Still"
        4 -> "Unknown"
        5 -> "Tilting"
        7 -> "Walking"
        8 -> "Running"
        else -> "Unknown"
    }
}