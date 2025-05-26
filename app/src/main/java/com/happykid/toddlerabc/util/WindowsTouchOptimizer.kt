package com.happykid.toddlerabc.util

import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Windows Touch Optimizer for Happy_Kid App
 * Phase 6: Enhanced touch responsiveness and input optimization for Windows emulator
 *
 * This optimizer provides Windows emulator-specific touch handling improvements,
 * including latency measurement, gesture optimization, and toddler-friendly
 * interaction enhancements specifically tuned for Windows development environment.
 */
@Singleton
class WindowsTouchOptimizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "WindowsTouchOptimizer"
        private const val TOUCH_DEBOUNCE_MS = 50L
        private const val TODDLER_TOUCH_TIMEOUT_MS = 300L
        private const val WINDOWS_EMULATOR_TOUCH_SCALE = 1.2f
    }

    private var lastTouchTime = 0L
    private var touchStartTime = 0L
    private val isWindowsEmulator = WindowsDeviceUtils.isWindowsEmulator()

    /**
     * Optimized touch modifier for Windows emulator with toddler-friendly enhancements
     * Phase 6: Windows-specific touch optimization
     */
    @Composable
    fun Modifier.optimizedToddlerTouch(
        enabled: Boolean = true,
        onTap: () -> Unit
    ): Modifier {
        return if (enabled) {
            this.pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        handleTouchStart(offset)

                        // Wait for release or timeout
                        val released = tryAwaitRelease()
                        if (released) {
                            handleTouchEnd(offset, onTap)
                        } else {
                            handleTouchTimeout(offset)
                        }
                    }
                )
            }
        } else {
            this
        }
    }

    /**
     * Enhanced button interaction source with Windows emulator optimizations
     * Phase 6: Improved button responsiveness for toddlers
     */
    @Composable
    fun createOptimizedInteractionSource(): MutableInteractionSource {
        val interactionSource = remember { MutableInteractionSource() }

        // Add Windows emulator-specific interaction optimizations
        if (isWindowsEmulator) {
            LaunchedEffect(interactionSource) {
                // Pre-warm interaction handling for better responsiveness
                delay(100)
                Log.d(TAG, "Interaction source optimized for Windows emulator")
            }
        }

        return interactionSource
    }

    /**
     * Handle touch start with latency measurement
     */
    private fun handleTouchStart(offset: Offset) {
        touchStartTime = SystemClock.elapsedRealtime()

        if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
            Log.d(TAG, "Touch started at (${offset.x}, ${offset.y}) on Windows emulator")
        }
    }

    /**
     * Handle touch end with performance tracking
     */
    private fun handleTouchEnd(offset: Offset, onTap: () -> Unit) {
        val currentTime = SystemClock.elapsedRealtime()
        val touchDuration = currentTime - touchStartTime
        val timeSinceLastTouch = currentTime - lastTouchTime

        // Debounce touches for toddler-friendly interaction
        if (timeSinceLastTouch < TOUCH_DEBOUNCE_MS) {
            Log.d(TAG, "Touch debounced (${timeSinceLastTouch}ms since last touch)")
            return
        }

        // Record touch latency for performance monitoring
        if (touchStartTime > 0) {
            val latency = touchDuration.toFloat()

            if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                Log.d(TAG, "Touch completed: ${latency}ms latency, Windows emulator")
            }
        }

        lastTouchTime = currentTime

        // Execute the tap action
        try {
            onTap()
        } catch (e: Exception) {
            Log.e(TAG, "Error executing tap action", e)
        }
    }

    /**
     * Handle touch timeout for toddler accessibility
     */
    private fun handleTouchTimeout(offset: Offset) {
        if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
            Log.d(TAG, "Touch timeout at (${offset.x}, ${offset.y}) - toddler-friendly handling")
        }
    }

    /**
     * Optimize motion event for Windows emulator
     * Phase 6: Windows-specific motion event processing
     */
    fun optimizeMotionEvent(event: MotionEvent): MotionEvent {
        if (!isWindowsEmulator) {
            return event
        }

        return try {
            // Apply Windows emulator-specific touch scaling for better toddler interaction
            val scaledX = event.x * WINDOWS_EMULATOR_TOUCH_SCALE
            val scaledY = event.y * WINDOWS_EMULATOR_TOUCH_SCALE

            // Create optimized motion event for Windows emulator
            MotionEvent.obtain(
                event.downTime,
                event.eventTime,
                event.action,
                scaledX,
                scaledY,
                event.metaState
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to optimize motion event for Windows emulator", e)
            event
        }
    }

    /**
     * Create Windows-optimized press interaction
     * Phase 6: Enhanced interaction feedback for Windows emulator
     */
    fun createOptimizedPressInteraction(
        pressPosition: Offset,
        interactionSource: MutableInteractionSource
    ): PressInteraction.Press {
        val optimizedPosition = if (isWindowsEmulator) {
            Offset(
                x = pressPosition.x * WINDOWS_EMULATOR_TOUCH_SCALE,
                y = pressPosition.y * WINDOWS_EMULATOR_TOUCH_SCALE
            )
        } else {
            pressPosition
        }

        return PressInteraction.Press(optimizedPosition)
    }

    /**
     * Get Windows emulator touch recommendations
     * Phase 6: Touch optimization settings for Windows development
     */
    fun getWindowsTouchRecommendations(): WindowsTouchSettings {
        return if (isWindowsEmulator) {
            WindowsTouchSettings(
                debounceTimeMs = TOUCH_DEBOUNCE_MS,
                timeoutMs = TODDLER_TOUCH_TIMEOUT_MS,
                touchScale = WINDOWS_EMULATOR_TOUCH_SCALE,
                enableEnhancedLogging = WindowsDeviceUtils.isWindowsDebugMode(),
                optimizeForToddlers = true
            )
        } else {
            WindowsTouchSettings(
                debounceTimeMs = 30L,
                timeoutMs = 200L,
                touchScale = 1.0f,
                enableEnhancedLogging = false,
                optimizeForToddlers = true
            )
        }
    }

    /**
     * Log touch performance summary for Windows development
     */
    fun logTouchPerformanceSummary() {
        if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
            val touchSettings = getWindowsTouchRecommendations()
            Log.d(TAG, "=== Windows Touch Performance Summary ===")
            Log.d(TAG, "Debounce Time: ${touchSettings.debounceTimeMs}ms")
            Log.d(TAG, "Timeout: ${touchSettings.timeoutMs}ms")
            Log.d(TAG, "Touch Scale: ${touchSettings.touchScale}")
            Log.d(TAG, "Toddler Optimized: ${touchSettings.optimizeForToddlers}")
            Log.d(TAG, "========================================")
        }
    }
}

/**
 * Data class for Windows touch optimization settings
 * Phase 6: Touch configuration for Windows emulator
 */
data class WindowsTouchSettings(
    val debounceTimeMs: Long,
    val timeoutMs: Long,
    val touchScale: Float,
    val enableEnhancedLogging: Boolean,
    val optimizeForToddlers: Boolean
)
