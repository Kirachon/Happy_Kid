package com.happykid.toddlerabc

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.happykid.toddlerabc.navigation.HappyKidNavGraph
import com.happykid.toddlerabc.ui.theme.DynamicHappyKidTheme
import com.happykid.toddlerabc.util.WindowsDeviceUtils
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Happy_Kid Toddler ABC Learning App
 *
 * Optimized for Windows development environment with specific
 * configurations for Windows emulator testing and debugging.
 * Phase 2: Enhanced with Hilt dependency injection
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen with Windows optimizations
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Windows development logging
        logWindowsDeviceInfo()

        setContent {
            DynamicHappyKidTheme {
                // Phase 5: Use dynamic theme with font-aware typography
                HappyKidApp()
            }
        }

        // Configure splash screen for Windows emulator
        configureSplashScreenForWindows(splashScreen)
    }

    /**
     * Log device information for Windows development debugging
     */
    private fun logWindowsDeviceInfo() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "=== MainActivity Started ===")
            Log.d(TAG, "Windows Emulator: ${WindowsDeviceUtils.isWindowsEmulator()}")
            Log.d(TAG, "Screen Density: ${resources.displayMetrics.density}")
            Log.d(TAG, "Screen Size: ${resources.displayMetrics.widthPixels}x${resources.displayMetrics.heightPixels}")
            Log.d(TAG, "============================")
        }
    }

    /**
     * Configure splash screen with Windows emulator optimizations
     */
    private fun configureSplashScreenForWindows(splashScreen: androidx.core.splashscreen.SplashScreen) {
        if (WindowsDeviceUtils.isWindowsEmulator()) {
            // Optimize splash screen duration for Windows emulator
            splashScreen.setKeepOnScreenCondition { false }
        }
    }

    override fun onResume() {
        super.onResume()

        if (BuildConfig.DEBUG && WindowsDeviceUtils.isWindowsEmulator()) {
            Log.d(TAG, "MainActivity resumed on Windows emulator")
        }
    }

    override fun onPause() {
        super.onPause()

        if (BuildConfig.DEBUG && WindowsDeviceUtils.isWindowsEmulator()) {
            Log.d(TAG, "MainActivity paused on Windows emulator")
        }
    }
}

/**
 * Main Composable for Happy_Kid App with Windows optimizations and Navigation Component
 * Phase 3: Enhanced with Navigation Component integration
 */
@Composable
fun HappyKidApp() {
    val navController = rememberNavController()
    val configuration = LocalConfiguration.current

    // Windows emulator detection and optimization
    LaunchedEffect(Unit) {
        if (WindowsDeviceUtils.isWindowsEmulator()) {
            Log.d("HappyKidApp", "Compose UI initialized on Windows emulator")
            Log.d("HappyKidApp", "Screen configuration: ${configuration.screenWidthDp}x${configuration.screenHeightDp} dp")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // Phase 3: Navigation Component integration
        HappyKidNavGraph(navController = navController)
    }
}

