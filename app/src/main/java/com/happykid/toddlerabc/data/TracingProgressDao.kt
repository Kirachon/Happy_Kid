package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.TracingProgress
import com.happykid.toddlerabc.model.TracingSessionSummary
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for TracingProgress entity
 * Phase 8: Provides database operations for tracing progress tracking
 *
 * Handles detailed tracing metrics, session analysis, and progress reporting
 * with optimized queries for Windows emulator performance.
 */
@Dao
interface TracingProgressDao {

    /**
     * Insert a new tracing session
     */
    @Insert
    suspend fun insertTracingSession(tracingProgress: TracingProgress): Long

    /**
     * Insert multiple tracing sessions
     */
    @Insert
    suspend fun insertTracingSessions(sessions: List<TracingProgress>)

    /**
     * Update an existing tracing session
     */
    @Update
    suspend fun updateTracingSession(tracingProgress: TracingProgress)

    /**
     * Delete a specific tracing session
     */
    @Delete
    suspend fun deleteTracingSession(tracingProgress: TracingProgress)

    /**
     * Get all tracing sessions for a specific letter
     */
    @Query("SELECT * FROM tracing_progress WHERE character = :character ORDER BY sessionTimestamp DESC")
    fun getTracingSessionsForLetter(character: Char): Flow<List<TracingProgress>>

    /**
     * Get recent tracing sessions (last 10) for a letter
     */
    @Query("SELECT * FROM tracing_progress WHERE character = :character ORDER BY sessionTimestamp DESC LIMIT 10")
    suspend fun getRecentSessionsForLetter(character: Char): List<TracingProgress>

    /**
     * Get all tracing sessions ordered by timestamp
     */
    @Query("SELECT * FROM tracing_progress ORDER BY sessionTimestamp DESC")
    fun getAllTracingSessions(): Flow<List<TracingProgress>>

    /**
     * Get successful tracing sessions for a letter
     */
    @Query("SELECT * FROM tracing_progress WHERE character = :character AND isSuccessful = 1 ORDER BY sessionTimestamp DESC")
    fun getSuccessfulSessionsForLetter(character: Char): Flow<List<TracingProgress>>

    /**
     * Get tracing sessions by difficulty level
     */
    @Query("SELECT * FROM tracing_progress WHERE difficultyLevel = :level ORDER BY sessionTimestamp DESC")
    fun getSessionsByDifficulty(level: Int): Flow<List<TracingProgress>>

    /**
     * Get best accuracy for a letter
     */
    @Query("SELECT MAX(accuracyPercentage) FROM tracing_progress WHERE character = :character")
    suspend fun getBestAccuracyForLetter(character: Char): Float?

    /**
     * Get average accuracy for a letter
     */
    @Query("SELECT AVG(accuracyPercentage) FROM tracing_progress WHERE character = :character")
    suspend fun getAverageAccuracyForLetter(character: Char): Float?

    /**
     * Get total practice time for a letter
     */
    @Query("SELECT SUM(sessionDurationMs) FROM tracing_progress WHERE character = :character")
    suspend fun getTotalPracticeTimeForLetter(character: Char): Long?

    /**
     * Get session count for a letter
     */
    @Query("SELECT COUNT(*) FROM tracing_progress WHERE character = :character")
    suspend fun getSessionCountForLetter(character: Char): Int

    /**
     * Get successful session count for a letter
     */
    @Query("SELECT COUNT(*) FROM tracing_progress WHERE character = :character AND isSuccessful = 1")
    suspend fun getSuccessfulSessionCountForLetter(character: Char): Int

    /**
     * Get current difficulty level for a letter (most recent session)
     */
    @Query("SELECT difficultyLevel FROM tracing_progress WHERE character = :character ORDER BY sessionTimestamp DESC LIMIT 1")
    suspend fun getCurrentDifficultyForLetter(character: Char): Int?

    /**
     * Get letters that have been practiced (have tracing sessions)
     */
    @Query("SELECT DISTINCT character FROM tracing_progress ORDER BY character ASC")
    suspend fun getPracticedLetters(): List<Char>

