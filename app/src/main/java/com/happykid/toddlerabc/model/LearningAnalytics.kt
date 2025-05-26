package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Learning Analytics data models for Happy Kid app
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Comprehensive analytics system for tracking learning progress,
 * performance patterns, and adaptive difficulty adjustments.
 */

/**
 * Learning session analytics entity
 * Tracks detailed session metrics across all learning activities
 */
@Entity(tableName = "learning_analytics")
data class LearningAnalytics(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Session identification
    val sessionId: String,
    val userId: String = "default_user", // For future multi-user support
    val activityType: LearningActivityType,
    val sessionTimestamp: Long = System.currentTimeMillis(),
    val sessionDurationMs: Long = 0,

    // Performance metrics
    val accuracyPercentage: Float = 0f,
    val completionPercentage: Float = 0f,
    val attemptsCount: Int = 1,
    val hintsUsed: Int = 0,
    val errorsCount: Int = 0,

    // Learning content
    val contentId: String, // Letter, word, or phonics content ID
    val difficultyLevel: Int = 1, // 1-5 scale
    val adaptiveDifficultyAdjustment: Int = 0, // -2 to +2 adjustment

    // Engagement metrics
    val engagementScore: Float = 0f, // 0-100 scale
    val focusTimeMs: Long = 0, // Time actively engaged
    val distractionEvents: Int = 0,

    // Learning outcomes
    val masteryLevel: MasteryLevel = MasteryLevel.BEGINNER,
    val improvementRate: Float = 0f, // Percentage improvement from previous sessions
    val retentionScore: Float = 0f, // How well content is retained over time

    // Adaptive learning data
    val recommendedNextActivity: String? = null,
    val suggestedDifficultyLevel: Int = 1,
    val learningPathProgress: Float = 0f, // 0-100% through learning path

    // Performance context
    val timeOfDay: Int = 0, // Hour of day (0-23)
    val dayOfWeek: Int = 1, // 1-7 (Monday = 1)
    val sessionSequenceNumber: Int = 1, // Session number in current day

    // Technical metrics
    val frameRate: Float = 60f,
    val responseTimeMs: Float = 0f,
    val devicePerformanceScore: Float = 100f
)

/**
 * Learning activity types for analytics tracking
 */
enum class LearningActivityType {
    ALPHABET_LEARNING,
    LETTER_TRACING,
    PHONICS_PRACTICE,
    SIGHT_WORDS,
    READING_COMPREHENSION,
    SPEECH_RECOGNITION,
    ASSESSMENT,
    FREE_PLAY
}

/**
 * Mastery levels for learning content
 */
enum class MasteryLevel {
    BEGINNER,      // 0-25% mastery
    DEVELOPING,    // 26-50% mastery
    PROFICIENT,    // 51-75% mastery
    ADVANCED,      // 76-90% mastery
    EXPERT         // 91-100% mastery
}

/**
 * Achievement entity for milestone tracking
 */
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Achievement identification
    val achievementId: String,
    val userId: String = "default_user",
    val title: String,
    val description: String,
    val category: AchievementCategory,

    // Achievement details
    val iconResource: String,
    val badgeColor: String = "#FFD700", // Gold default
    val pointsAwarded: Int = 0,
    val difficultyTier: Int = 1, // 1-5 scale

    // Progress tracking
    val isUnlocked: Boolean = false,
    val unlockedTimestamp: Long? = null,
    val progressCurrent: Int = 0,
    val progressTarget: Int = 1,
    val progressPercentage: Float = 0f,

    // Requirements
    val requirementType: AchievementRequirement,
    val requirementValue: String, // JSON string for complex requirements
    val prerequisiteAchievements: String? = null, // Comma-separated achievement IDs

    // Celebration
    val celebrationMessage: String,
    val celebrationAnimation: String = "confetti",
    val shareableText: String? = null
) {
    // Computed property for compatibility
    val unlockedAt: Long get() = unlockedTimestamp ?: 0L
}

/**
 * Achievement categories
 */
