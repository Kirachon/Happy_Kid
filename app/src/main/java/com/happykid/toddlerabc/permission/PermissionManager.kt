package com.happykid.toddlerabc.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Permission Manager for Happy_Kid App
 * Phase 7: Microphone permission handling with parental consent flow
 *
 * This manager provides secure microphone permission handling specifically designed
 * for toddler applications with parental consent requirements and COPPA compliance.
 * Includes graceful degradation when permissions are denied.
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "PermissionManager"
        const val MICROPHONE_PERMISSION = Manifest.permission.RECORD_AUDIO
        private const val PARENTAL_CONSENT_KEY = "parental_consent_microphone"
        private const val PERMISSION_DENIED_COUNT_KEY = "microphone_permission_denied_count"
        private const val MAX_PERMISSION_REQUESTS = 2
    }

    private val sharedPreferences = context.getSharedPreferences("happy_kid_permissions", Context.MODE_PRIVATE)

    // Permission state management
    private val _microphonePermissionState = MutableStateFlow(getMicrophonePermissionStatus())
    val microphonePermissionState: StateFlow<PermissionState> = _microphonePermissionState.asStateFlow()

    private val _parentalConsentGiven = MutableStateFlow(getParentalConsentStatus())
    val parentalConsentGiven: StateFlow<Boolean> = _parentalConsentGiven.asStateFlow()

    private val _shouldShowPermissionRationale = MutableStateFlow(false)
    val shouldShowPermissionRationale: StateFlow<Boolean> = _shouldShowPermissionRationale.asStateFlow()

    private val _permissionDeniedCount = MutableStateFlow(getPermissionDeniedCount())
    val permissionDeniedCount: StateFlow<Int> = _permissionDeniedCount.asStateFlow()

    init {
        updatePermissionStates()
    }

    /**
     * Check if microphone permission is granted
     */
    fun isMicrophonePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            MICROPHONE_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Check if speech recognition features are available
     * Requires both permission and parental consent
     */
    fun isSpeechRecognitionAvailable(): Boolean {
        return isMicrophonePermissionGranted() && _parentalConsentGiven.value
    }

    /**
     * Get current microphone permission status
     */
    private fun getMicrophonePermissionStatus(): PermissionState {
        return when {
            isMicrophonePermissionGranted() -> PermissionState.Granted
            getPermissionDeniedCount() >= MAX_PERMISSION_REQUESTS -> PermissionState.PermanentlyDenied
            else -> PermissionState.NotRequested
        }
    }

    /**
     * Update permission states based on current status
     */
    fun updatePermissionStates() {
        _microphonePermissionState.value = getMicrophonePermissionStatus()
        _parentalConsentGiven.value = getParentalConsentStatus()
        _permissionDeniedCount.value = getPermissionDeniedCount()
        
        Log.d(TAG, "Permission states updated - Microphone: ${_microphonePermissionState.value}, Consent: ${_parentalConsentGiven.value}")
    }

    /**
     * Handle permission request result
     * Phase 7: Toddler-friendly permission handling
     */
    fun onPermissionResult(permission: String, isGranted: Boolean) {
        when (permission) {
            MICROPHONE_PERMISSION -> {
                if (isGranted) {
                    _microphonePermissionState.value = PermissionState.Granted
                    Log.d(TAG, "Microphone permission granted")
                } else {
                    incrementPermissionDeniedCount()
                    _microphonePermissionState.value = if (getPermissionDeniedCount() >= MAX_PERMISSION_REQUESTS) {
                        PermissionState.PermanentlyDenied
                    } else {
                        PermissionState.Denied
                    }
                    Log.d(TAG, "Microphone permission denied (count: ${getPermissionDeniedCount()})")
                }
            }
        }
    }

    /**
     * Set parental consent for microphone usage
     * Phase 7: COPPA compliance with parental consent
     */
    fun setParentalConsent(granted: Boolean) {
        sharedPreferences.edit()
            .putBoolean(PARENTAL_CONSENT_KEY, granted)
            .apply()
        
        _parentalConsentGiven.value = granted
        
        Log.d(TAG, "Parental consent for microphone: $granted")
    }

    /**
     * Get parental consent status
     */
    private fun getParentalConsentStatus(): Boolean {
        return sharedPreferences.getBoolean(PARENTAL_CONSENT_KEY, false)
    }

    /**
     * Get permission denied count
     */
    private fun getPermissionDeniedCount(): Int {
        return sharedPreferences.getInt(PERMISSION_DENIED_COUNT_KEY, 0)
    }

    /**
     * Increment permission denied count
     */
    private fun incrementPermissionDeniedCount() {
        val currentCount = getPermissionDeniedCount()
        val newCount = currentCount + 1
        sharedPreferences.edit()
            .putInt(PERMISSION_DENIED_COUNT_KEY, newCount)
            .apply()
        
        _permissionDeniedCount.value = newCount
    }

    /**
     * Reset permission denied count (for testing or reset scenarios)
     */
    fun resetPermissionDeniedCount() {
        sharedPreferences.edit()
            .putInt(PERMISSION_DENIED_COUNT_KEY, 0)
            .apply()
        
        _permissionDeniedCount.value = 0
        updatePermissionStates()
    }

    /**
     * Check if we should show permission rationale
     */
    fun updateShouldShowRationale(shouldShow: Boolean) {
        _shouldShowPermissionRationale.value = shouldShow
    }

    /**
     * Get user-friendly permission status message
     * Phase 7: Child and parent-friendly messaging
     */
    fun getPermissionStatusMessage(): String {
        return when (_microphonePermissionState.value) {
            PermissionState.Granted -> {
                if (_parentalConsentGiven.value) {
                    "Speech features are ready! 🎤"
                } else {
                    "Microphone permission granted. Parental consent needed for speech features."
                }
            }
            PermissionState.Denied -> "Microphone permission needed for speaking activities"
            PermissionState.PermanentlyDenied -> "Please enable microphone permission in Settings to use speech features"
            PermissionState.NotRequested -> "Speech features require microphone permission"
        }
    }

    /**
     * Get parental consent message
     */
    fun getParentalConsentMessage(): String {
        return """
            Happy Kid would like to use the microphone for speech recognition activities.
            
            This helps your child:
            • Practice letter pronunciation
            • Build speaking confidence
            • Engage with interactive learning
            
            All speech processing happens on your device - no data is sent to servers.
            
            Do you give consent for microphone usage?
        """.trimIndent()
    }

    /**
     * Check if permission can be requested
     */
    fun canRequestPermission(): Boolean {
        return _microphonePermissionState.value != PermissionState.PermanentlyDenied &&
                getPermissionDeniedCount() < MAX_PERMISSION_REQUESTS
    }

    /**
     * Get permission request rationale for parents
     */
    fun getPermissionRationale(): String {
        return """
            Microphone Permission for Speech Learning
            
            Happy Kid uses the microphone to help your toddler practice speaking and pronunciation.
            
            Features include:
            • Letter pronunciation practice
            • Vocal effort recognition
            • Speaking confidence building
            
            Privacy & Safety:
            • All processing happens on your device
            • No audio is recorded or stored
            • No data is sent to external servers
            • You can disable this feature anytime
            
            This permission helps create an interactive learning experience for your child.
        """.trimIndent()
    }

    /**
     * Clear all permission data (for reset/testing)
     */
    fun clearAllPermissionData() {
        sharedPreferences.edit()
            .remove(PARENTAL_CONSENT_KEY)
            .remove(PERMISSION_DENIED_COUNT_KEY)
            .apply()
        
        updatePermissionStates()
        Log.d(TAG, "All permission data cleared")
    }
}

/**
 * Permission state enumeration for clear state management
 * Phase 7: Comprehensive permission state tracking
 */
enum class PermissionState {
    NotRequested,
    Granted,
    Denied,
    PermanentlyDenied
}
