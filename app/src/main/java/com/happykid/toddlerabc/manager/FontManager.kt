package com.happykid.toddlerabc.manager

import android.content.Context
import android.util.Log
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.happykid.toddlerabc.R
import com.happykid.toddlerabc.data.FontPreferenceDao
import com.happykid.toddlerabc.model.FontPreference
import com.happykid.toddlerabc.model.ToddlerFontFamily
import com.happykid.toddlerabc.model.FontSizeScale
import com.happykid.toddlerabc.util.WindowsDeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Font Manager for Happy Kid app
 * Phase 5: Centralized font management with Hilt dependency injection
 * Phase 6: Enhanced with Windows emulator font rendering optimizations
 *
 * Manages font loading, caching, and preference persistence while maintaining
 * compatibility with Windows emulator and existing Phase 1-4 functionality.
 * Includes Windows-specific font rendering optimizations for improved performance.
 */
@Singleton
class FontManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fontPreferenceDao: FontPreferenceDao
) {

    companion object {
        private const val TAG = "FontManager"
        private const val WINDOWS_FONT_CACHE_SIZE = 10
        private const val STANDARD_FONT_CACHE_SIZE = 5
    }

    // Font family cache for performance - optimized for Windows emulator
    private val fontFamilyCache = mutableMapOf<String, FontFamily>()

    // Windows emulator font optimization settings
    private val isWindowsEmulator = WindowsDeviceUtils.isWindowsEmulator()
    private val maxCacheSize = if (isWindowsEmulator) WINDOWS_FONT_CACHE_SIZE else STANDARD_FONT_CACHE_SIZE

    /**
     * Get current font preference as Flow for reactive UI updates
     */
    val currentFontPreference: Flow<FontPreference> = fontPreferenceDao.getFontPreferenceFlow()
        .map { it ?: FontPreference() }

    /**
     * Get current font family as Flow
     */
    val currentFontFamily: Flow<FontFamily> = currentFontPreference
        .map { preference -> getFontFamily(preference.selectedFontFamily) }

    /**
     * Get current font size scale as Flow
     */
    val currentFontSizeScale: Flow<Float> = currentFontPreference
        .map { it.fontSize }

    /**
     * Initialize font manager and ensure default preferences exist
     */
    suspend fun initialize() {
        fontPreferenceDao.initializeDefaultIfNotExists()
    }

    /**
     * Get FontFamily for a given font resource name with caching
     * Phase 6: Enhanced with Windows emulator font optimization
     */
    fun getFontFamily(fontResourceName: String): FontFamily {
        return fontFamilyCache.getOrPut(fontResourceName) {
            val startTime = System.currentTimeMillis()
            val fontFamily = createFontFamily(fontResourceName)

            // Log font loading performance for Windows development
            if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                val loadTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "Font '$fontResourceName' loaded in ${loadTime}ms (Windows emulator)")
            }

            // Manage cache size for Windows emulator optimization
            manageFontCache()

            fontFamily
        }
    }

    /**
     * Manage font cache size for optimal Windows emulator performance
     * Phase 6: Windows-specific cache management
     */
    private fun manageFontCache() {
        if (fontFamilyCache.size > maxCacheSize) {
            // Remove oldest entries to maintain optimal cache size
            val entriesToRemove = fontFamilyCache.size - maxCacheSize
            val iterator = fontFamilyCache.iterator()
            repeat(entriesToRemove) {
                if (iterator.hasNext()) {
                    iterator.next()
                    iterator.remove()
                }
            }

            if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                Log.d(TAG, "Font cache trimmed to $maxCacheSize entries for Windows emulator optimization")
            }
        }
    }

    /**
     * Create FontFamily from resource name with fallback handling
     */
    private fun createFontFamily(fontResourceName: String): FontFamily {
        return try {
            when (fontResourceName) {
                "default" -> FontFamily.Default
                "abeezee_regular" -> createCustomFontFamily("ABeeZee")
                "lexend_regular" -> createCustomFontFamily("Lexend")
                "atkinson_hyperlegible_regular" -> createCustomFontFamily("Atkinson Hyperlegible")
                "comic_neue_regular" -> createCustomFontFamily("Comic Neue")
                else -> FontFamily.Default
            }
        } catch (e: Exception) {
            // Fallback to default font if custom font fails to load
            FontFamily.Default
        }
    }

    /**
     * Create custom font family with proper fallback
     * For Phase 5 implementation, we'll use system fonts with fallback
     * In production, this would load actual TTF files
     */
    private fun createCustomFontFamily(fontName: String): FontFamily {
        return try {
            // For now, return system fonts as placeholders
            // In production, this would load actual font files:
            // FontFamily(Font(R.font.actual_font_file))
            when (fontName) {
                "ABeeZee" -> FontFamily.SansSerif // Child-friendly fallback
                "Lexend" -> FontFamily.Default // Readable fallback
                "Atkinson Hyperlegible" -> FontFamily.Monospace // High contrast fallback
                "Comic Neue" -> FontFamily.Cursive // Friendly fallback
                else -> FontFamily.Default
            }
        } catch (e: Exception) {
            FontFamily.Default
        }
    }

    /**
     * Update selected font family
     */
    suspend fun updateFontFamily(fontFamily: ToddlerFontFamily) {
        fontPreferenceDao.updateFontFamily(fontFamily.fontResourceName)
    }

    /**
     * Update font size scale
     */
    suspend fun updateFontSize(scale: FontSizeScale) {
        fontPreferenceDao.updateFontSize(scale.scale)
    }

    /**
     * Reset to default font preferences
     */
    suspend fun resetToDefaults() {
        fontPreferenceDao.resetFontPreferences()
        fontPreferenceDao.initializeDefaultIfNotExists()
        // Clear cache to force reload
        fontFamilyCache.clear()
    }

    /**
     * Get all available font families for selection
     */
    fun getAvailableFonts(): List<ToddlerFontFamily> {
        return ToddlerFontFamily.getAllFonts()
    }

    /**
     * Get all available font size scales
     */
    fun getAvailableFontSizes(): List<FontSizeScale> {
        return FontSizeScale.values().toList()
    }

    /**
     * Check if a font is available and working
     */
    fun isFontAvailable(fontResourceName: String): Boolean {
        return try {
            createFontFamily(fontResourceName)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get font preview text for testing
     */
    fun getFontPreviewText(): String {
        return "ABC abc 123\nHello Toddler!\nLearning is Fun!"
    }
}
