package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.data.TracingProgressDao
import com.happykid.toddlerabc.model.TracingProgress
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Tracing Progress data operations
 * Phase 8: Provides a clean API for tracing progress tracking and analytics
 *
 * Handles tracing session management, progress analytics, accuracy tracking,
 * and performance metrics with optimized performance for Windows emulator.
 */
@Singleton
class TracingProgressRepository @Inject constructor(
    private val tracingProgressDao: TracingProgressDao
) {

    /**
     * Get all tracing progress sessions as Flow for reactive UI
     */
    fun getAllTracingProgressFlow(): Flow<List<TracingProgress>> =
        tracingProgressDao.getAllTracingSessions()

    /**
     * Get tracing progress for a specific letter
     */
    fun getProgressForLetter(letter: Char): Flow<List<TracingProgress>> =
        tracingProgressDao.getTracingSessionsForLetter(letter)

    /**
     * Get tracing progress for a specific letter as list
     */
    suspend fun getProgressForLetterList(letter: Char): List<TracingProgress> =
        tracingProgressDao.getRecentSessionsForLetter(letter)

    /**
     * Record a new tracing session
     */
    suspend fun recordTracingSession(tracingProgress: TracingProgress): Long =
        tracingProgressDao.insertTracingSession(tracingProgress)

    /**
     * Record multiple tracing sessions
     */
    suspend fun recordTracingSessions(sessions: List<TracingProgress>) =
        tracingProgressDao.insertTracingSessions(sessions)

    /**
     * Update an existing tracing session
     */
    suspend fun updateTracingSession(tracingProgress: TracingProgress) =
        tracingProgressDao.updateTracingSession(tracingProgress)

    /**
     * Delete a specific tracing session
     */
    suspend fun deleteTracingSession(tracingProgress: TracingProgress) =
        tracingProgressDao.deleteTracingSession(tracingProgress)

    /**
     * Get recent tracing sessions
     */
    suspend fun getRecentSessions(hoursAgo: Int = 24): List<TracingProgress> {
        val since = System.currentTimeMillis() - (hoursAgo * 60 * 60 * 1000L)
        val endTime = System.currentTimeMillis()
        return tracingProgressDao.getSessionsInDateRange(since, endTime)
    }

    /**
     * Get tracing sessions for a specific letter
     */
    suspend fun getSessionsForLetter(letter: Char): List<TracingProgress> =
        tracingProgressDao.getRecentSessionsForLetter(letter)

    /**
     * Get successful tracing sessions
     */
    suspend fun getSuccessfulSessions(): List<TracingProgress> =
        tracingProgressDao.getRecentSessionsForAnalysis().filter { it.isSuccessful }

    /**
     * Get sessions that need improvement
     */
    suspend fun getSessionsNeedingImprovement(): List<TracingProgress> =
        tracingProgressDao.getRecentSessionsForAnalysis().filter { !it.isSuccessful && it.accuracyPercentage < 60f }

    /**
     * Get latest session for each letter
     */
    suspend fun getLatestSessionPerLetter(): List<TracingProgress> =
        tracingProgressDao.getRecentSessionsForAnalysis()

    /**
     * Get average accuracy for a letter
     */
    suspend fun getAverageAccuracyForLetter(letter: Char): Float =
        tracingProgressDao.getAverageAccuracyForLetter(letter) ?: 0f

    /**
     * Get overall average accuracy
     */
    suspend fun getOverallAverageAccuracy(): Float =
        tracingProgressDao.getOverallAverageAccuracy() ?: 0f

    /**
     * Get total session count
     */
    suspend fun getTotalSessionCount(): Int = tracingProgressDao.getTotalSessionCount()

    /**
     * Get session count for a specific letter
     */
    suspend fun getSessionCountForLetter(letter: Char): Int =
        tracingProgressDao.getSessionCountForLetter(letter)

    /**
     * Get successful session count for a specific letter
     */
    suspend fun getSuccessfulSessionCountForLetter(letter: Char): Int =
        tracingProgressDao.getSuccessfulSessionCountForLetter(letter)

    /**
     * Get current success streak
     */
    suspend fun getCurrentSuccessStreak(): Int = tracingProgressDao.getCurrentSuccessStreak()

    /**
     * Get best success streak
     */
    suspend fun getBestSuccessStreak(): Int = getCurrentSuccessStreak() // Simplified for now

    /**
     * Get total practice time in milliseconds
     */
    suspend fun getTotalPracticeTime(): Long = tracingProgressDao.getTotalPracticeTime() ?: 0L

    /**
     * Get average session duration in milliseconds
     */
    suspend fun getAverageSessionDuration(): Long {
        val totalTime = getTotalPracticeTime()
        val totalSessions = getTotalSessionCount()
        return if (totalSessions > 0) totalTime / totalSessions else 0L
    }

    /**
     * Get average session duration in minutes
     */
    suspend fun getAverageSessionDurationMinutes(): Float {
        val avgMs = getAverageSessionDuration()
        return avgMs / 60000f // Convert to minutes
    }

    /**
     * Get letters that have been practiced
     */
    suspend fun getPracticedLetters(): List<Char> = tracingProgressDao.getPracticedLetters()

    /**
     * Get letters with high accuracy (>= 75%)
     */
    suspend fun getHighAccuracyLetters(): List<Char> = tracingProgressDao.getHighAccuracyLetters()

    /**
     * Get letters ordered by practice frequency
     */
    suspend fun getLettersByPracticeFrequency() = tracingProgressDao.getLettersByPracticeFrequency()

    /**
     * Get letters ordered by average accuracy (most challenging first)
     */
    suspend fun getLettersByAverageAccuracy() = tracingProgressDao.getLettersByAverageAccuracy()

    /**
     * Get recent improvement trend
     */
    suspend fun getRecentImprovementTrend(): Float {
        val recentSessions = tracingProgressDao.getRecentSessionsForAnalysis()
        if (recentSessions.size < 2) return 0f

        val firstHalf = recentSessions.takeLast(recentSessions.size / 2)
        val secondHalf = recentSessions.take(recentSessions.size / 2)

        val firstAvg = firstHalf.map { it.accuracyPercentage }.average().toFloat()
        val secondAvg = secondHalf.map { it.accuracyPercentage }.average().toFloat()

        return secondAvg - firstAvg
    }

    /**
     * Check if this is the first session for a letter
     */
    suspend fun isFirstSessionForLetter(letter: Char): Boolean =
        tracingProgressDao.getSessionCountForLetter(letter) == 0

    /**
     * Get performance metrics for Windows emulator optimization
     */
    suspend fun getPerformanceMetrics(hoursAgo: Int = 24) =
        tracingProgressDao.getPerformanceMetrics(
            System.currentTimeMillis() - (hoursAgo * 60 * 60 * 1000L)
        )

    /**
     * Reset all tracing progress
     */
    suspend fun resetAllProgress() = tracingProgressDao.deleteAllTracingProgress()

    /**
     * Reset progress for specific letter
     */
    suspend fun resetProgressForLetter(letter: Char) =
        tracingProgressDao.deleteTracingProgressForLetter(letter)

    /**
     * Clean up old sessions (older than specified days)
     */
    suspend fun cleanupOldSessions(daysOld: Int = 90) {
        // For now, we'll implement this as a simple delete all old sessions
        // In a real implementation, we'd add a specific DAO method for this
        val cutoffTimestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        val oldSessions = tracingProgressDao.getSessionsInDateRange(0, cutoffTimestamp)
        oldSessions.forEach { session ->
            tracingProgressDao.deleteTracingSession(session)
        }
    }

    /**
     * Get comprehensive tracing statistics
     */
    suspend fun getTracingStats(): TracingStats {
        val totalSessions = getTotalSessionCount()
        val totalPracticeTime = getTotalPracticeTime()
        val averageAccuracy = getOverallAverageAccuracy()
        val practicedLetters = getPracticedLetters()
        val highAccuracyLetters = getHighAccuracyLetters()
        val currentStreak = getCurrentSuccessStreak()
        val bestStreak = getBestSuccessStreak()
        val recentImprovement = getRecentImprovementTrend()

        return TracingStats(
            totalLettersTraced = practicedLetters.size,
            totalTracingSessions = totalSessions,
            totalPracticeTimeMs = totalPracticeTime,
            averageAccuracyAllLetters = averageAccuracy,
            lettersStarted = practicedLetters.size,
            lettersMastered = highAccuracyLetters.size,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            recentImprovement = recentImprovement
        )
    }
}

/**
 * Data class for tracing statistics
 */
data class TracingStats(
    val totalLettersTraced: Int,
    val totalTracingSessions: Int,
    val totalPracticeTimeMs: Long,
    val averageAccuracyAllLetters: Float,
    val lettersStarted: Int,
    val lettersMastered: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val recentImprovement: Float
)
