package com.happykid.toddlerabc.data

import androidx.room.TypeConverter
import com.happykid.toddlerabc.model.InteractiveElement
import com.happykid.toddlerabc.model.ActivityConfiguration

/**
 * Type converters for content expansion entities
 * Phase 12: Content Expansion - Room type converters for new content entities
 *
 * Handles conversion between complex data types and database storage formats
 * for stories, vocabulary words, activities, and content progress.
 * Uses simple string serialization to avoid external dependencies.
 */
class ContentTypeConverters {

    // Note: String List converters are already defined in PhonicsTypeConverters

    // ==================== Interactive Element Converters ====================

    @TypeConverter
    fun fromInteractiveElementList(value: List<InteractiveElement>?): String {
        if (value == null || value.isEmpty()) return ""
        return value.joinToString("||") { element ->
            "${element.type}|${element.target}|${element.instruction}|${element.feedback ?: ""}|${element.points}"
        }
    }

    @TypeConverter
    fun toInteractiveElementList(value: String): List<InteractiveElement> {
        if (value.isEmpty()) return emptyList()
        return value.split("||").mapNotNull { elementStr ->
            val parts = elementStr.split("|")
            if (parts.size >= 5) {
                InteractiveElement(
                    type = parts[0],
                    target = parts[1],
                    instruction = parts[2],
                    feedback = if (parts[3].isEmpty()) null else parts[3],
                    points = parts[4].toIntOrNull() ?: 10
                )
            } else null
        }
    }

    // ==================== Activity Configuration Converters ====================

    @TypeConverter
    fun fromActivityConfiguration(value: ActivityConfiguration?): String {
        if (value == null) return ""
        val parametersStr = value.parameters.entries.joinToString(";") { "${it.key}:${it.value}" }
        return "${value.itemCount}|${value.timeLimit}|${value.showHints}|${value.allowRetries}|$parametersStr"
    }

    @TypeConverter
    fun toActivityConfiguration(value: String): ActivityConfiguration? {
        if (value.isEmpty()) return null
        return try {
            val parts = value.split("|")
            if (parts.size >= 5) {
                val parameters = if (parts[4].isEmpty()) {
                    emptyMap()
                } else {
                    parts[4].split(";").associate { entry ->
                        val (key, value) = entry.split(":")
                        key to value
                    }
                }
                ActivityConfiguration(
                    itemCount = parts[0].toInt(),
                    timeLimit = parts[1].toInt(),
                    showHints = parts[2].toBoolean(),
                    allowRetries = parts[3].toBoolean(),
                    parameters = parameters
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Note: Map converters are already defined in AnalyticsTypeConverters
}
