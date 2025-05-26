package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.ContentProgress
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ContentProgress entities
 * Phase 12: Content Expansion - Content progress tracking with Room database
 *
 * Provides database operations for tracking user progress across all content types
 * including stories, vocabulary words, and activities with spaced repetition support.
 */
@Dao
interface ContentProgressDao {
    
    // ==================== Query Operations ====================
    
    /**
     * Get all content progress as Flow for reactive UI updates
     */
    @Query("SELECT * FROM content_progress ORDER BY lastInteractionAt DESC")
    fun getAllContentProgressFlow(): Flow<List<ContentProgress>>
    
    /**
     * Get all content progress (non-reactive)
     */
    @Query("SELECT * FROM content_progress ORDER BY lastInteractionAt DESC")
    suspend fun getAllContentProgress(): List<ContentProgress>
    
    /**
     * Get content progress by ID
     */
    @Query("SELECT * FROM content_progress WHERE id = :progressId")
    suspend fun getContentProgressById(progressId: String): ContentProgress?
    
    /**
     * Get content progress by ID as Flow
     */
    @Query("SELECT * FROM content_progress WHERE id = :progressId")
    fun getContentProgressByIdFlow(progressId: String): Flow<ContentProgress?>
    
    /**
     * Get content progress by content ID and user ID
     */
    @Query("SELECT * FROM content_progress WHERE contentId = :contentId AND userId = :userId")
    suspend fun getContentProgress(contentId: String, userId: String = "default_user"): ContentProgress?
    
    /**
     * Get content progress by content ID and user ID as Flow
     */
    @Query("SELECT * FROM content_progress WHERE contentId = :contentId AND userId = :userId")
    fun getContentProgressFlow(contentId: String, userId: String = "default_user"): Flow<ContentProgress?>
    
