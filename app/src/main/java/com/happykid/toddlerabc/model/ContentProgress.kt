package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.happykid.toddlerabc.data.ContentTypeConverters

/**
 * Content progress entity for tracking completion across all content types
 * Phase 12: Content Expansion - Comprehensive progress tracking for stories, vocabulary, and activities
 *
 * Tracks user progress across stories, vocabulary words, and activities with
 * detailed metrics for adaptive learning and parent dashboard insights.
 */
@Entity(tableName = "content_progress")
@TypeConverters(ContentTypeConverters::class)
data class ContentProgress(
    @PrimaryKey
    val id: String,
    
    /**
     * Content ID (story, vocabulary word, or activity)
     */
    val contentId: String,
    
    /**
     * Content type (story, vocabulary, activity)
     */
    val contentType: String,
    
    /**
     * User ID (for future multi-user support)
     */
    val userId: String = "default_user",
    
    /**
     * Progress status (not_started, in_progress, completed, mastered)
     */
    val status: String,
    
    /**
     * Completion percentage (0.0 - 1.0)
     */
    val completionPercentage: Float,
    
    /**
     * Number of attempts made
     */
    val attemptCount: Int,
    
    /**
     * Number of successful completions
     */
    val successCount: Int,
    
    /**
     * Best accuracy score achieved
     */
    val bestAccuracy: Float,
    
    /**
     * Average accuracy across all attempts
     */
    val averageAccuracy: Float,
    
    /**
     * Total time spent on this content (milliseconds)
     */
    val totalTimeSpent: Long,
    
    /**
     * Average time per attempt (milliseconds)
     */
    val averageTimePerAttempt: Long,
    
    /**
     * Last interaction timestamp
     */
    val lastInteractionAt: Long,
    
    /**
     * First started timestamp
     */
    val firstStartedAt: Long,
    
    /**
     * Completion timestamp (null if not completed)
     */
    val completedAt: Long?,
    
    /**
     * Mastery timestamp (null if not mastered)
     */
    val masteredAt: Long?,
    
    /**
     * Difficulty level when content was completed
     */
    val completionDifficulty: Int?,
    
    /**
     * Hints used during completion
     */
    val hintsUsed: Int,
    
    /**
     * Mistakes made during learning
     */
    val mistakeCount: Int,
    
    /**
     * Learning streak (consecutive successful attempts)
     */
    val learningStreak: Int,
    
    /**
     * Engagement score (0.0 - 1.0)
     */
    val engagementScore: Float,
    
    /**
     * Retention score for spaced repetition
     */
    val retentionScore: Float,
    
    /**
     * Next review date for spaced repetition
     */
    val nextReviewDate: Long?,
    
    /**
     * Spaced repetition interval in days
     */
    val repetitionInterval: Int,
    
    /**
     * Additional progress metadata
     */
    val metadata: Map<String, String> = emptyMap(),
    
    /**
     * Progress creation timestamp
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Last update timestamp
     */
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        // Progress status constants
        const val STATUS_NOT_STARTED = "not_started"
        const val STATUS_IN_PROGRESS = "in_progress"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_MASTERED = "mastered"
        
        // Content type constants
        const val CONTENT_TYPE_STORY = "story"
        const val CONTENT_TYPE_VOCABULARY = "vocabulary"
        const val CONTENT_TYPE_ACTIVITY = "activity"
        
        /**
         * Create initial progress entry for new content
         */
        fun createInitialProgress(
            contentId: String,
            contentType: String,
            userId: String = "default_user"
        ): ContentProgress {
            val now = System.currentTimeMillis()
            return ContentProgress(
                id = "${contentType}_${contentId}_${userId}",
                contentId = contentId,
                contentType = contentType,
                userId = userId,
                status = STATUS_NOT_STARTED,
                completionPercentage = 0.0f,
                attemptCount = 0,
                successCount = 0,
                bestAccuracy = 0.0f,
                averageAccuracy = 0.0f,
                totalTimeSpent = 0L,
                averageTimePerAttempt = 0L,
                lastInteractionAt = now,
                firstStartedAt = now,
                completedAt = null,
                masteredAt = null,
                completionDifficulty = null,
                hintsUsed = 0,
                mistakeCount = 0,
                learningStreak = 0,
                engagementScore = 0.0f,
                retentionScore = 0.0f,
                nextReviewDate = null,
                repetitionInterval = 1,
                createdAt = now,
                updatedAt = now
            )
        }
        
        /**
         * Update progress after an attempt
         */
        fun updateAfterAttempt(
            current: ContentProgress,
            accuracy: Float,
            timeSpent: Long,
            hintsUsed: Int,
            mistakes: Int,
            isCompleted: Boolean
        ): ContentProgress {
            val now = System.currentTimeMillis()
            val newAttemptCount = current.attemptCount + 1
            val newSuccessCount = if (isCompleted) current.successCount + 1 else current.successCount
            val newTotalTime = current.totalTimeSpent + timeSpent
            val newAverageTime = newTotalTime / newAttemptCount
            val newAverageAccuracy = ((current.averageAccuracy * current.attemptCount) + accuracy) / newAttemptCount
            val newBestAccuracy = maxOf(current.bestAccuracy, accuracy)
            val newStreak = if (isCompleted) current.learningStreak + 1 else 0
            
            // Calculate engagement score based on time spent and accuracy
            val engagementScore = calculateEngagementScore(timeSpent, accuracy, hintsUsed, mistakes)
            
            // Calculate retention score for spaced repetition
            val retentionScore = calculateRetentionScore(accuracy, newStreak, current.retentionScore)
            
            // Determine new status
            val newStatus = when {
                newStreak >= 3 && newBestAccuracy >= 0.9f -> STATUS_MASTERED
                isCompleted -> STATUS_COMPLETED
                newAttemptCount > 0 -> STATUS_IN_PROGRESS
                else -> STATUS_NOT_STARTED
            }
            
            // Calculate completion percentage
            val completionPercentage = when (newStatus) {
                STATUS_MASTERED -> 1.0f
                STATUS_COMPLETED -> 0.8f + (newBestAccuracy * 0.2f)
                STATUS_IN_PROGRESS -> minOf(0.7f, newAttemptCount * 0.1f + (newAverageAccuracy * 0.3f))
                else -> 0.0f
            }
            
            return current.copy(
                status = newStatus,
                completionPercentage = completionPercentage,
                attemptCount = newAttemptCount,
                successCount = newSuccessCount,
                bestAccuracy = newBestAccuracy,
                averageAccuracy = newAverageAccuracy,
                totalTimeSpent = newTotalTime,
                averageTimePerAttempt = newAverageTime,
                lastInteractionAt = now,
                completedAt = if (isCompleted && current.completedAt == null) now else current.completedAt,
                masteredAt = if (newStatus == STATUS_MASTERED && current.masteredAt == null) now else current.masteredAt,
                hintsUsed = current.hintsUsed + hintsUsed,
                mistakeCount = current.mistakeCount + mistakes,
                learningStreak = newStreak,
                engagementScore = engagementScore,
                retentionScore = retentionScore,
                nextReviewDate = calculateNextReviewDate(retentionScore),
                repetitionInterval = calculateRepetitionInterval(retentionScore, current.repetitionInterval),
                updatedAt = now
            )
        }
        
        /**
         * Calculate engagement score based on interaction metrics
         */
        private fun calculateEngagementScore(
            timeSpent: Long,
            accuracy: Float,
            hintsUsed: Int,
            mistakes: Int
        ): Float {
            // Base score from accuracy
            var score = accuracy
            
            // Adjust for time spent (optimal range: 30s - 5min)
            val timeSeconds = timeSpent / 1000
            val timeScore = when {
                timeSeconds < 30 -> 0.5f // Too fast, might not be engaged
                timeSeconds > 300 -> 0.7f // Too slow, might be struggling
                else -> 1.0f // Good engagement time
            }
            score *= timeScore
            
            // Penalize excessive hint usage
            val hintPenalty = minOf(0.3f, hintsUsed * 0.1f)
            score -= hintPenalty
            
            // Penalize mistakes but not too harshly (learning opportunity)
            val mistakePenalty = minOf(0.2f, mistakes * 0.05f)
            score -= mistakePenalty
            
            return maxOf(0.0f, minOf(1.0f, score))
        }
        
        /**
         * Calculate retention score for spaced repetition
         */
        private fun calculateRetentionScore(
            accuracy: Float,
            streak: Int,
            previousRetention: Float
        ): Float {
            // Weighted average of current performance and previous retention
            val currentWeight = 0.3f
            val previousWeight = 0.7f
            
            val currentScore = accuracy + (streak * 0.1f)
            return (currentScore * currentWeight) + (previousRetention * previousWeight)
        }
        
        /**
         * Calculate next review date based on retention score
         */
        private fun calculateNextReviewDate(retentionScore: Float): Long {
            val now = System.currentTimeMillis()
            val daysToAdd = when {
                retentionScore >= 0.9f -> 7 // High retention: review in a week
                retentionScore >= 0.7f -> 3 // Medium retention: review in 3 days
                retentionScore >= 0.5f -> 1 // Low retention: review tomorrow
                else -> 0 // Very low retention: review today
            }
            return now + (daysToAdd * 24 * 60 * 60 * 1000L)
        }
        
        /**
         * Calculate spaced repetition interval
         */
        private fun calculateRepetitionInterval(retentionScore: Float, currentInterval: Int): Int {
            return when {
                retentionScore >= 0.9f -> minOf(currentInterval * 2, 30) // Double interval, max 30 days
                retentionScore >= 0.7f -> currentInterval // Keep same interval
                else -> maxOf(currentInterval / 2, 1) // Halve interval, min 1 day
            }
        }
    }
}
