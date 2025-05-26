package com.happykid.toddlerabc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.happykid.toddlerabc.util.WindowsDeviceUtils

/**
 * Happy_Kid theme with Windows development optimizations
 * 
 * Toddler-friendly color scheme optimized for:
 * - Young children's visual preferences
 * - Windows emulator display
 * - Accessibility requirements
 */

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    
    // Toddler-friendly background colors
    background = ToddlerBackground,
    surface = ToddlerSurface,
    
    // High contrast for better visibility
    onPrimary = ToddlerOnPrimary,
    onSecondary = ToddlerOnSecondary,
    onTertiary = ToddlerOnTertiary,
    onBackground = ToddlerOnBackground,
    onSurface = ToddlerOnSurface,
)

/**
 * Toddler-optimized color scheme for better engagement and accessibility
 */
private val ToddlerLightColorScheme = lightColorScheme(
    primary = ToddlerPrimary,
    secondary = ToddlerSecondary,
    tertiary = ToddlerTertiary,
    background = ToddlerBackground,
    surface = ToddlerSurface,
    onPrimary = ToddlerOnPrimary,
    onSecondary = ToddlerOnSecondary,
    onTertiary = ToddlerOnTertiary,
    onBackground = ToddlerOnBackground,
    onSurface = ToddlerOnSurface,
)

@Composable
fun HappyKidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled for consistent toddler experience
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Disable dynamic colors for consistent toddler experience
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        
        // Use toddler-optimized colors for light theme (primary use case)
        !darkTheme -> ToddlerLightColorScheme
        
        // Dark theme (limited use for toddlers)
        else -> DarkColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            // Windows emulator optimization
            if (WindowsDeviceUtils.isWindowsEmulator()) {
                // Optimize status bar for Windows emulator
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            } else {
                // Standard Android device configuration
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ToddlerTypography,
        content = content
    )
}
