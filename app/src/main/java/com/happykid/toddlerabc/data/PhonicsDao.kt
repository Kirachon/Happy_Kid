package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.PhonicsWord
import com.happykid.toddlerabc.model.WordCategory
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for PhonicsWord entity
 * Phase 9: Provides database operations for phonics words and vocabulary
 *
 * Handles phonics word management, CVC word operations, sight word tracking,
 * and word family organization with optimized queries for Windows emulator performance.
 */
@Dao
interface PhonicsDao {

    /**
     * Insert a new phonics word
     */
    @Insert
    suspend fun insertPhonicsWord(word: PhonicsWord): Long

    /**
     * Insert multiple phonics words
     */
    @Insert
    suspend fun insertPhonicsWords(words: List<PhonicsWord>)

    /**
     * Update an existing phonics word
     */
    @Update
    suspend fun updatePhonicsWord(word: PhonicsWord)

    /**
     * Delete a phonics word
     */
    @Delete
    suspend fun deletePhonicsWord(word: PhonicsWord)

    /**
     * Get all phonics words as Flow for reactive UI
     */
    @Query("SELECT * FROM phonics_words ORDER BY difficultyLevel ASC, word ASC")
    fun getAllPhonicsWordsFlow(): Flow<List<PhonicsWord>>

    /**
     * Get all phonics words as list
     */
    @Query("SELECT * FROM phonics_words ORDER BY difficultyLevel ASC, word ASC")
    suspend fun getAllPhonicsWords(): List<PhonicsWord>

    /**
     * Get a specific phonics word by ID
     */
    @Query("SELECT * FROM phonics_words WHERE id = :id")
    suspend fun getPhonicsWordById(id: Long): PhonicsWord?

    /**
     * Get a specific phonics word by word text
     */
    @Query("SELECT * FROM phonics_words WHERE word = :word LIMIT 1")
    suspend fun getPhonicsWordByText(word: String): PhonicsWord?

    /**
     * Get words by category
     */
    @Query("SELECT * FROM phonics_words WHERE category = :category ORDER BY difficultyLevel ASC, word ASC")
    fun getWordsByCategory(category: WordCategory): Flow<List<PhonicsWord>>

    /**
     * Get words by difficulty level
     */
    @Query("SELECT * FROM phonics_words WHERE difficultyLevel = :level ORDER BY word ASC")
    fun getWordsByDifficulty(level: Int): Flow<List<PhonicsWord>>

    /**
     * Get sight words
     */
    @Query("SELECT * FROM phonics_words WHERE isSightWord = 1 ORDER BY difficultyLevel ASC, word ASC")
    fun getSightWordsFlow(): Flow<List<PhonicsWord>>

    /**
     * Get high-frequency words
     */
    @Query("SELECT * FROM phonics_words WHERE isHighFrequency = 1 ORDER BY timesPresented DESC")
    fun getHighFrequencyWordsFlow(): Flow<List<PhonicsWord>>

    /**
     * Get CVC words
     */
    @Query("SELECT * FROM phonics_words WHERE category = 'CVC' ORDER BY wordFamily ASC, word ASC")
    fun getCVCWordsFlow(): Flow<List<PhonicsWord>>

    /**
     * Get words by word family
     */
    @Query("SELECT * FROM phonics_words WHERE wordFamily = :family ORDER BY word ASC")
    fun getWordsByFamily(family: String): Flow<List<PhonicsWord>>

    /**
     * Get mastered words (high accuracy with minimum attempts)
     */
    @Query("SELECT * FROM phonics_words WHERE timesPresented >= 3 AND (timesRecognized * 100.0 / timesPresented) >= 80.0 ORDER BY word ASC")
    suspend fun getMasteredWords(): List<PhonicsWord>

    /**
     * Get words that need practice (low accuracy or not attempted)
     */
    @Query("SELECT * FROM phonics_words WHERE timesPresented = 0 OR (timesRecognized * 100.0 / timesPresented) < 60.0 ORDER BY timesPresented ASC, word ASC")
    suspend fun getWordsNeedingPractice(): List<PhonicsWord>

    /**
     * Get recently practiced words
     */
    @Query("SELECT * FROM phonics_words WHERE lastPresentedTimestamp > :since ORDER BY lastPresentedTimestamp DESC")
    suspend fun getRecentlyPracticedWords(since: Long): List<PhonicsWord>

