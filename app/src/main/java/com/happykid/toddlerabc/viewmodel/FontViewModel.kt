package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.ui.text.font.FontFamily
import com.happykid.toddlerabc.manager.FontManager
import com.happykid.toddlerabc.model.FontPreference
import com.happykid.toddlerabc.model.ToddlerFontFamily
import com.happykid.toddlerabc.model.FontSizeScale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel wrapper for FontManager
 * Phase 5: Provides ViewModel interface for font management in Compose
 * 
 * This ViewModel wraps the FontManager to provide proper Hilt injection
 * and lifecycle management for font preferences in Compose screens.
 */
@HiltViewModel
class FontViewModel @Inject constructor(
    private val fontManager: FontManager
) : ViewModel() {

    /**
     * Current font preference as Flow for reactive UI updates
     */
    val currentFontPreference: Flow<FontPreference> = fontManager.currentFontPreference

    /**
     * Current font family as Flow
     */
    val currentFontFamily: Flow<FontFamily> = fontManager.currentFontFamily

    /**
     * Current font size scale as Flow
     */
    val currentFontSizeScale: Flow<Float> = fontManager.currentFontSizeScale

    /**
     * Initialize font manager
     */
    fun initialize() {
        viewModelScope.launch {
            fontManager.initialize()
        }
    }

    /**
     * Get FontFamily for a given font resource name
     */
    fun getFontFamily(fontResourceName: String): FontFamily {
        return fontManager.getFontFamily(fontResourceName)
    }

    /**
     * Update selected font family
     */
    fun updateFontFamily(fontFamily: ToddlerFontFamily) {
        viewModelScope.launch {
            fontManager.updateFontFamily(fontFamily)
        }
    }

    /**
     * Update font size scale
     */
    fun updateFontSize(scale: FontSizeScale) {
        viewModelScope.launch {
            fontManager.updateFontSize(scale)
        }
    }

    /**
     * Reset to default font preferences
     */
    fun resetToDefaults() {
        viewModelScope.launch {
            fontManager.resetToDefaults()
        }
    }

    /**
     * Get all available font families for selection
     */
    fun getAvailableFonts(): List<ToddlerFontFamily> {
        return fontManager.getAvailableFonts()
    }

    /**
     * Get all available font size scales
     */
    fun getAvailableFontSizes(): List<FontSizeScale> {
        return fontManager.getAvailableFontSizes()
    }

    /**
     * Check if a font is available and working
     */
    fun isFontAvailable(fontResourceName: String): Boolean {
        return fontManager.isFontAvailable(fontResourceName)
    }

    /**
     * Get font preview text for testing
     */
    fun getFontPreviewText(): String {
        return fontManager.getFontPreviewText()
    }
}