    /**
     * Get progress by content type
     */
    @Query("SELECT * FROM content_progress WHERE contentType = :contentType AND userId = :userId ORDER BY lastInteractionAt DESC")
    suspend fun getProgressByContentType(contentType: String, userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get progress by content type as Flow
     */
    @Query("SELECT * FROM content_progress WHERE contentType = :contentType AND userId = :userId ORDER BY lastInteractionAt DESC")
    fun getProgressByContentTypeFlow(contentType: String, userId: String = "default_user"): Flow<List<ContentProgress>>
    
    /**
     * Get progress by status
     */
    @Query("SELECT * FROM content_progress WHERE status = :status AND userId = :userId ORDER BY lastInteractionAt DESC")
    suspend fun getProgressByStatus(status: String, userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get progress by status as Flow
     */
    @Query("SELECT * FROM content_progress WHERE status = :status AND userId = :userId ORDER BY lastInteractionAt DESC")
    fun getProgressByStatusFlow(status: String, userId: String = "default_user"): Flow<List<ContentProgress>>
    
    /**
     * Get completed content progress
     */
    @Query("SELECT * FROM content_progress WHERE status IN ('completed', 'mastered') AND userId = :userId ORDER BY completedAt DESC")
    suspend fun getCompletedProgress(userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get completed content progress as Flow
     */
    @Query("SELECT * FROM content_progress WHERE status IN ('completed', 'mastered') AND userId = :userId ORDER BY completedAt DESC")
    fun getCompletedProgressFlow(userId: String = "default_user"): Flow<List<ContentProgress>>
    
    /**
     * Get mastered content progress
     */
    @Query("SELECT * FROM content_progress WHERE status = 'mastered' AND userId = :userId ORDER BY masteredAt DESC")
    suspend fun getMasteredProgress(userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get mastered content progress as Flow
     */
    @Query("SELECT * FROM content_progress WHERE status = 'mastered' AND userId = :userId ORDER BY masteredAt DESC")
    fun getMasteredProgressFlow(userId: String = "default_user"): Flow<List<ContentProgress>>
    
    /**
     * Get content due for review (spaced repetition)
     */
    @Query("SELECT * FROM content_progress WHERE nextReviewDate <= :currentTime AND userId = :userId ORDER BY nextReviewDate ASC")
    suspend fun getContentDueForReview(currentTime: Long = System.currentTimeMillis(), userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get content due for review as Flow
     */
    @Query("SELECT * FROM content_progress WHERE nextReviewDate <= :currentTime AND userId = :userId ORDER BY nextReviewDate ASC")
    fun getContentDueForReviewFlow(currentTime: Long = System.currentTimeMillis(), userId: String = "default_user"): Flow<List<ContentProgress>>
    
    /**
     * Get progress with high engagement scores
     */
    @Query("SELECT * FROM content_progress WHERE engagementScore >= :minScore AND userId = :userId ORDER BY engagementScore DESC")
    suspend fun getHighEngagementProgress(minScore: Float = 0.8f, userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get progress with low engagement scores
     */
    @Query("SELECT * FROM content_progress WHERE engagementScore < :maxScore AND attemptCount > 0 AND userId = :userId ORDER BY engagementScore ASC")
    suspend fun getLowEngagementProgress(maxScore: Float = 0.5f, userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get recent progress (last N days)
     */
    @Query("SELECT * FROM content_progress WHERE lastInteractionAt >= :since AND userId = :userId ORDER BY lastInteractionAt DESC")
    suspend fun getRecentProgress(since: Long, userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get recent progress as Flow
     */
    @Query("SELECT * FROM content_progress WHERE lastInteractionAt >= :since AND userId = :userId ORDER BY lastInteractionAt DESC")
    fun getRecentProgressFlow(since: Long, userId: String = "default_user"): Flow<List<ContentProgress>>
    
    /**
     * Get progress statistics by content type
     */
    @Query("""
        SELECT contentType, 
               COUNT(*) as total,
               SUM(CASE WHEN status = 'completed' OR status = 'mastered' THEN 1 ELSE 0 END) as completed,
               SUM(CASE WHEN status = 'mastered' THEN 1 ELSE 0 END) as mastered,
               AVG(completionPercentage) as avgCompletion,
               AVG(bestAccuracy) as avgAccuracy,
               AVG(engagementScore) as avgEngagement
        FROM content_progress 
        WHERE userId = :userId 
        GROUP BY contentType
    """)
    suspend fun getProgressStatsByContentType(userId: String = "default_user"): List<ContentTypeStats>
    
    /**
     * Get overall progress statistics
     */
    @Query("""
        SELECT COUNT(*) as totalContent,
               SUM(CASE WHEN status = 'completed' OR status = 'mastered' THEN 1 ELSE 0 END) as completedContent,
               SUM(CASE WHEN status = 'mastered' THEN 1 ELSE 0 END) as masteredContent,
               AVG(completionPercentage) as avgCompletion,
               AVG(bestAccuracy) as avgAccuracy,
               AVG(engagementScore) as avgEngagement,
               SUM(totalTimeSpent) as totalTimeSpent,
               SUM(attemptCount) as totalAttempts
        FROM content_progress 
        WHERE userId = :userId
    """)
    suspend fun getOverallProgressStats(userId: String = "default_user"): OverallProgressStats?
    
    // ==================== Insert Operations ====================
    
    /**
     * Insert a single content progress
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentProgress(progress: ContentProgress): Long
    
    /**
     * Insert multiple content progress entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentProgress(progressList: List<ContentProgress>): List<Long>
    
    // ==================== Update Operations ====================
    
    /**
     * Update content progress
     */
    @Update
    suspend fun updateContentProgress(progress: ContentProgress): Int
    
    /**
     * Update multiple content progress entries
     */
    @Update
    suspend fun updateContentProgress(progressList: List<ContentProgress>): Int
    
    /**
     * Update progress status
     */
    @Query("UPDATE content_progress SET status = :status, updatedAt = :updatedAt WHERE id = :progressId")
    suspend fun updateProgressStatus(progressId: String, status: String, updatedAt: Long = System.currentTimeMillis()): Int
    
    /**
     * Update completion percentage
     */
    @Query("UPDATE content_progress SET completionPercentage = :percentage, updatedAt = :updatedAt WHERE id = :progressId")
    suspend fun updateCompletionPercentage(progressId: String, percentage: Float, updatedAt: Long = System.currentTimeMillis()): Int
    
    /**
     * Update spaced repetition data
     */
    @Query("""
        UPDATE content_progress 
        SET nextReviewDate = :nextReviewDate, repetitionInterval = :interval, retentionScore = :retentionScore, updatedAt = :updatedAt 
        WHERE id = :progressId
    """)
    suspend fun updateSpacedRepetitionData(
        progressId: String, 
        nextReviewDate: Long, 
        interval: Int, 
        retentionScore: Float, 
        updatedAt: Long = System.currentTimeMillis()
    ): Int
    
    // ==================== Delete Operations ====================
    
    /**
     * Delete content progress by ID
     */
    @Query("DELETE FROM content_progress WHERE id = :progressId")
    suspend fun deleteContentProgressById(progressId: String): Int
    
    /**
     * Delete content progress entity
     */
    @Delete
    suspend fun deleteContentProgress(progress: ContentProgress): Int
    
    /**
     * Delete multiple content progress entries
     */
    @Delete
    suspend fun deleteContentProgress(progressList: List<ContentProgress>): Int
    
    /**
     * Delete all content progress (for testing/reset)
     */
    @Query("DELETE FROM content_progress")
    suspend fun deleteAllContentProgress(): Int
    
    /**
     * Delete progress by content type
     */
    @Query("DELETE FROM content_progress WHERE contentType = :contentType AND userId = :userId")
    suspend fun deleteProgressByContentType(contentType: String, userId: String = "default_user"): Int
    
    /**
     * Delete progress by user
     */
    @Query("DELETE FROM content_progress WHERE userId = :userId")
    suspend fun deleteProgressByUser(userId: String): Int
    
    // ==================== Utility Operations ====================
    
    /**
     * Check if content progress exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM content_progress WHERE contentId = :contentId AND userId = :userId)")
    suspend fun contentProgressExists(contentId: String, userId: String = "default_user"): Boolean
    
    /**
     * Get total content progress count
     */
    @Query("SELECT COUNT(*) FROM content_progress WHERE userId = :userId")
    suspend fun getTotalProgressCount(userId: String = "default_user"): Int
    
    /**
     * Get progress count by status
     */
    @Query("SELECT COUNT(*) FROM content_progress WHERE status = :status AND userId = :userId")
    suspend fun getProgressCountByStatus(status: String, userId: String = "default_user"): Int
    
    /**
     * Get progress created after timestamp
     */
    @Query("SELECT * FROM content_progress WHERE createdAt > :timestamp AND userId = :userId ORDER BY createdAt DESC")
    suspend fun getProgressCreatedAfter(timestamp: Long, userId: String = "default_user"): List<ContentProgress>
    
    /**
     * Get progress updated after timestamp
     */
    @Query("SELECT * FROM content_progress WHERE updatedAt > :timestamp AND userId = :userId ORDER BY updatedAt DESC")
    suspend fun getProgressUpdatedAfter(timestamp: Long, userId: String = "default_user"): List<ContentProgress>
}

/**
 * Data class for content type statistics
 */
data class ContentTypeStats(
    val contentType: String,
    val total: Int,
    val completed: Int,
    val mastered: Int,
    val avgCompletion: Float,
    val avgAccuracy: Float,
    val avgEngagement: Float
)

/**
 * Data class for overall progress statistics
 */
data class OverallProgressStats(
    val totalContent: Int,
    val completedContent: Int,
    val masteredContent: Int,
    val avgCompletion: Float,
    val avgAccuracy: Float,
    val avgEngagement: Float,
    val totalTimeSpent: Long,
    val totalAttempts: Int
)
