package com.happykid.toddlerabc.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.os.SystemClock
import android.util.Log
import android.view.Choreographer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Windows Performance Profiler for Happy_Kid App
 * Phase 6: Comprehensive performance monitoring and optimization for Windows emulator environment
 *
 * This profiler provides real-time performance metrics specifically optimized for Windows
 * Android emulator development, including frame rate monitoring, memory tracking,
 * touch latency measurement, and build performance analysis.
 */
@Singleton
class WindowsPerformanceProfiler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "WindowsPerformanceProfiler"
        private const val MONITORING_INTERVAL_MS = 1000L
        private const val FRAME_RATE_SAMPLE_SIZE = 60
        private const val TOUCH_LATENCY_SAMPLE_SIZE = 20
    }

    private val profilerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    // Performance monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _currentProfile = MutableStateFlow<WindowsPerformanceProfile?>(null)
    val currentProfile: StateFlow<WindowsPerformanceProfile?> = _currentProfile.asStateFlow()

    // Frame rate monitoring
    private val frameRateSamples = mutableListOf<Long>()
    private var lastFrameTime = 0L
    private var frameCount = 0

    // Touch latency monitoring
    private val touchLatencySamples = mutableListOf<Float>()
    private var touchEventCount = 0
    private var missedTouchEvents = 0

    // CPU monitoring
    private var lastCpuTime = 0L
    private var lastAppCpuTime = 0L

    /**
     * Start comprehensive performance monitoring
     * Phase 6: Enhanced monitoring for Windows emulator optimization
     */
    fun startMonitoring() {
        if (_isMonitoring.value) {
            Log.w(TAG, "Performance monitoring already active")
            return
        }

        Log.i(TAG, "Starting Windows performance monitoring")
        _isMonitoring.value = true

        // Initialize monitoring components
        initializeFrameRateMonitoring()
        initializeCpuMonitoring()

        // Start periodic performance collection
        profilerScope.launch {
            while (_isMonitoring.value) {
                collectPerformanceMetrics()
                delay(MONITORING_INTERVAL_MS)
            }
        }
    }

    /**
     * Stop performance monitoring
     */
    fun stopMonitoring() {
        Log.i(TAG, "Stopping Windows performance monitoring")
        _isMonitoring.value = false
        clearSamples()
    }

    /**
     * Initialize frame rate monitoring using Choreographer
     * Phase 6: Windows emulator-specific frame rate optimization
     */
    private fun initializeFrameRateMonitoring() {
        val frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (_isMonitoring.value) {
                    recordFrameTime(frameTimeNanos)
                    Choreographer.getInstance().postFrameCallback(this)
                }
            }
        }
        
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    /**
     * Record frame time for frame rate calculation
     */
    private fun recordFrameTime(frameTimeNanos: Long) {
        val currentTime = frameTimeNanos / 1_000_000 // Convert to milliseconds
        
        if (lastFrameTime > 0) {
            val frameDuration = currentTime - lastFrameTime
            
            synchronized(frameRateSamples) {
                frameRateSamples.add(frameDuration)
                if (frameRateSamples.size > FRAME_RATE_SAMPLE_SIZE) {
                    frameRateSamples.removeAt(0)
                }
            }
        }
        
        lastFrameTime = currentTime
        frameCount++
    }

    /**
     * Initialize CPU monitoring for Windows emulator
     */
    private fun initializeCpuMonitoring() {
        try {
            val statFile = File("/proc/stat")
            if (statFile.exists()) {
                lastCpuTime = getTotalCpuTime()
                lastAppCpuTime = getAppCpuTime()
            }
        } catch (e: Exception) {
            Log.w(TAG, "CPU monitoring initialization failed: ${e.message}")
        }
    }

    /**
     * Record touch latency for input optimization
     * Phase 6: Touch responsiveness measurement
     */
    fun recordTouchLatency(latencyMs: Float) {
        synchronized(touchLatencySamples) {
            touchLatencySamples.add(latencyMs)
            if (touchLatencySamples.size > TOUCH_LATENCY_SAMPLE_SIZE) {
                touchLatencySamples.removeAt(0)
            }
        }
        touchEventCount++
    }

    /**
     * Record missed touch event for accuracy tracking
     */
    fun recordMissedTouchEvent() {
        missedTouchEvents++
    }

    /**
     * Collect comprehensive performance metrics
     * Phase 6: Windows emulator-specific performance profiling
     */
    private suspend fun collectPerformanceMetrics() {
        try {
            val memorySettings = WindowsDeviceUtils.getWindowsMemoryRecommendations()
            val frameRate = calculateCurrentFrameRate()
            val cpuUsage = calculateCpuUsage()
            val touchMetrics = calculateTouchMetrics()

            val profile = WindowsPerformanceProfile(
                timestamp = System.currentTimeMillis(),
                memorySettings = memorySettings,
                touchMetrics = touchMetrics,
                buildMetrics = null, // Build metrics collected separately
                frameRate = frameRate,
                cpuUsagePercent = cpuUsage,
                isEmulatorOptimized = WindowsDeviceUtils.isWindowsEmulator()
            )

            _currentProfile.value = profile

            // Log performance summary for Windows development
            if (WindowsDeviceUtils.isWindowsDebugMode()) {
                logPerformanceSummary(profile)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to collect performance metrics", e)
        }
    }

    /**
     * Calculate current frame rate from samples
     */
    private fun calculateCurrentFrameRate(): Float {
        synchronized(frameRateSamples) {
            if (frameRateSamples.isEmpty()) return 0f
            
            val averageFrameDuration = frameRateSamples.average()
            return if (averageFrameDuration > 0) {
                (1000f / averageFrameDuration).toFloat()
            } else 0f
        }
    }

    /**
     * Calculate current CPU usage percentage
     */
    private fun calculateCpuUsage(): Float {
        return try {
            val currentCpuTime = getTotalCpuTime()
            val currentAppCpuTime = getAppCpuTime()

            if (lastCpuTime > 0 && lastAppCpuTime > 0) {
                val cpuDelta = currentCpuTime - lastCpuTime
                val appCpuDelta = currentAppCpuTime - lastAppCpuTime

                lastCpuTime = currentCpuTime
                lastAppCpuTime = currentAppCpuTime

                if (cpuDelta > 0) {
                    (appCpuDelta.toFloat() / cpuDelta.toFloat()) * 100f
                } else 0f
            } else {
                lastCpuTime = currentCpuTime
                lastAppCpuTime = currentAppCpuTime
                0f
            }
        } catch (e: Exception) {
            Log.w(TAG, "CPU usage calculation failed: ${e.message}")
            0f
        }
    }

    /**
     * Calculate touch performance metrics
     */
    private fun calculateTouchMetrics(): WindowsTouchMetrics? {
        synchronized(touchLatencySamples) {
            if (touchLatencySamples.isEmpty()) return null

            val averageLatency = touchLatencySamples.average().toFloat()
            val maxLatency = touchLatencySamples.maxOrNull() ?: 0f
            val accuracy = if (touchEventCount > 0) {
                ((touchEventCount - missedTouchEvents).toFloat() / touchEventCount.toFloat()) * 100f
            } else 100f

            return WindowsTouchMetrics(
                averageLatencyMs = averageLatency,
                maxLatencyMs = maxLatency,
                touchEventCount = touchEventCount,
                missedTouchEvents = missedTouchEvents,
                touchAccuracy = accuracy
            )
        }
    }

    /**
     * Get total CPU time from /proc/stat
     */
    private fun getTotalCpuTime(): Long {
        return try {
            RandomAccessFile("/proc/stat", "r").use { reader ->
                val load = reader.readLine()
                val cpuTimes = load.split(" ").drop(2).take(4).map { it.toLong() }
                cpuTimes.sum()
            }
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get app-specific CPU time
     */
    private fun getAppCpuTime(): Long {
        return try {
            Debug.threadCpuTimeNanos() / 1_000_000 // Convert to milliseconds
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Clear all performance samples
     */
    private fun clearSamples() {
        synchronized(frameRateSamples) {
            frameRateSamples.clear()
        }
        synchronized(touchLatencySamples) {
            touchLatencySamples.clear()
        }
        frameCount = 0
        touchEventCount = 0
        missedTouchEvents = 0
    }

    /**
     * Log performance summary for Windows development debugging
     */
    private fun logPerformanceSummary(profile: WindowsPerformanceProfile) {
        Log.d(TAG, "=== Windows Performance Summary ===")
        Log.d(TAG, "Frame Rate: ${String.format("%.1f", profile.frameRate)} FPS")
        Log.d(TAG, "CPU Usage: ${String.format("%.1f", profile.cpuUsagePercent)}%")
        Log.d(TAG, "Memory: ${profile.memorySettings.freeMemoryMB}MB free / ${profile.memorySettings.totalMemoryMB}MB total")
        
        profile.touchMetrics?.let { touch ->
            Log.d(TAG, "Touch Latency: ${String.format("%.1f", touch.averageLatencyMs)}ms avg")
            Log.d(TAG, "Touch Accuracy: ${String.format("%.1f", touch.touchAccuracy)}%")
        }
        
        Log.d(TAG, "Emulator Optimized: ${profile.isEmulatorOptimized}")
        Log.d(TAG, "===================================")
    }
}
