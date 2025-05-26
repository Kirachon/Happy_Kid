package com.happykid.toddlerabc

import android.app.Application
import android.os.Build
import android.util.Log
import com.happykid.toddlerabc.repository.LetterRepository
import com.happykid.toddlerabc.manager.FontManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Happy_Kid Application class with Windows development optimizations
 *
 * This application class is optimized for Windows-based Android development
 * and includes specific configurations for Windows emulator testing.
 * Phase 5: Enhanced with font management initialization
 */
@HiltAndroidApp
class HappyKidApplication : Application() {

    // Phase 2: Repository will be injected via Hilt instead of manual instantiation
    @Inject
    lateinit var repository: LetterRepository

    // Phase 5: Font manager for custom fonts
    @Inject
    lateinit var fontManager: FontManager

    // Phase 6: Windows performance profiler for emulator optimization
    // Temporarily disabled for Phase 9 build
    // @Inject
    // lateinit var performanceProfiler: WindowsPerformanceProfiler

    // Phase 6: Windows touch optimizer for enhanced input handling
    // Temporarily disabled for Phase 9 build
    // @Inject
    // lateinit var touchOptimizer: WindowsTouchOptimizer

    // Application scope for initialization tasks
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "HappyKidApp"

        // Windows development flags
        const val WINDOWS_EMULATOR_DETECTED = "sdk_gphone"
        const val WINDOWS_DEBUG_MODE = BuildConfig.WINDOWS_DEBUG
    }

    override fun onCreate() {
        super.onCreate()

        initializeWindowsOptimizations()
        initializeDatabase()
        initializeFontManager()
        // Temporarily disabled for Phase 9 build
        // initializePerformanceMonitoring()
        // initializeTouchOptimization()
        logApplicationInfo()
        setupCrashReporting()
    }

    /**
     * Initialize database with default data
     */
    private fun initializeDatabase() {
        applicationScope.launch {
            try {
                repository.initializeIfEmpty()
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Database initialized successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize database", e)
            }
        }
    }

    /**
     * Initialize font manager with default preferences
     * Phase 5: Ensures font preferences are available at app startup
     */
    private fun initializeFontManager() {
        applicationScope.launch {
            try {
                fontManager.initialize()
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Font manager initialized successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize font manager", e)
            }
        }
    }

    /**
     * Initialize performance monitoring
     * Phase 6: Windows emulator performance optimization
     * Temporarily disabled for Phase 9 build
     */
    /*
    private fun initializePerformanceMonitoring() {
        if (isWindowsEmulator() && BuildConfig.DEBUG) {
            applicationScope.launch {
                try {
                    performanceProfiler.startMonitoring()
                    Log.d(TAG, "Performance monitoring started for Windows emulator")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to start performance monitoring", e)
                }
            }
        }
    }
    */

    /**
     * Initialize touch optimization
     * Phase 6: Windows emulator touch responsiveness enhancement
     * Temporarily disabled for Phase 9 build
     */
    /*
    private fun initializeTouchOptimization() {
        if (isWindowsEmulator()) {
            try {
                touchOptimizer.logTouchPerformanceSummary()
                Log.d(TAG, "Touch optimization initialized for Windows emulator")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize touch optimization", e)
            }
        }
    }
    */

    /**
     * Initialize Windows-specific optimizations for development
     */
    private fun initializeWindowsOptimizations() {
        if (isWindowsEmulator()) {
            Log.d(TAG, "Windows emulator detected - applying optimizations")

            // Windows emulator specific optimizations
            System.setProperty("java.net.useSystemProxies", "true")

            // Optimize for Windows development environment
            if (BuildConfig.DEBUG) {
                // Enable strict mode for Windows development
                enableStrictModeForWindows()
            }
        }
    }

    /**
     * Check if running on Windows Android emulator
     */
    private fun isWindowsEmulator(): Boolean {
        return Build.PRODUCT.contains(WINDOWS_EMULATOR_DETECTED) ||
                Build.MODEL.contains("Emulator") ||
                Build.HARDWARE.contains("goldfish")
    }

    /**
     * Enable strict mode optimized for Windows development
     */
    private fun enableStrictModeForWindows() {
        if (BuildConfig.DEBUG && WINDOWS_DEBUG_MODE) {
            // StrictMode configuration optimized for Windows development
            android.os.StrictMode.setThreadPolicy(
                android.os.StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )

            android.os.StrictMode.setVmPolicy(
                android.os.StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }
    }

    /**
     * Log application information for Windows development debugging
     */
    private fun logApplicationInfo() {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "=== Happy_Kid Application Started ===")
            Log.i(TAG, "Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            Log.i(TAG, "Build Type: ${BuildConfig.BUILD_TYPE}")
            Log.i(TAG, "Windows Debug Mode: $WINDOWS_DEBUG_MODE")
            Log.i(TAG, "Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            Log.i(TAG, "Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            Log.i(TAG, "Windows Emulator: ${isWindowsEmulator()}")
            Log.i(TAG, "=====================================")
        }
    }

    /**
     * Setup crash reporting with Windows development considerations
     */
    private fun setupCrashReporting() {
        // Firebase Crashlytics will be initialized automatically
        // Additional Windows-specific crash handling can be added here

        if (BuildConfig.DEBUG && isWindowsEmulator()) {
            Log.d(TAG, "Crash reporting configured for Windows development")
        }
    }

    /**
     * Handle Windows-specific memory management
     */
    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        when (level) {
            TRIM_MEMORY_RUNNING_MODERATE,
            TRIM_MEMORY_RUNNING_LOW,
            TRIM_MEMORY_RUNNING_CRITICAL -> {
                if (isWindowsEmulator()) {
                    Log.d(TAG, "Windows emulator memory trim level: $level")
                    // Perform Windows emulator specific memory cleanup
                }
            }
        }
    }

    /**
     * Handle Windows development lifecycle
     */
    override fun onLowMemory() {
        super.onLowMemory()

        if (isWindowsEmulator() && BuildConfig.DEBUG) {
            Log.w(TAG, "Low memory warning on Windows emulator")
            // Implement Windows emulator specific low memory handling
        }
    }
}
