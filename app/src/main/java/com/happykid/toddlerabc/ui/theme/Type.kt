package com.happykid.toddlerabc.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.happykid.toddlerabc.R

/**
 * Typography for Happy_Kid Toddler ABC Learning App
 * Phase 5: Enhanced with dynamic font family support
 *
 * Typography is specifically designed for:
 * - Toddler readability and engagement
 * - Large, clear fonts for developing vision
 * - Windows emulator display optimization
 * - Accessibility compliance
 * - Dynamic font family selection
 */

// Custom font family for toddlers (using system fonts for initial build)
val ToddlerFontFamily = FontFamily.Default

// Fallback to system fonts if custom fonts are not available
val SystemToddlerFontFamily = FontFamily.Default

/**
 * Create typography with dynamic font family and scale
 * Phase 5: Supports runtime font family changes
 */
fun createToddlerTypography(
    fontFamily: FontFamily = ToddlerFontFamily,
    fontScale: Float = 1.0f
): Typography {
    return Typography(
        // Display styles - for large headings and titles
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (48 * fontScale).sp,
            lineHeight = (56 * fontScale).sp,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (40 * fontScale).sp,
            lineHeight = (48 * fontScale).sp,
            letterSpacing = 0.sp,
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * fontScale).sp,
            lineHeight = (40 * fontScale).sp,
            letterSpacing = 0.sp,
        ),
        // Headline styles - for section headers
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * fontScale).sp,
            lineHeight = (36 * fontScale).sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * fontScale).sp,
            lineHeight = (32 * fontScale).sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (20 * fontScale).sp,
            lineHeight = (28 * fontScale).sp,
            letterSpacing = 0.sp,
        ),
        // Title styles - for card headers and important text
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = (18 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * fontScale).sp,
            lineHeight = (22 * fontScale).sp,
            letterSpacing = 0.15.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = 0.1.sp,
        ),
        // Body styles - for regular content
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = 0.4.sp,
        ),
        // Label styles - for buttons and small text
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = (10 * fontScale).sp,
            lineHeight = (14 * fontScale).sp,
            letterSpacing = 0.5.sp,
        )
    )
}

// Toddler-optimized typography scale (default implementation)
val ToddlerTypography = createToddlerTypography()

// Age-specific typography variations using dynamic function
val ToddlerAge1Typography = createToddlerTypography(fontScale = 1.15f) // Larger for 1-2 year olds
val ToddlerAge3Typography = createToddlerTypography(fontScale = 1.0f)  // Standard for 3-4 year olds

// Windows emulator optimized typography using dynamic function
val WindowsEmulatorTypography = createToddlerTypography(
    fontFamily = SystemToddlerFontFamily,
    fontScale = 1.1f
)

// Accessibility typography for high contrast mode using dynamic function
val AccessibilityTypography = createToddlerTypography(
    fontFamily = FontFamily.Default,
    fontScale = 1.25f
)
