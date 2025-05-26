package com.happykid.toddlerabc.data

import androidx.room.TypeConverter
import com.happykid.toddlerabc.model.WordCategory
import com.happykid.toddlerabc.model.PhonicsActivityType

/**
 * Type converters for Phonics entities
 * Phase 9: Handles complex data type conversions for Room database
 *
 * Converts complex types like Lists, Enums, and IntRange to/from database-compatible formats
 * for proper storage and retrieval of phonics word and progress data.
 */
class PhonicsTypeConverters {

    /**
     * Convert List<String> to String for database storage
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    /**
     * Convert String to List<String> from database
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split(",")
    }

    /**
     * Convert IntRange to String for database storage
     */
    @TypeConverter
    fun fromIntRange(value: IntRange): String {
        return "${value.first}-${value.last}"
    }

    /**
     * Convert String to IntRange from database
     */
    @TypeConverter
    fun toIntRange(value: String): IntRange {
        val parts = value.split("-")
        return if (parts.size == 2) {
            parts[0].toInt()..parts[1].toInt()
        } else {
            2..4 // Default range
        }
    }

    /**
     * Convert WordCategory enum to String for database storage
     */
    @TypeConverter
    fun fromWordCategory(value: WordCategory): String {
        return value.name
    }

    /**
     * Convert String to WordCategory enum from database
     */
    @TypeConverter
    fun toWordCategory(value: String): WordCategory {
        return try {
            WordCategory.valueOf(value)
        } catch (e: IllegalArgumentException) {
            WordCategory.CVC // Default category
        }
    }

    /**
     * Convert PhonicsActivityType enum to String for database storage
     */
    @TypeConverter
    fun fromPhonicsActivityType(value: PhonicsActivityType): String {
        return value.name
    }

    /**
     * Convert String to PhonicsActivityType enum from database
     */
    @TypeConverter
    fun toPhonicsActivityType(value: String): PhonicsActivityType {
        return try {
            PhonicsActivityType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            PhonicsActivityType.WORD_RECOGNITION // Default activity type
        }
    }
}
