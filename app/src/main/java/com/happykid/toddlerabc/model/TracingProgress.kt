package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tracing Progress data class for the Happy Kid app
 * Phase 8: Pre-Writing and Tracing System database entity
 *
 * Tracks detailed tracing metrics including accuracy, completion rates,
 * stroke analysis, and improvement over time for each letter.
 */
@Entity(tableName = "tracing_progress")
data class TracingProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Letter identification
    val character: Char,
    
    // Session information
    val sessionTimestamp: Long = System.currentTimeMillis(),
    val sessionDurationMs: Long = 0,
    
    // Tracing accuracy metrics
    val accuracyPercentage: Float = 0f,
    val completionPercentage: Float = 0f,
    val strokeAccuracy: Float = 0f,
    
    // Stroke analysis
    val totalStrokes: Int = 0,
    val completedStrokes: Int = 0,
    val correctStrokeOrder: Boolean = false,
    val averageStrokeSpeed: Float = 0f,
    
    // Touch metrics
    val totalTouchPoints: Int = 0,
    val accurateTouchPoints: Int = 0,
    val averagePressure: Float = 0f,
    val smoothnessScore: Float = 0f,
    
    // Difficulty and assistance
    val difficultyLevel: Int = 1, // 1-5 scale
    val guidanceUsed: Boolean = true,
    val hintsUsed: Int = 0,
    val attemptsCount: Int = 1,
    
    // Success metrics
    val isCompleted: Boolean = false,
    val isSuccessful: Boolean = false,
    val milestoneAchieved: String? = null,
    
    // Performance metrics
    val frameRate: Float = 60f,
    val touchLatencyMs: Float = 0f,
    val renderingPerformance: Float = 100f
) {
    companion object {
        /**
         * Create initial tracing progress for a letter
         */
        fun createInitial(character: Char): TracingProgress {
            return TracingProgress(
                character = character,
                difficultyLevel = 1,
                guidanceUsed = true
            )
        }
        
        /**
         * Difficulty level descriptions
         */
        fun getDifficultyDescription(level: Int): String {
            return when (level) {
                1 -> "Beginner - Full guidance with dots"
                2 -> "Easy - Guided lines with arrows"
                3 -> "Medium - Outline only"
                4 -> "Hard - Faded outline"
                5 -> "Expert - No guidance"
                else -> "Unknown difficulty"
            }
        }
        
        /**
         * Calculate overall progress score (0-100)
         */
        fun calculateProgressScore(
            accuracyPercentage: Float,
            completionPercentage: Float,
            strokeAccuracy: Float
        ): Float {
            return (accuracyPercentage * 0.4f + 
                   completionPercentage * 0.4f + 
                   strokeAccuracy * 0.2f).coerceIn(0f, 100f)
        }
        
        /**
         * Determine if session qualifies as successful
         */
        fun isSessionSuccessful(
            accuracyPercentage: Float,
            completionPercentage: Float,
            difficultyLevel: Int
        ): Boolean {
            val requiredAccuracy = when (difficultyLevel) {
                1 -> 60f  // Beginner
                2 -> 65f  // Easy
                3 -> 70f  // Medium
                4 -> 75f  // Hard
                5 -> 80f  // Expert
                else -> 60f
            }
            
            return accuracyPercentage >= requiredAccuracy && completionPercentage >= 80f
        }
        
        /**
         * Generate milestone achievement text
         */
        fun generateMilestone(
            character: Char,
            accuracyPercentage: Float,
            isFirstCompletion: Boolean,
            difficultyLevel: Int
        ): String? {
            return when {
                isFirstCompletion -> "First time tracing letter ${character.uppercaseChar()}! 🎉"
                accuracyPercentage >= 95f -> "Perfect tracing of letter ${character.uppercaseChar()}! ⭐"
                accuracyPercentage >= 90f -> "Excellent tracing! Almost perfect! 👏"
                difficultyLevel >= 4 && accuracyPercentage >= 75f -> "Advanced tracing mastery! 🏆"
                else -> null
            }
        }
    }
}

/**
 * Tracing session summary for analytics and reporting
 * Phase 8: Comprehensive tracing analytics
 */
data class TracingSessionSummary(
    val character: Char,
    val totalSessions: Int,
    val averageAccuracy: Float,
    val averageCompletion: Float,
    val bestAccuracy: Float,
    val currentDifficultyLevel: Int,
    val totalPracticeTime: Long,
    val improvementTrend: Float, // Positive = improving, Negative = declining
    val lastPracticed: Long,
    val milestonesAchieved: List<String>
)

/**
 * Overall tracing statistics across all letters
 * Phase 8: Global tracing progress tracking
 */
data class TracingStats(
    val totalTracingSessions: Int,
    val totalPracticeTimeMs: Long,
    val averageAccuracyAllLetters: Float,
    val lettersStarted: Int,
    val lettersCompleted: Int,
    val lettersWithHighAccuracy: Int, // >= 80% accuracy
    val currentStreak: Int, // Consecutive successful sessions
    val bestStreak: Int,
    val favoriteLetters: List<Char>, // Most practiced letters
    val challengingLetters: List<Char>, // Lowest accuracy letters
    val recentImprovement: Float // Improvement over last 10 sessions
)
