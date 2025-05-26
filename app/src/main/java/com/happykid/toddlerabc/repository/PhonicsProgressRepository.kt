package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.data.PhonicsProgressDao
import com.happykid.toddlerabc.model.PhonicsProgress
import com.happykid.toddlerabc.model.PhonicsActivityType
import com.happykid.toddlerabc.model.WordCategory
import com.happykid.toddlerabc.model.PhonicsStats
import com.happykid.toddlerabc.model.ReadingLevel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Phonics Progress data operations
 * Phase 9: Provides a clean API for phonics progress tracking and analytics
 *
 * Handles phonics learning session management, progress analytics, milestone tracking,
 * and reading comprehension metrics with optimized performance for Windows emulator.
 */
@Singleton
class PhonicsProgressRepository @Inject constructor(
    private val phonicsProgressDao: PhonicsProgressDao
) {

    /**
     * Get all phonics progress sessions as Flow for reactive UI
     */
    fun getAllPhonicsProgressFlow(): Flow<List<PhonicsProgress>> = 
        phonicsProgressDao.getAllPhonicsProgressFlow()

    /**
     * Get phonics progress for a specific word
     */
    fun getProgressForWord(wordId: Long): Flow<List<PhonicsProgress>> = 
        phonicsProgressDao.getProgressForWord(wordId)

    /**
     * Get phonics progress for a specific word as list
     */
    suspend fun getProgressForWordList(wordId: Long): List<PhonicsProgress> = 
        phonicsProgressDao.getProgressForWordList(wordId)

    /**
     * Get recent phonics sessions
     */
    suspend fun getRecentSessions(hoursAgo: Int = 24): List<PhonicsProgress> {
        val since = System.currentTimeMillis() - (hoursAgo * 60 * 60 * 1000L)
        return phonicsProgressDao.getRecentSessions(since)
    }

    /**
     * Get sessions by activity type
     */
    fun getSessionsByActivity(activityType: PhonicsActivityType): Flow<List<PhonicsProgress>> = 
        phonicsProgressDao.getSessionsByActivity(activityType)

    /**
     * Get sessions by word category
     */
    fun getSessionsByCategory(category: WordCategory): Flow<List<PhonicsProgress>> = 
        phonicsProgressDao.getSessionsByCategory(category)

    /**
     * Record a new phonics session
     */
    suspend fun recordPhonicsSession(progress: PhonicsProgress): Long = 
        phonicsProgressDao.insertPhonicsSession(progress)

    /**
     * Update an existing phonics session
     */
    suspend fun updatePhonicsSession(progress: PhonicsProgress) = 
        phonicsProgressDao.updatePhonicsSession(progress)

    /**
     * Get successful sessions
     */
    suspend fun getSuccessfulSessions(): List<PhonicsProgress> = 
        phonicsProgressDao.getSuccessfulSessions()

    /**
     * Get sessions with milestones achieved
     */
    suspend fun getSessionsWithMilestones(): List<PhonicsProgress> = 
        phonicsProgressDao.getSessionsWithMilestones()

    /**
     * Get latest session for each word
     */
    suspend fun getLatestSessionPerWord(): List<PhonicsProgress> = 
        phonicsProgressDao.getLatestSessionPerWord()

    /**
     * Get average accuracy for a word
     */
    suspend fun getAverageAccuracyForWord(wordId: Long): Float = 
        phonicsProgressDao.getAverageAccuracyForWord(wordId) ?: 0f

    /**
     * Get overall average accuracy
     */
    suspend fun getOverallAverageAccuracy(): Float = 
        phonicsProgressDao.getOverallAverageAccuracy() ?: 0f

    /**
     * Get comprehensive phonics statistics
     */
    suspend fun getPhonicsStats(): PhonicsStats {
        val totalSessions = phonicsProgressDao.getTotalSessionCount()
        val totalPracticeTime = phonicsProgressDao.getTotalPracticeTime() ?: 0L
        val averageAccuracy = getOverallAverageAccuracy()
        val practicedWordIds = phonicsProgressDao.getPracticedWordIds()
        val highAccuracyWordIds = phonicsProgressDao.getHighAccuracyWordIds()
        val currentStreak = phonicsProgressDao.getCurrentSuccessStreak()
        val bestStreak = phonicsProgressDao.getBestSuccessStreak() ?: 0
        val recentImprovement = phonicsProgressDao.getRecentImprovementTrend() ?: 0f
        
        // Determine reading level based on progress
        val readingLevel = determineReadingLevel(averageAccuracy, highAccuracyWordIds.size, totalSessions)
        
        return PhonicsStats(
            totalPhonicsWords = practicedWordIds.size,
            totalReadingSessions = totalSessions,
            totalPracticeTimeMs = totalPracticeTime,
            averageAccuracyAllWords = averageAccuracy,
            wordsStarted = practicedWordIds.size,
            wordsMastered = highAccuracyWordIds.size,
            sightWordsMastered = getSightWordsMastered(),
            cvcWordsMastered = getCVCWordsMastered(),
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            favoriteWordFamilies = getFavoriteWordFamilies(),
            challengingWords = getChallengingWords(),
            recentImprovement = recentImprovement,
            readingLevel = readingLevel
        )
    }

    /**
     * Get words that have been practiced
     */
    suspend fun getPracticedWordIds(): List<Long> = phonicsProgressDao.getPracticedWordIds()

    /**
     * Get words with high accuracy
     */
    suspend fun getHighAccuracyWordIds(): List<Long> = phonicsProgressDao.getHighAccuracyWordIds()

    /**
     * Get session count for a specific word
     */
    suspend fun getSessionCountForWord(wordId: Long): Int = 
        phonicsProgressDao.getSessionCountForWord(wordId)

    /**
     * Get successful session count for a specific word
     */
    suspend fun getSuccessfulSessionCountForWord(wordId: Long): Int = 
        phonicsProgressDao.getSuccessfulSessionCountForWord(wordId)

    /**
     * Get current success streak
     */
    suspend fun getCurrentSuccessStreak(): Int = phonicsProgressDao.getCurrentSuccessStreak()

    /**
     * Get best success streak
     */
    suspend fun getBestSuccessStreak(): Int = phonicsProgressDao.getBestSuccessStreak() ?: 0

    /**
     * Get average session duration in minutes
     */
    suspend fun getAverageSessionDurationMinutes(): Float {
        val avgMs = phonicsProgressDao.getAverageSessionDuration() ?: 0L
        return avgMs / 60000f // Convert to minutes
    }

    /**
     * Get sessions requiring assistance count
     */
    suspend fun getSessionsRequiringAssistance(): Int = 
        phonicsProgressDao.getSessionsRequiringAssistance()

    /**
     * Get average engagement score
     */
    suspend fun getAverageEngagementScore(): Float = 
        phonicsProgressDao.getAverageEngagementScore() ?: 0f

    /**
     * Get milestones achieved for a word
     */
    suspend fun getMilestonesForWord(wordId: Long): List<String> = 
        phonicsProgressDao.getMilestonesForWord(wordId)

    /**
     * Get words ordered by practice frequency
     */
    suspend fun getWordsByPracticeFrequency() = phonicsProgressDao.getWordsByPracticeFrequency()

    /**
     * Get words ordered by average accuracy (most challenging first)
     */
    suspend fun getWordsByAverageAccuracy() = phonicsProgressDao.getWordsByAverageAccuracy()

    /**
     * Get activity type performance
     */
    suspend fun getActivityTypePerformance() = phonicsProgressDao.getActivityTypePerformance()

    /**
     * Get recent improvement trend
     */
    suspend fun getRecentImprovementTrend(): Float = 
        phonicsProgressDao.getRecentImprovementTrend() ?: 0f

    /**
     * Check if this is the first session for a word
     */
    suspend fun isFirstSessionForWord(wordId: Long): Boolean = 
        phonicsProgressDao.getSessionCountForWordCheck(wordId) == 0

    /**
     * Get performance metrics for Windows emulator optimization
     */
    suspend fun getPerformanceMetrics(hoursAgo: Int = 24) = 
        phonicsProgressDao.getPerformanceMetrics(
            System.currentTimeMillis() - (hoursAgo * 60 * 60 * 1000L)
        )

    /**
     * Reset all phonics progress
     */
    suspend fun resetAllProgress() = phonicsProgressDao.deleteAllPhonicsProgress()

    /**
     * Reset progress for specific word
     */
    suspend fun resetProgressForWord(wordId: Long) = 
        phonicsProgressDao.deletePhonicsProgressForWord(wordId)

    /**
     * Clean up old sessions (older than specified days)
     */
    suspend fun cleanupOldSessions(daysOld: Int = 90) {
        val cutoffTimestamp = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
        phonicsProgressDao.deleteOldSessions(cutoffTimestamp)
    }

    /**
     * Create a successful phonics session record
     */
    suspend fun recordSuccessfulSession(
        wordId: Long,
        word: String,
        category: WordCategory,
        activityType: PhonicsActivityType,
        recognitionAccuracy: Float = 85f,
        sessionDurationMs: Long = 30000L
    ): Long {
        val session = PhonicsProgress.createSuccessfulSession(
            wordId, word, category, activityType, recognitionAccuracy, sessionDurationMs
        )
        return recordPhonicsSession(session)
    }

    /**
     * Create an assisted learning session record
     */
    suspend fun recordAssistedSession(
        wordId: Long,
        word: String,
        category: WordCategory,
        activityType: PhonicsActivityType,
        hintsUsed: Int = 2
    ): Long {
        val session = PhonicsProgress.createAssistedSession(
            wordId, word, category, activityType, hintsUsed
        )
        return recordPhonicsSession(session)
    }

    // Private helper methods

    private suspend fun getSightWordsMastered(): Int {
        // This would require joining with phonics_words table
        // For now, return estimated count based on high accuracy words
        return (getHighAccuracyWordIds().size * 0.3).toInt() // Estimate 30% are sight words
    }

    private suspend fun getCVCWordsMastered(): Int {
        // This would require joining with phonics_words table
        // For now, return estimated count based on high accuracy words
        return (getHighAccuracyWordIds().size * 0.7).toInt() // Estimate 70% are CVC words
    }

    private suspend fun getFavoriteWordFamilies(): List<String> {
        // Extract word families from most practiced words
        val wordsByFrequency = getWordsByPracticeFrequency()
        return wordsByFrequency.take(5).map { it.word.takeLast(2) }.distinct()
    }

    private suspend fun getChallengingWords(): List<String> {
        val wordsByAccuracy = getWordsByAverageAccuracy()
        return wordsByAccuracy.take(5).map { it.word }
    }

    private fun determineReadingLevel(
        averageAccuracy: Float,
        masteredWords: Int,
        totalSessions: Int
    ): ReadingLevel {
        return when {
            totalSessions < 5 || averageAccuracy < 40f -> ReadingLevel.PRE_READER
            averageAccuracy < 60f || masteredWords < 3 -> ReadingLevel.EMERGING_READER
            averageAccuracy < 80f || masteredWords < 8 -> ReadingLevel.EARLY_READER
            else -> ReadingLevel.DEVELOPING_READER
        }
    }
}
