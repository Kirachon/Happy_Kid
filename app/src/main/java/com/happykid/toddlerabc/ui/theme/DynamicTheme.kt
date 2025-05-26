package com.happykid.toddlerabc.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.happykid.toddlerabc.viewmodel.FontViewModel
import com.happykid.toddlerabc.model.FontPreference
import com.happykid.toddlerabc.util.WindowsDeviceUtils

/**
 * Dynamic Happy_Kid theme with font-aware typography
 * Phase 5: Enhanced with dynamic font family and size support
 *
 * This theme provider responds to font preference changes and applies
 * the selected typography throughout the app while maintaining all
 * existing Windows optimizations and toddler-friendly design.
 */
@Composable
fun DynamicHappyKidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent toddler experience
    fontViewModel: FontViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    // Collect font preferences
    val fontPreference by fontViewModel.currentFontPreference.collectAsState(initial = FontPreference())
    val currentFontFamily by fontViewModel.currentFontFamily.collectAsState(initial = FontFamily.Default)

    // Initialize font manager
    LaunchedEffect(Unit) {
        fontViewModel.initialize()
    }

    // Create dynamic typography based on current font preferences
    val dynamicTypography = remember(fontPreference, currentFontFamily) {
        createToddlerTypography(
            fontFamily = currentFontFamily,
            fontScale = fontPreference.fontSize
        )
    }

    // Color scheme logic (unchanged from original theme)
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

    // Window configuration (unchanged from original theme)
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
        typography = dynamicTypography, // Use dynamic typography instead of static
        content = content
    )
}

/**
 * Convenience composable for screens that need specific font overrides
 * Phase 5: Allows temporary font family or scale overrides
 */
@Composable
fun HappyKidThemeWithFontOverride(
    fontFamily: FontFamily? = null,
    fontScale: Float? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontViewModel: FontViewModel = hiltViewModel(),
    content: @Composable () -> Unit
) {
    // Get current preferences
    val fontPreference by fontViewModel.currentFontPreference.collectAsState(initial = FontPreference())
    val currentFontFamily by fontViewModel.currentFontFamily.collectAsState(initial = FontFamily.Default)

    // Use overrides if provided, otherwise use current preferences
    val effectiveFontFamily = fontFamily ?: currentFontFamily
    val effectiveFontScale = fontScale ?: fontPreference.fontSize

    // Create typography with overrides
    val overrideTypography = remember(effectiveFontFamily, effectiveFontScale) {
        createToddlerTypography(
            fontFamily = effectiveFontFamily,
            fontScale = effectiveFontScale
        )
    }

    // Color scheme (same as main theme)
    val colorScheme = if (!darkTheme) ToddlerLightColorScheme else DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = overrideTypography,
        content = content
    )
}

/**
 * Preview theme for font selection components
 * Phase 5: Provides isolated theme for font previews
 */
@Composable
fun FontPreviewTheme(
    fontFamily: FontFamily,
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val previewTypography = remember(fontFamily, fontScale) {
        createToddlerTypography(
            fontFamily = fontFamily,
            fontScale = fontScale
        )
    }

    MaterialTheme(
        colorScheme = ToddlerLightColorScheme,
        typography = previewTypography,
        content = content
    )
}

// Color schemes (moved from Theme.kt for consistency)
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
