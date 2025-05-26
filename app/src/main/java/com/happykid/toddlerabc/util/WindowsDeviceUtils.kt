package com.happykid.toddlerabc.util

import android.os.Build
import android.util.Log

/**
 * Utility class for Windows development environment detection and optimization
 *
 * This class provides utilities specifically designed for Windows-based Android
 * development, including emulator detection and Windows-specific optimizations.
 */
object WindowsDeviceUtils {

    private const val TAG = "WindowsDeviceUtils"

    // Windows emulator detection patterns
    private val WINDOWS_EMULATOR_INDICATORS = listOf(
        "sdk_gphone",
        "Emulator",
        "goldfish",
        "ranchu",
        "vbox86",
        "generic"
    )

    /**
     * Check if the app is running on a Windows Android emulator
     */
    fun isWindowsEmulator(): Boolean {
        val isEmulator = WINDOWS_EMULATOR_INDICATORS.any { indicator ->
            Build.PRODUCT.contains(indicator, ignoreCase = true) ||
            Build.MODEL.contains(indicator, ignoreCase = true) ||
            Build.HARDWARE.contains(indicator, ignoreCase = true) ||
            Build.DEVICE.contains(indicator, ignoreCase = true)
        }

        if (isEmulator) {
            Log.d(TAG, "Windows emulator detected:")
            Log.d(TAG, "  Product: ${Build.PRODUCT}")
            Log.d(TAG, "  Model: ${Build.MODEL}")
            Log.d(TAG, "  Hardware: ${Build.HARDWARE}")
            Log.d(TAG, "  Device: ${Build.DEVICE}")
        }

        return isEmulator
    }

    /**
     * Check if running in Windows development debug mode
     */
    fun isWindowsDebugMode(): Boolean {
        return true // Simplified for initial build
    }

    /**
     * Get Windows-optimized performance settings
     */
    fun getWindowsPerformanceSettings(): WindowsPerformanceSettings {
        return if (isWindowsEmulator()) {
            WindowsPerformanceSettings(
                enableHighPerformanceMode = false,
                reduceAnimations = true,
                optimizeForEmulator = true,
                enableDebugLogging = isWindowsDebugMode()
            )
        } else {
            WindowsPerformanceSettings(
                enableHighPerformanceMode = true,
                reduceAnimations = false,
                optimizeForEmulator = false,
                enableDebugLogging = isWindowsDebugMode()
            )
        }
    }

    /**
     * Get Windows-specific file path handling
     */
    fun normalizeWindowsPath(path: String): String {
        return if (isWindowsHost()) {
            path.replace("/", "\\")
        } else {
            path.replace("\\", "/")
        }
    }

    /**
     * Check if the development host is Windows
     */
    private fun isWindowsHost(): Boolean {
        return System.getProperty("os.name")?.lowercase()?.contains("windows") == true
    }

    /**
     * Get Windows-specific memory recommendations
     */
    fun getWindowsMemoryRecommendations(): WindowsMemorySettings {
        val runtime = Runtime.getRuntime()
        val maxMemory = runtime.maxMemory()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()

        return WindowsMemorySettings(
            maxMemoryMB = (maxMemory / 1024 / 1024).toInt(),
            totalMemoryMB = (totalMemory / 1024 / 1024).toInt(),
            freeMemoryMB = (freeMemory / 1024 / 1024).toInt(),
            isLowMemoryDevice = maxMemory < 512 * 1024 * 1024, // Less than 512MB
            recommendedCacheSize = if (isWindowsEmulator()) 32 else 64 // MB
        )
    }

    /**
     * Log Windows development environment information
     */
    fun logWindowsEnvironmentInfo() {
        if (isWindowsDebugMode()) {
            Log.i(TAG, "=== Windows Development Environment ===")
            Log.i(TAG, "Windows Emulator: ${isWindowsEmulator()}")
            Log.i(TAG, "Windows Debug Mode: ${isWindowsDebugMode()}")
            Log.i(TAG, "Host OS: ${System.getProperty("os.name")}")
            Log.i(TAG, "Java Version: ${System.getProperty("java.version")}")

            val memorySettings = getWindowsMemoryRecommendations()
            Log.i(TAG, "Memory - Max: ${memorySettings.maxMemoryMB}MB, Free: ${memorySettings.freeMemoryMB}MB")
            Log.i(TAG, "Low Memory Device: ${memorySettings.isLowMemoryDevice}")
            Log.i(TAG, "======================================")
        }
    }
}

/**
 * Data class for Windows performance settings
 */
data class WindowsPerformanceSettings(
    val enableHighPerformanceMode: Boolean,
    val reduceAnimations: Boolean,
    val optimizeForEmulator: Boolean,
    val enableDebugLogging: Boolean
)

/**
 * Data class for Windows memory settings
 */
data class WindowsMemorySettings(
    val maxMemoryMB: Int,
    val totalMemoryMB: Int,
    val freeMemoryMB: Int,
    val isLowMemoryDevice: Boolean,
    val recommendedCacheSize: Int
)

/**
 * Data class for Windows touch performance metrics
 * Phase 6: Enhanced input optimization tracking
 */
data class WindowsTouchMetrics(
    val averageLatencyMs: Float,
    val maxLatencyMs: Float,
    val touchEventCount: Int,
    val missedTouchEvents: Int,
    val touchAccuracy: Float
)

/**
 * Data class for Windows build performance metrics
 * Phase 6: Build system optimization tracking
 */
data class WindowsBuildMetrics(
    val buildTimeMs: Long,
    val incrementalBuildTimeMs: Long,
    val compilationTimeMs: Long,
    val resourceProcessingTimeMs: Long,
    val dexingTimeMs: Long
)

/**
 * Data class for comprehensive Windows performance profile
 * Phase 6: Complete performance monitoring
 */
data class WindowsPerformanceProfile(
    val timestamp: Long,
    val memorySettings: WindowsMemorySettings,
    val touchMetrics: WindowsTouchMetrics?,
    val buildMetrics: WindowsBuildMetrics?,
    val frameRate: Float,
    val cpuUsagePercent: Float,
    val isEmulatorOptimized: Boolean
)
