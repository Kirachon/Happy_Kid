package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.PhonicsProgress
import com.happykid.toddlerabc.model.PhonicsActivityType
import com.happykid.toddlerabc.model.WordCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PhonicsProgress entity
 * Phase 9: Provides database operations for phonics progress tracking
 *
 * Handles detailed phonics learning metrics, session analysis, and progress reporting
 * with optimized queries for Windows emulator performance.
 */
@Dao
interface PhonicsProgressDao {

    /**
     * Insert a new phonics progress session
     */
    @Insert
    suspend fun insertPhonicsSession(progress: PhonicsProgress): Long

    /**
     * Insert multiple phonics progress sessions
     */
    @Insert
    suspend fun insertPhonicsSessions(sessions: List<PhonicsProgress>)

    /**
     * Update an existing phonics progress session
     */
    @Update
    suspend fun updatePhonicsSession(progress: PhonicsProgress)

    /**
     * Delete a phonics progress session
     */
    @Delete
    suspend fun deletePhonicsSession(progress: PhonicsProgress)

    /**
     * Get all phonics progress sessions as Flow
     */
    @Query("SELECT * FROM phonics_progress ORDER BY sessionTimestamp DESC")
    fun getAllPhonicsProgressFlow(): Flow<List<PhonicsProgress>>

    /**
     * Get phonics progress for a specific word
     */
    @Query("SELECT * FROM phonics_progress WHERE wordId = :wordId ORDER BY sessionTimestamp DESC")
    fun getProgressForWord(wordId: Long): Flow<List<PhonicsProgress>>

    /**
     * Get phonics progress for a specific word as list
     */
    @Query("SELECT * FROM phonics_progress WHERE wordId = :wordId ORDER BY sessionTimestamp DESC")
    suspend fun getProgressForWordList(wordId: Long): List<PhonicsProgress>

    /**
     * Get recent phonics sessions
     */
    @Query("SELECT * FROM phonics_progress WHERE sessionTimestamp > :since ORDER BY sessionTimestamp DESC")
    suspend fun getRecentSessions(since: Long): List<PhonicsProgress>

    /**
     * Get sessions by activity type
     */
    @Query("SELECT * FROM phonics_progress WHERE activityType = :activityType ORDER BY sessionTimestamp DESC")
    fun getSessionsByActivity(activityType: PhonicsActivityType): Flow<List<PhonicsProgress>>

    /**
     * Get sessions by word category
     */
    @Query("SELECT * FROM phonics_progress WHERE category = :category ORDER BY sessionTimestamp DESC")
    fun getSessionsByCategory(category: WordCategory): Flow<List<PhonicsProgress>>

    /**
     * Get successful sessions (overall success = true)
     */
    @Query("SELECT * FROM phonics_progress WHERE isSuccessful = 1 ORDER BY sessionTimestamp DESC")
    suspend fun getSuccessfulSessions(): List<PhonicsProgress>

    /**
     * Get sessions with milestones achieved
     */
    @Query("SELECT * FROM phonics_progress WHERE milestoneAchieved IS NOT NULL ORDER BY sessionTimestamp DESC")
    suspend fun getSessionsWithMilestones(): List<PhonicsProgress>

    /**
     * Get latest session for each word
     */
    @Query("""
        SELECT * FROM phonics_progress
        WHERE id IN (
            SELECT id FROM phonics_progress p1
            WHERE sessionTimestamp = (
                SELECT MAX(sessionTimestamp)
                FROM phonics_progress p2
                WHERE p2.wordId = p1.wordId
            )
        )
        ORDER BY word ASC
    """)
    suspend fun getLatestSessionPerWord(): List<PhonicsProgress>

    /**
     * Get average accuracy for a word
     */
    @Query("SELECT AVG(recognitionAccuracy) FROM phonics_progress WHERE wordId = :wordId")
    suspend fun getAverageAccuracyForWord(wordId: Long): Float?

    /**
     * Get average accuracy across all words
     */
    @Query("SELECT AVG(recognitionAccuracy) FROM phonics_progress")
    suspend fun getOverallAverageAccuracy(): Float?

    /**
     * Get total number of phonics sessions
     */
    @Query("SELECT COUNT(*) FROM phonics_progress")
    suspend fun getTotalSessionCount(): Int

    /**
     * Get total practice time across all sessions
     */
    @Query("SELECT SUM(sessionDurationMs) FROM phonics_progress")
    suspend fun getTotalPracticeTime(): Long?

    /**
     * Get words that have been practiced (have sessions)
     */
    @Query("SELECT DISTINCT wordId FROM phonics_progress ORDER BY wordId ASC")
    suspend fun getPracticedWordIds(): List<Long>

    /**
     * Get words with high accuracy (>= 80%)
     */
    @Query("SELECT DISTINCT wordId FROM phonics_progress WHERE recognitionAccuracy >= 80.0 ORDER BY wordId ASC")
    suspend fun getHighAccuracyWordIds(): List<Long>

    /**
     * Get session count for a specific word
     */
    @Query("SELECT COUNT(*) FROM phonics_progress WHERE wordId = :wordId")
    suspend fun getSessionCountForWord(wordId: Long): Int

