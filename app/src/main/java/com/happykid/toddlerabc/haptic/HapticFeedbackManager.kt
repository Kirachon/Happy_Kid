package com.happykid.toddlerabc.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.happykid.toddlerabc.util.WindowsDeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Haptic Feedback Manager for Happy_Kid App
 * Phase 8: Tactile feedback system for tracing interactions
 *
 * Provides customizable haptic feedback patterns for tracing success, guidance,
 * and encouragement with Windows emulator simulation for development testing.
 */
@Singleton
class HapticFeedbackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "HapticFeedbackManager"
        
        // Vibration durations in milliseconds
        private const val SUCCESS_VIBRATION_MS = 100L
        private const val GUIDANCE_VIBRATION_MS = 50L
        private const val ENCOURAGEMENT_VIBRATION_MS = 75L
        private const val MILESTONE_VIBRATION_MS = 200L
        private const val ERROR_VIBRATION_MS = 150L
        
        // Vibration intensities (0-255)
        private const val LIGHT_INTENSITY = 80
        private const val MEDIUM_INTENSITY = 150
        private const val STRONG_INTENSITY = 255
    }

    private val vibrator: Vibrator? = getVibrator()
    private val isWindowsEmulator = WindowsDeviceUtils.isWindowsEmulator()

    // Haptic feedback state
    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _hapticIntensity = MutableStateFlow(MEDIUM_INTENSITY)
    val hapticIntensity: StateFlow<Int> = _hapticIntensity.asStateFlow()

    init {
        initializeHapticFeedback()
    }

    /**
     * Initialize haptic feedback system
     */
    private fun initializeHapticFeedback() {
        val hasVibrator = vibrator?.hasVibrator() ?: false
        
        if (isWindowsEmulator) {
            Log.d(TAG, "Haptic feedback initialized for Windows emulator (simulated)")
        } else if (hasVibrator) {
            Log.d(TAG, "Haptic feedback initialized successfully")
        } else {
            Log.w(TAG, "Device does not support haptic feedback")
            _isHapticEnabled.value = false
        }
    }

    /**
     * Get vibrator instance based on Android version
     */
    private fun getVibrator(): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get vibrator service", e)
            null
        }
    }

    /**
     * Provide haptic feedback for successful stroke completion
     * Phase 8: Success feedback for completed tracing strokes
     */
    fun provideSuccessFeedback() {
        if (!_isHapticEnabled.value) return

        if (isWindowsEmulator) {
            simulateHapticFeedback("Success vibration", SUCCESS_VIBRATION_MS)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(
                    SUCCESS_VIBRATION_MS,
                    _hapticIntensity.value
                )
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(SUCCESS_VIBRATION_MS)
            }
            
            Log.d(TAG, "Success haptic feedback provided")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provide success haptic feedback", e)
        }
    }

    /**
     * Provide haptic feedback for guidance corrections
     * Phase 8: Gentle guidance for tracing direction
     */
    fun provideGuidanceFeedback() {
        if (!_isHapticEnabled.value) return

        if (isWindowsEmulator) {
            simulateHapticFeedback("Guidance vibration", GUIDANCE_VIBRATION_MS)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(
                    GUIDANCE_VIBRATION_MS,
                    LIGHT_INTENSITY
                )
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(GUIDANCE_VIBRATION_MS)
            }
            
            Log.d(TAG, "Guidance haptic feedback provided")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provide guidance haptic feedback", e)
        }
    }

    /**
     * Provide haptic feedback for encouragement
     * Phase 8: Positive reinforcement for tracing efforts
     */
    fun provideEncouragementFeedback() {
        if (!_isHapticEnabled.value) return

        if (isWindowsEmulator) {
            simulateHapticFeedback("Encouragement vibration", ENCOURAGEMENT_VIBRATION_MS)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a gentle double-tap pattern
                val pattern = longArrayOf(0, ENCOURAGEMENT_VIBRATION_MS, 50, ENCOURAGEMENT_VIBRATION_MS)
                val amplitudes = intArrayOf(0, LIGHT_INTENSITY, 0, LIGHT_INTENSITY)
                
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, ENCOURAGEMENT_VIBRATION_MS, 50, ENCOURAGEMENT_VIBRATION_MS)
                vibrator?.vibrate(pattern, -1)
            }
            
            Log.d(TAG, "Encouragement haptic feedback provided")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provide encouragement haptic feedback", e)
        }
    }

    /**
     * Provide haptic feedback for milestone achievements
     * Phase 8: Celebration feedback for major accomplishments
     */
    fun provideMilestoneFeedback() {
        if (!_isHapticEnabled.value) return

        if (isWindowsEmulator) {
            simulateHapticFeedback("Milestone celebration", MILESTONE_VIBRATION_MS)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a celebration pattern
                val pattern = longArrayOf(0, 100, 50, 100, 50, 200)
                val amplitudes = intArrayOf(0, MEDIUM_INTENSITY, 0, MEDIUM_INTENSITY, 0, STRONG_INTENSITY)
                
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 100, 50, 100, 50, 200)
                vibrator?.vibrate(pattern, -1)
            }
            
            Log.d(TAG, "Milestone haptic feedback provided")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provide milestone haptic feedback", e)
        }
    }

    /**
     * Provide haptic feedback for errors or corrections needed
     * Phase 8: Gentle correction feedback
     */
    fun provideErrorFeedback() {
        if (!_isHapticEnabled.value) return

        if (isWindowsEmulator) {
            simulateHapticFeedback("Error correction", ERROR_VIBRATION_MS)
            return
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create a gentle correction pattern
                val pattern = longArrayOf(0, 50, 30, 50, 30, 50)
                val amplitudes = intArrayOf(0, LIGHT_INTENSITY, 0, LIGHT_INTENSITY, 0, LIGHT_INTENSITY)
                
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 50, 30, 50, 30, 50)
                vibrator?.vibrate(pattern, -1)
            }
            
            Log.d(TAG, "Error correction haptic feedback provided")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provide error haptic feedback", e)
        }
    }

    /**
     * Provide custom haptic feedback pattern
     */
    fun provideCustomFeedback(
        duration: Long,
        intensity: Int = _hapticIntensity.value
    ) {
        if (!_isHapticEnabled.value) return

        if (isWindowsEmulator) {
            simulateHapticFeedback("Custom vibration", duration)
            return
        }

        try {
            val clampedIntensity = intensity.coerceIn(1, 255)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(duration, clampedIntensity)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
            
            Log.d(TAG, "Custom haptic feedback provided: ${duration}ms, intensity: $clampedIntensity")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to provide custom haptic feedback", e)
        }
    }

    /**
     * Enable or disable haptic feedback
     */
    fun setHapticEnabled(enabled: Boolean) {
        _isHapticEnabled.value = enabled
        Log.d(TAG, "Haptic feedback ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Set haptic feedback intensity
     */
    fun setHapticIntensity(intensity: Int) {
        val clampedIntensity = intensity.coerceIn(1, 255)
        _hapticIntensity.value = clampedIntensity
        Log.d(TAG, "Haptic intensity set to: $clampedIntensity")
    }

    /**
     * Test haptic feedback functionality
     */
    fun testHapticFeedback() {
        Log.d(TAG, "Testing haptic feedback...")
        provideSuccessFeedback()
    }

    /**
     * Check if device supports haptic feedback
     */
    fun isHapticSupported(): Boolean {
        return if (isWindowsEmulator) {
            true // Simulated support for Windows emulator
        } else {
            vibrator?.hasVibrator() ?: false
        }
    }

    /**
     * Simulate haptic feedback for Windows emulator
     */
    private fun simulateHapticFeedback(type: String, duration: Long) {
        if (WindowsDeviceUtils.isWindowsDebugMode()) {
            Log.d(TAG, "Windows Emulator: Simulating $type (${duration}ms)")
        }
        
        // In a real implementation, this could trigger visual feedback
        // or other simulation methods for Windows development
    }

    /**
     * Stop all haptic feedback
     */
    fun stopHapticFeedback() {
        try {
            vibrator?.cancel()
            Log.d(TAG, "All haptic feedback stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop haptic feedback", e)
        }
    }

    /**
     * Get haptic feedback recommendations for different scenarios
     */
    fun getHapticRecommendations(): HapticRecommendations {
        return HapticRecommendations(
            successPattern = HapticPattern("Success", SUCCESS_VIBRATION_MS, MEDIUM_INTENSITY),
            guidancePattern = HapticPattern("Guidance", GUIDANCE_VIBRATION_MS, LIGHT_INTENSITY),
            encouragementPattern = HapticPattern("Encouragement", ENCOURAGEMENT_VIBRATION_MS, LIGHT_INTENSITY),
            milestonePattern = HapticPattern("Milestone", MILESTONE_VIBRATION_MS, STRONG_INTENSITY),
            errorPattern = HapticPattern("Error", ERROR_VIBRATION_MS, LIGHT_INTENSITY)
        )
    }
}

/**
 * Haptic pattern configuration
 */
data class HapticPattern(
    val name: String,
    val duration: Long,
    val intensity: Int
)

/**
 * Haptic feedback recommendations for different scenarios
 */
data class HapticRecommendations(
    val successPattern: HapticPattern,
    val guidancePattern: HapticPattern,
    val encouragementPattern: HapticPattern,
    val milestonePattern: HapticPattern,
    val errorPattern: HapticPattern
)