    /**
     * Get letters with high accuracy (>= 80%)
     */
    @Query("SELECT DISTINCT character FROM tracing_progress WHERE accuracyPercentage >= 80.0 ORDER BY character ASC")
    suspend fun getHighAccuracyLetters(): List<Char>

    /**
     * Get total number of tracing sessions
     */
    @Query("SELECT COUNT(*) FROM tracing_progress")
    suspend fun getTotalSessionCount(): Int

    /**
     * Get total practice time across all letters
     */
    @Query("SELECT SUM(sessionDurationMs) FROM tracing_progress")
    suspend fun getTotalPracticeTime(): Long?

    /**
     * Get overall average accuracy across all letters
     */
    @Query("SELECT AVG(accuracyPercentage) FROM tracing_progress")
    suspend fun getOverallAverageAccuracy(): Float?

    /**
     * Get recent sessions for improvement analysis (last 10 sessions)
     */
    @Query("SELECT * FROM tracing_progress ORDER BY sessionTimestamp DESC LIMIT 10")
    suspend fun getRecentSessionsForAnalysis(): List<TracingProgress>

    /**
     * Get sessions within a date range
     */
    @Query("SELECT * FROM tracing_progress WHERE sessionTimestamp BETWEEN :startTime AND :endTime ORDER BY sessionTimestamp DESC")
    suspend fun getSessionsInDateRange(startTime: Long, endTime: Long): List<TracingProgress>

    /**
     * Get milestones achieved for a letter
     */
    @Query("SELECT milestoneAchieved FROM tracing_progress WHERE character = :character AND milestoneAchieved IS NOT NULL ORDER BY sessionTimestamp DESC")
    suspend fun getMilestonesForLetter(character: Char): List<String>

    /**
     * Get letters ordered by practice frequency (most practiced first)
     */
    @Query("SELECT character, COUNT(*) as sessionCount FROM tracing_progress GROUP BY character ORDER BY sessionCount DESC")
    suspend fun getLettersByPracticeFrequency(): List<LetterPracticeCount>

    /**
     * Get letters ordered by average accuracy (lowest first - most challenging)
     */
    @Query("SELECT character, AVG(accuracyPercentage) as avgAccuracy FROM tracing_progress GROUP BY character ORDER BY avgAccuracy ASC")
    suspend fun getLettersByAverageAccuracy(): List<LetterAccuracyAverage>

    /**
     * Get current streak of successful sessions
     */
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT isSuccessful FROM tracing_progress 
            ORDER BY sessionTimestamp DESC
        ) WHERE isSuccessful = 1
    """)
    suspend fun getCurrentSuccessStreak(): Int

    /**
     * Check if this is the first completion for a letter
     */
    @Query("SELECT COUNT(*) FROM tracing_progress WHERE character = :character AND isCompleted = 1")
    suspend fun getCompletionCountForLetter(character: Char): Int

    /**
     * Delete all tracing progress (for reset functionality)
     */
    @Query("DELETE FROM tracing_progress")
    suspend fun deleteAllTracingProgress()

    /**
     * Delete tracing progress for a specific letter
     */
    @Query("DELETE FROM tracing_progress WHERE character = :character")
    suspend fun deleteTracingProgressForLetter(character: Char)

    /**
     * Get performance metrics for Windows emulator optimization
     */
    @Query("SELECT AVG(frameRate) as avgFrameRate, AVG(touchLatencyMs) as avgTouchLatency, AVG(renderingPerformance) as avgRenderingPerf FROM tracing_progress WHERE sessionTimestamp > :since")
    suspend fun getPerformanceMetrics(since: Long): TracingPerformanceMetrics?
}

/**
 * Data class for letter practice count query result
 */
data class LetterPracticeCount(
    val character: Char,
    val sessionCount: Int
)

/**
 * Data class for letter accuracy average query result
 */
data class LetterAccuracyAverage(
    val character: Char,
    val avgAccuracy: Float
)

/**
 * Data class for performance metrics query result
 */
data class TracingPerformanceMetrics(
    val avgFrameRate: Float,
    val avgTouchLatency: Float,
    val avgRenderingPerf: Float
)