    /**
     * Get successful session count for a specific word
     */
    @Query("SELECT COUNT(*) FROM phonics_progress WHERE wordId = :wordId AND isSuccessful = 1")
    suspend fun getSuccessfulSessionCountForWord(wordId: Long): Int

    /**
     * Get current success streak across all sessions
     */
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT isSuccessful FROM phonics_progress
            ORDER BY sessionTimestamp DESC
        ) WHERE isSuccessful = 1
    """)
    suspend fun getCurrentSuccessStreak(): Int

    /**
     * Get best success streak (simplified query)
     */
    @Query("SELECT COUNT(*) FROM phonics_progress WHERE isSuccessful = 1")
    suspend fun getBestSuccessStreak(): Int?

    /**
     * Get average session duration
     */
    @Query("SELECT AVG(sessionDurationMs) FROM phonics_progress WHERE sessionDurationMs > 0")
    suspend fun getAverageSessionDuration(): Long?

    /**
     * Get sessions requiring assistance
     */
    @Query("SELECT COUNT(*) FROM phonics_progress WHERE assistanceRequired = 1")
    suspend fun getSessionsRequiringAssistance(): Int

    /**
     * Get average engagement score
     */
    @Query("SELECT AVG(engagementScore) FROM phonics_progress")
    suspend fun getAverageEngagementScore(): Float?

    /**
     * Get milestones achieved for a word
     */
    @Query("SELECT milestoneAchieved FROM phonics_progress WHERE wordId = :wordId AND milestoneAchieved IS NOT NULL ORDER BY sessionTimestamp DESC")
    suspend fun getMilestonesForWord(wordId: Long): List<String>

    /**
     * Get words ordered by practice frequency (most practiced first)
     */
    @Query("SELECT wordId, word, COUNT(*) as sessionCount FROM phonics_progress GROUP BY wordId, word ORDER BY sessionCount DESC")
    suspend fun getWordsByPracticeFrequency(): List<WordPracticeCount>

    /**
     * Get words ordered by average accuracy (lowest first - most challenging)
     */
    @Query("SELECT wordId, word, AVG(recognitionAccuracy) as avgAccuracy FROM phonics_progress GROUP BY wordId, word ORDER BY avgAccuracy ASC")
    suspend fun getWordsByAverageAccuracy(): List<WordAccuracyAverage>

    /**
     * Get activity type performance
     */
    @Query("SELECT activityType, AVG(recognitionAccuracy) as avgAccuracy, COUNT(*) as sessionCount FROM phonics_progress GROUP BY activityType ORDER BY avgAccuracy DESC")
    suspend fun getActivityTypePerformance(): List<ActivityPerformance>

    /**
     * Get recent improvement trend (last 10 sessions vs previous 10)
     */
    @Query("""
        SELECT
            (SELECT AVG(recognitionAccuracy) FROM (SELECT recognitionAccuracy FROM phonics_progress ORDER BY sessionTimestamp DESC LIMIT 10)) -
            (SELECT AVG(recognitionAccuracy) FROM (SELECT recognitionAccuracy FROM phonics_progress ORDER BY sessionTimestamp DESC LIMIT 20 OFFSET 10))
        as improvement
    """)
    suspend fun getRecentImprovementTrend(): Float?

    /**
     * Check if this is the first session for a word
     */
    @Query("SELECT COUNT(*) FROM phonics_progress WHERE wordId = :wordId")
    suspend fun getSessionCountForWordCheck(wordId: Long): Int

    /**
     * Get performance metrics for Windows emulator optimization
     */
    @Query("SELECT AVG(frameRate) as avgFrameRate, AVG(touchLatencyMs) as avgTouchLatency, AVG(audioLatencyMs) as avgAudioLatency, AVG(renderingPerformance) as avgRenderingPerf FROM phonics_progress WHERE sessionTimestamp > :since")
    suspend fun getPerformanceMetrics(since: Long): PhonicsPerformanceMetrics?

    /**
     * Delete all phonics progress (for reset functionality)
     */
    @Query("DELETE FROM phonics_progress")
    suspend fun deleteAllPhonicsProgress()

    /**
     * Delete phonics progress for a specific word
     */
    @Query("DELETE FROM phonics_progress WHERE wordId = :wordId")
    suspend fun deletePhonicsProgressForWord(wordId: Long)

    /**
     * Delete old sessions (older than specified timestamp)
     */
    @Query("DELETE FROM phonics_progress WHERE sessionTimestamp < :cutoffTimestamp")
    suspend fun deleteOldSessions(cutoffTimestamp: Long)
}

/**
 * Data class for word practice count statistics
 */
data class WordPracticeCount(
    val wordId: Long,
    val word: String,
    val sessionCount: Int
)

/**
 * Data class for word accuracy average statistics
 */
data class WordAccuracyAverage(
    val wordId: Long,
    val word: String,
    val avgAccuracy: Float
)

/**
 * Data class for activity type performance statistics
 */
data class ActivityPerformance(
    val activityType: PhonicsActivityType,
    val avgAccuracy: Float,
    val sessionCount: Int
)

/**
 * Data class for phonics performance metrics
 */
data class PhonicsPerformanceMetrics(
    val avgFrameRate: Float,
    val avgTouchLatency: Float,
    val avgAudioLatency: Float,
    val avgRenderingPerf: Float
)