    /**
     * Record word presentation
     */
    @Query("UPDATE phonics_words SET timesPresented = timesPresented + 1, lastPresentedTimestamp = :timestamp WHERE id = :wordId")
    suspend fun recordWordPresentation(wordId: Long, timestamp: Long)

    /**
     * Record word recognition success
     */
    @Query("UPDATE phonics_words SET timesRecognized = timesRecognized + 1 WHERE id = :wordId")
    suspend fun recordWordRecognition(wordId: Long)

    /**
     * Get total number of phonics words
     */
    @Query("SELECT COUNT(*) FROM phonics_words")
    suspend fun getTotalWordCount(): Int

    /**
     * Get total number of mastered words
     */
    @Query("SELECT COUNT(*) FROM phonics_words WHERE timesPresented >= 3 AND (timesRecognized * 100.0 / timesPresented) >= 80.0")
    suspend fun getMasteredWordCount(): Int

    /**
     * Get total number of sight words mastered
     */
    @Query("SELECT COUNT(*) FROM phonics_words WHERE isSightWord = 1 AND timesPresented >= 3 AND (timesRecognized * 100.0 / timesPresented) >= 80.0")
    suspend fun getMasteredSightWordCount(): Int

    /**
     * Get total number of CVC words mastered
     */
    @Query("SELECT COUNT(*) FROM phonics_words WHERE category = 'CVC' AND timesPresented >= 3 AND (timesRecognized * 100.0 / timesPresented) >= 80.0")
    suspend fun getMasteredCVCWordCount(): Int

    /**
     * Get average recognition accuracy across all words
     */
    @Query("SELECT AVG(CASE WHEN timesPresented > 0 THEN (timesRecognized * 100.0 / timesPresented) ELSE 0 END) FROM phonics_words")
    suspend fun getAverageRecognitionAccuracy(): Float?

    /**
     * Get word families ordered by practice frequency
     */
    @Query("SELECT wordFamily, COUNT(*) as wordCount, SUM(timesPresented) as totalPractices FROM phonics_words WHERE wordFamily IS NOT NULL GROUP BY wordFamily ORDER BY totalPractices DESC")
    suspend fun getWordFamiliesByPractice(): List<WordFamilyStats>

    /**
     * Get most challenging words (lowest accuracy)
     */
    @Query("SELECT word, (timesRecognized * 100.0 / timesPresented) as accuracy FROM phonics_words WHERE timesPresented >= 3 ORDER BY accuracy ASC LIMIT :limit")
    suspend fun getMostChallengingWords(limit: Int = 5): List<WordAccuracyStats>

    /**
     * Get words by age appropriateness
     */
    @Query("SELECT * FROM phonics_words WHERE difficultyLevel <= :maxLevel ORDER BY difficultyLevel ASC, word ASC")
    suspend fun getWordsForAge(maxLevel: Int): List<PhonicsWord>

    /**
     * Search words by partial text
     */
    @Query("SELECT * FROM phonics_words WHERE word LIKE '%' || :searchText || '%' ORDER BY word ASC")
    suspend fun searchWords(searchText: String): List<PhonicsWord>

    /**
     * Get random words for practice session
     */
    @Query("SELECT * FROM phonics_words WHERE difficultyLevel <= :maxLevel ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomWordsForPractice(maxLevel: Int, count: Int): List<PhonicsWord>

    /**
     * Reset all word progress
     */
    @Query("UPDATE phonics_words SET timesPresented = 0, timesRecognized = 0, lastPresentedTimestamp = 0")
    suspend fun resetAllWordProgress()

    /**
     * Reset progress for specific word
     */
    @Query("UPDATE phonics_words SET timesPresented = 0, timesRecognized = 0, lastPresentedTimestamp = 0 WHERE id = :wordId")
    suspend fun resetWordProgress(wordId: Long)

    /**
     * Delete all phonics words
     */
    @Query("DELETE FROM phonics_words")
    suspend fun deleteAllPhonicsWords()

    /**
     * Check if phonics words are initialized
     */
    @Query("SELECT COUNT(*) FROM phonics_words")
    suspend fun getPhonicsWordCount(): Int
}

/**
 * Data class for word family statistics
 */
data class WordFamilyStats(
    val wordFamily: String,
    val wordCount: Int,
    val totalPractices: Int
)

/**
 * Data class for word accuracy statistics
 */
data class WordAccuracyStats(
    val word: String,
    val accuracy: Float
)
