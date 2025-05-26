package com.happykid.toddlerabc.data

import androidx.room.TypeConverter
import com.happykid.toddlerabc.model.*
import com.happykid.toddlerabc.analytics.*

/**
 * Type converters for analytics-related enums and complex types
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Converts enum types to strings for Room database storage
 * and back to enum types when reading from database.
 */
class AnalyticsTypeConverters {

    // ==================== Learning Activity Type Converters ====================

    @TypeConverter
    fun fromLearningActivityType(activityType: LearningActivityType): String {
        return activityType.name
    }

    @TypeConverter
    fun toLearningActivityType(activityType: String): LearningActivityType {
        return LearningActivityType.valueOf(activityType)
    }

    // ==================== Mastery Level Converters ====================

    @TypeConverter
    fun fromMasteryLevel(masteryLevel: MasteryLevel): String {
        return masteryLevel.name
    }

    @TypeConverter
    fun toMasteryLevel(masteryLevel: String): MasteryLevel {
        return MasteryLevel.valueOf(masteryLevel)
    }

    // ==================== Achievement Category Converters ====================

    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory): String {
        return category.name
    }

    @TypeConverter
    fun toAchievementCategory(category: String): AchievementCategory {
        return AchievementCategory.valueOf(category)
    }

    // ==================== Achievement Requirement Converters ====================

    @TypeConverter
    fun fromAchievementRequirement(requirement: AchievementRequirement): String {
        return requirement.name
    }

    @TypeConverter
    fun toAchievementRequirement(requirement: String): AchievementRequirement {
        return AchievementRequirement.valueOf(requirement)
    }

    // ==================== Learning Path Type Converters ====================

    @TypeConverter
    fun fromLearningPathType(pathType: LearningPathType): String {
        return pathType.name
    }

    @TypeConverter
    fun toLearningPathType(pathType: String): LearningPathType {
        return LearningPathType.valueOf(pathType)
    }

    // ==================== Difficulty Adjustment Reason Converters ====================

    @TypeConverter
    fun fromDifficultyAdjustmentReason(reason: DifficultyAdjustmentReason): String {
        return reason.name
    }

    @TypeConverter
    fun toDifficultyAdjustmentReason(reason: String): DifficultyAdjustmentReason {
        return DifficultyAdjustmentReason.valueOf(reason)
    }

    // ==================== Engagement Recommendation Type Converters ====================

    @TypeConverter
    fun fromEngagementRecommendationType(type: EngagementRecommendationType): String {
        return type.name
    }

    @TypeConverter
    fun toEngagementRecommendationType(type: String): EngagementRecommendationType {
        return EngagementRecommendationType.valueOf(type)
    }

    // ==================== Engagement Priority Converters ====================

    @TypeConverter
    fun fromEngagementPriority(priority: EngagementPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toEngagementPriority(priority: String): EngagementPriority {
        return EngagementPriority.valueOf(priority)
    }

    // ==================== Insight Category Converters ====================

    @TypeConverter
    fun fromInsightCategory(category: InsightCategory): String {
        return category.name
    }

    @TypeConverter
    fun toInsightCategory(category: String): InsightCategory {
        return InsightCategory.valueOf(category)
    }

    // ==================== Insight Priority Converters ====================

    @TypeConverter
    fun fromInsightPriority(priority: InsightPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toInsightPriority(priority: String): InsightPriority {
        return InsightPriority.valueOf(priority)
    }

    // ==================== Recommendation Type Converters ====================

    @TypeConverter
    fun fromRecommendationType(type: RecommendationType): String {
        return type.name
    }

    @TypeConverter
    fun toRecommendationType(type: String): RecommendationType {
        return RecommendationType.valueOf(type)
    }

    // ==================== Recommendation Priority Converters ====================

    @TypeConverter
    fun fromRecommendationPriority(priority: RecommendationPriority): String {
        return priority.name
    }

    @TypeConverter
    fun toRecommendationPriority(priority: String): RecommendationPriority {
        return RecommendationPriority.valueOf(priority)
    }

    // ==================== Report Timeframe Converters ====================

    @TypeConverter
    fun fromReportTimeframe(timeframe: ReportTimeframe): String {
        return timeframe.name
    }

    @TypeConverter
    fun toReportTimeframe(timeframe: String): ReportTimeframe {
        return ReportTimeframe.valueOf(timeframe)
    }

    // ==================== Export Format Converters ====================

    @TypeConverter
    fun fromExportFormat(format: ExportFormat): String {
        return format.name
    }

    @TypeConverter
    fun toExportFormat(format: String): ExportFormat {
        return ExportFormat.valueOf(format)
    }

    // ==================== List Converters ====================
    // Note: String list converters are handled by PhonicsTypeConverters

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isEmpty()) emptyList() else value.split(",").map { it.toInt() }
    }

    // ==================== Map Converters ====================

    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        if (value.isEmpty()) return emptyMap()
        return value.split(";").associate { entry ->
            val (key, value) = entry.split(":")
            key to value
        }
    }

    @TypeConverter
    fun fromStringFloatMap(value: Map<String, Float>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringFloatMap(value: String): Map<String, Float> {
        if (value.isEmpty()) return emptyMap()
        return value.split(";").associate { entry ->
            val (key, value) = entry.split(":")
            key to value.toFloat()
        }
    }

    @TypeConverter
    fun fromStringAnyMap(value: Map<String, Any>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }

    @TypeConverter
    fun toStringAnyMap(value: String): Map<String, Any> {
        if (value.isEmpty()) return emptyMap()
        return value.split(";").associate { entry ->
            val (key, value) = entry.split(":")
            key to value
        }
    }

    // ==================== Nullable Converters ====================
    // Note: Basic nullable converters removed to avoid conflicts
    // Only keeping specialized nullable converters if needed
}
