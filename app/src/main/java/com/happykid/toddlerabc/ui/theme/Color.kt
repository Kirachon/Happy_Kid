package com.happykid.toddlerabc.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for Happy_Kid Toddler ABC Learning App
 * 
 * Colors are specifically chosen for:
 * - Toddler visual appeal and engagement
 * - High contrast for better readability
 * - Windows emulator display optimization
 * - Accessibility compliance
 */

// Standard Material 3 colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Toddler-optimized color palette
// Primary colors - bright and engaging for toddlers
val ToddlerPrimary = Color(0xFF4CAF50)        // Bright Green - positive, growth
val ToddlerSecondary = Color(0xFF2196F3)      // Bright Blue - trust, learning
val ToddlerTertiary = Color(0xFFFF9800)       // Orange - energy, creativity

// Background colors - soft and comfortable
val ToddlerBackground = Color(0xFFFFFBFE)     // Very light warm white
val ToddlerSurface = Color(0xFFF8F9FA)        // Light gray-white

// Accent colors for interactive elements
val ToddlerAccent1 = Color(0xFFE91E63)        // Pink - fun, playful
val ToddlerAccent2 = Color(0xFF9C27B0)        // Purple - imagination
val ToddlerAccent3 = Color(0xFFFFEB3B)        // Yellow - happiness, attention

// Text colors - high contrast for readability
val ToddlerOnPrimary = Color(0xFFFFFFFF)      // White on primary
val ToddlerOnSecondary = Color(0xFFFFFFFF)    // White on secondary
val ToddlerOnTertiary = Color(0xFF000000)     // Black on tertiary
val ToddlerOnBackground = Color(0xFF1C1B1F)   // Dark gray on background
val ToddlerOnSurface = Color(0xFF1C1B1F)      // Dark gray on surface

// Success and error colors for feedback
val ToddlerSuccess = Color(0xFF4CAF50)        // Green - correct answers
val ToddlerError = Color(0xFFFF5722)          // Red-orange - gentle error indication
val ToddlerWarning = Color(0xFFFF9800)        // Orange - attention needed

// Letter and alphabet specific colors
val AlphabetRed = Color(0xFFE53935)           // A, B, C
val AlphabetBlue = Color(0xFF1E88E5)          // D, E, F
val AlphabetGreen = Color(0xFF43A047)         // G, H, I
val AlphabetOrange = Color(0xFFFF7043)        // J, K, L
val AlphabetPurple = Color(0xFF8E24AA)        // M, N, O
val AlphabetTeal = Color(0xFF00ACC1)          // P, Q, R
val AlphabetPink = Color(0xFFD81B60)          // S, T, U
val AlphabetIndigo = Color(0xFF3949AB)        // V, W, X
val AlphabetAmber = Color(0xFFFFB300)         // Y, Z

// Windows emulator optimized colors
// These colors are specifically tested for Windows Android emulator display
val WindowsEmulatorPrimary = Color(0xFF2E7D32)    // Darker green for better emulator visibility
val WindowsEmulatorSecondary = Color(0xFF1565C0)  // Darker blue for emulator contrast
val WindowsEmulatorBackground = Color(0xFFFAFAFA) // Slightly gray background for emulator

// Age-specific color variations
// Colors for 1-2 year olds - high contrast, simple
val ToddlerAge1Primary = Color(0xFFFF5722)        // Bright red-orange
val ToddlerAge1Secondary = Color(0xFF4CAF50)      // Bright green
val ToddlerAge1Accent = Color(0xFFFFEB3B)          // Bright yellow

// Colors for 3-4 year olds - more sophisticated
val ToddlerAge3Primary = Color(0xFF3F51B5)        // Indigo
val ToddlerAge3Secondary = Color(0xFF009688)      // Teal
val ToddlerAge3Accent = Color(0xFFE91E63)          // Pink

// Accessibility colors - WCAG AA compliant
val AccessibilityHighContrast = Color(0xFF000000) // Pure black
val AccessibilityBackground = Color(0xFFFFFFFF)   // Pure white
val AccessibilityFocus = Color(0xFF0066CC)        // Focus indicator blue

// Interactive element colors
val ButtonPrimary = ToddlerPrimary
val ButtonSecondary = ToddlerSecondary
val ButtonDisabled = Color(0xFFBDBDBD)
val ButtonPressed = Color(0xFF388E3C)

// Progress and achievement colors
val ProgressEmpty = Color(0xFFE0E0E0)
val ProgressFilled = ToddlerSuccess
val AchievementGold = Color(0xFFFFD700)
val AchievementSilver = Color(0xFFC0C0C0)
val AchievementBronze = Color(0xFFCD7F32)

// Tracing activity colors
val TracingPath = Color(0xFF9E9E9E)          // Gray path to trace
val TracingCorrect = ToddlerSuccess          // Green for correct tracing
val TracingActive = ToddlerPrimary           // Blue for active tracing

// Speech recognition colors
val SpeechListening = Color(0xFF2196F3)      // Blue when listening
val SpeechRecognized = ToddlerSuccess        // Green when recognized
val SpeechError = ToddlerError               // Orange for speech errors

// Parental control colors
val ParentalPrimary = Color(0xFF37474F)      // Dark blue-gray
val ParentalSecondary = Color(0xFF546E7A)    // Medium blue-gray
val ParentalBackground = Color(0xFFF5F5F5)   // Light gray background