enum class AchievementCategory {
    ALPHABET_MASTERY,
    TRACING_SKILLS,
    PHONICS_PROGRESS,
    READING_MILESTONES,
    CONSISTENCY,
    SPEED_ACCURACY,
    EXPLORATION,
    SPECIAL_EVENTS
}

/**
 * Achievement requirement types
 */
enum class AchievementRequirement {
    COMPLETE_SESSIONS,      // Complete X sessions
    ACCURACY_THRESHOLD,     // Achieve X% accuracy
    CONSECUTIVE_DAYS,       // Practice X consecutive days
    MASTER_CONTENT,         // Master X letters/words
    TIME_SPENT,            // Spend X minutes learning
    PERFECT_SESSIONS,      // Complete X perfect sessions
    IMPROVEMENT_RATE,      // Show X% improvement
    EXPLORATION,           // Try X different activities
    SPEED_MILESTONE,       // Complete task in X seconds
    CUSTOM                 // Custom requirement logic
}

/**
 * Adaptive difficulty recommendation
 */
data class DifficultyRecommendation(
    val contentId: String,
    val currentDifficulty: Int,
    val recommendedDifficulty: Int,
    val adjustmentReason: DifficultyAdjustmentReason,
    val confidence: Float, // 0-1 confidence in recommendation
    val supportingData: String // JSON with supporting metrics
)

/**
 * Reasons for difficulty adjustments
 */
enum class DifficultyAdjustmentReason {
    HIGH_ACCURACY,          // Consistently high performance
    LOW_ACCURACY,           // Struggling with current level
    FAST_COMPLETION,        // Completing too quickly
    SLOW_PROGRESS,          // Taking too long
    ENGAGEMENT_DROP,        // Losing interest/engagement
    MASTERY_ACHIEVED,       // Content mastered
    REGRESSION_DETECTED,    // Performance declining
    OPTIMAL_CHALLENGE       // Maintain current level
}

/**
 * Learning path progress tracking
 */
@Entity(tableName = "learning_paths")
data class LearningPath(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val pathId: String,
    val userId: String = "default_user",
    val pathName: String,
    val pathDescription: String,
    val pathType: LearningPathType,

    // Progress tracking
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val progressPercentage: Float = 0f,
    val estimatedCompletionDays: Int = 0,

    // Adaptive features
    val isAdaptive: Boolean = true,
    val difficultyLevel: Int = 1,
    val personalizedContent: String? = null, // JSON with personalized steps

    // Timing
    val startedTimestamp: Long? = null,
    val lastAccessedTimestamp: Long? = null,
    val completedTimestamp: Long? = null,

    // Performance
    val averageAccuracy: Float = 0f,
    val totalTimeSpentMs: Long = 0,
    val masteredSteps: Int = 0
)

/**
 * Learning path types
 */
enum class LearningPathType {
    ALPHABET_BASICS,
    LETTER_RECOGNITION,
    PHONICS_FOUNDATION,
    EARLY_READING,
    WRITING_PREPARATION,
    COMPREHENSIVE_LITERACY,
    REMEDIAL_SUPPORT,
    ADVANCED_CHALLENGE,
    CUSTOM_PATH
}

/**
 * Parent dashboard insights
 */
data class ParentInsight(
    val insightId: String,
    val title: String,
    val description: String,
    val category: InsightCategory,
    val priority: InsightPriority,
    val actionable: Boolean = true,
    val recommendations: List<String> = emptyList(),
    val supportingData: Map<String, Any> = emptyMap(),
    val generatedTimestamp: Long = System.currentTimeMillis()
)

/**
 * Insight categories for parent dashboard
 */
enum class InsightCategory {
    PROGRESS_SUMMARY,
    STRENGTH_AREAS,
    IMPROVEMENT_OPPORTUNITIES,
    LEARNING_PATTERNS,
    ENGAGEMENT_TRENDS,
    MILESTONE_ACHIEVEMENTS,
    RECOMMENDED_ACTIVITIES,
    DEVELOPMENTAL_READINESS
}

/**
 * Insight priority levels
 */
enum class InsightPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
