package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Font preference entity for Room database
 * Phase 5: Custom fonts integration
 */
@Entity(tableName = "font_preferences")
data class FontPreference(
    @PrimaryKey
    val id: Int = 1, // Single row for app-wide font preference
    val selectedFontFamily: String = "default",
    val fontSize: Float = 1.0f, // Scale factor for font size
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Available font families for toddler learning
 */
enum class ToddlerFontFamily(
    val displayName: String,
    val fontResourceName: String,
    val description: String
) {
    DEFAULT("System Default", "default", "Standard Android font, reliable and clear"),
    ABEEZEE("ABeeZee", "abeezee_regular", "Designed specifically for children's learning"),
    LEXEND("Lexend", "lexend_regular", "Proven to improve reading comprehension"),
    ATKINSON("Atkinson Hyperlegible", "atkinson_hyperlegible_regular", "High contrast for better visibility"),
    COMIC_NEUE("Comic Neue", "comic_neue_regular", "Friendly and approachable for young learners");

    companion object {
        fun fromResourceName(resourceName: String): ToddlerFontFamily {
            return values().find { it.fontResourceName == resourceName } ?: DEFAULT
        }

        fun getAllFonts(): List<ToddlerFontFamily> = values().toList()
    }
}

/**
 * Font size scale options for accessibility
 */
enum class FontSizeScale(
    val displayName: String,
    val scale: Float,
    val description: String
) {
    SMALL("Small", 0.85f, "Compact text for smaller screens"),
    NORMAL("Normal", 1.0f, "Standard size for most toddlers"),
    LARGE("Large", 1.15f, "Larger text for better visibility"),
    EXTRA_LARGE("Extra Large", 1.3f, "Maximum size for accessibility");

    companion object {
        fun fromScale(scale: Float): FontSizeScale {
            return values().minByOrNull { kotlin.math.abs(it.scale - scale) } ?: NORMAL
        }
    }
}
