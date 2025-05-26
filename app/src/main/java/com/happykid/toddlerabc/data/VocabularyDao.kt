package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.VocabularyWord
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for VocabularyWord entities
 * Phase 12: Content Expansion - Vocabulary management with Room database
 *
 * Provides database operations for vocabulary words including
 * CRUD operations, spaced repetition queries, and category filtering.
 */
@Dao
interface VocabularyDao {

    // ==================== Query Operations ====================

    /**
     * Get all active vocabulary words as Flow for reactive UI updates
     */
    @Query("SELECT * FROM vocabulary_words WHERE isActive = 1 ORDER BY frequencyScore DESC, word ASC")
    fun getAllVocabularyWordsFlow(): Flow<List<VocabularyWord>>

    /**
     * Get all active vocabulary words (non-reactive)
     */
    @Query("SELECT * FROM vocabulary_words WHERE isActive = 1 ORDER BY frequencyScore DESC, word ASC")
    suspend fun getAllVocabularyWords(): List<VocabularyWord>

    /**
     * Get vocabulary word by ID
     */
    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId")
    suspend fun getVocabularyWordById(wordId: String): VocabularyWord?

    /**
     * Get vocabulary word by ID as Flow
     */
    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId")
    fun getVocabularyWordByIdFlow(wordId: String): Flow<VocabularyWord?>

    /**
     * Get vocabulary word by word text
     */
    @Query("SELECT * FROM vocabulary_words WHERE word = :word AND isActive = 1")
    suspend fun getVocabularyWordByText(word: String): VocabularyWord?

    /**
     * Get vocabulary words by category
     */
    @Query("SELECT * FROM vocabulary_words WHERE category = :category AND isActive = 1 ORDER BY frequencyScore DESC")
    suspend fun getVocabularyWordsByCategory(category: String): List<VocabularyWord>

    /**
     * Get vocabulary words by category as Flow
     */
    @Query("SELECT * FROM vocabulary_words WHERE category = :category AND isActive = 1 ORDER BY frequencyScore DESC")
    fun getVocabularyWordsByCategoryFlow(category: String): Flow<List<VocabularyWord>>

    /**
     * Get vocabulary words by difficulty level
     */
    @Query("SELECT * FROM vocabulary_words WHERE difficultyLevel = :level AND isActive = 1 ORDER BY frequencyScore DESC")
    suspend fun getVocabularyWordsByDifficulty(level: Int): List<VocabularyWord>

    /**
     * Get vocabulary words by age range
     */
    @Query("SELECT * FROM vocabulary_words WHERE ageRange = :ageRange AND isActive = 1 ORDER BY frequencyScore DESC")
    suspend fun getVocabularyWordsByAgeRange(ageRange: String): List<VocabularyWord>

    /**
     * Get core vocabulary words
     */
    @Query("SELECT * FROM vocabulary_words WHERE isCoreVocabulary = 1 AND isActive = 1 ORDER BY frequencyScore DESC")
    suspend fun getCoreVocabularyWords(): List<VocabularyWord>

    /**
     * Get core vocabulary words as Flow
     */
    @Query("SELECT * FROM vocabulary_words WHERE isCoreVocabulary = 1 AND isActive = 1 ORDER BY frequencyScore DESC")
    fun getCoreVocabularyWordsFlow(): Flow<List<VocabularyWord>>

    /**
     * Get vocabulary words without unlock criteria (available immediately)
     */
    @Query("SELECT * FROM vocabulary_words WHERE unlockCriteria IS NULL AND isActive = 1 ORDER BY frequencyScore DESC")
    suspend fun getUnlockedVocabularyWords(): List<VocabularyWord>

    /**
     * Get vocabulary words without unlock criteria as Flow
     */
    @Query("SELECT * FROM vocabulary_words WHERE unlockCriteria IS NULL AND isActive = 1 ORDER BY frequencyScore DESC")
    fun getUnlockedVocabularyWordsFlow(): Flow<List<VocabularyWord>>

    /**
     * Get vocabulary words by associated letter
     */
    @Query("SELECT * FROM vocabulary_words WHERE associatedLetters LIKE '%' || :letter || '%' AND isActive = 1")
    suspend fun getVocabularyWordsByLetter(letter: String): List<VocabularyWord>

    /**
     * Get vocabulary words due for review (spaced repetition)
     */
    @Query("SELECT * FROM vocabulary_words WHERE nextReviewDate <= :currentTime AND isActive = 1 ORDER BY nextReviewDate ASC")
    suspend fun getVocabularyWordsDueForReview(currentTime: Long = System.currentTimeMillis()): List<VocabularyWord>

    /**
     * Get vocabulary words due for review as Flow
     */
    @Query("SELECT * FROM vocabulary_words WHERE nextReviewDate <= :currentTime AND isActive = 1 ORDER BY nextReviewDate ASC")
    fun getVocabularyWordsDueForReviewFlow(currentTime: Long = System.currentTimeMillis()): Flow<List<VocabularyWord>>

    /**
     * Search vocabulary words by word or definition
     */
    @Query("""
        SELECT * FROM vocabulary_words
        WHERE (word LIKE '%' || :query || '%' OR definition LIKE '%' || :query || '%')
        AND isActive = 1
        ORDER BY frequencyScore DESC
    """)
    suspend fun searchVocabularyWords(query: String): List<VocabularyWord>

    /**
     * Get vocabulary word count by category
     */
    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE category = :category AND isActive = 1")
    suspend fun getVocabularyCountByCategory(category: String): Int

    /**
     * Get vocabulary word count by difficulty level
     */
    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE difficultyLevel = :level AND isActive = 1")
    suspend fun getVocabularyCountByDifficulty(level: Int): Int

    /**
     * Get all vocabulary categories
     */
    @Query("SELECT DISTINCT category FROM vocabulary_words WHERE isActive = 1 ORDER BY category ASC")
    suspend fun getAllVocabularyCategories(): List<String>

    /**
     * Get all difficulty levels
     */
    @Query("SELECT DISTINCT difficultyLevel FROM vocabulary_words WHERE isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getAllDifficultyLevels(): List<Int>

    /**
     * Get all age ranges
     */
    @Query("SELECT DISTINCT ageRange FROM vocabulary_words WHERE isActive = 1 ORDER BY ageRange ASC")
    suspend fun getAllAgeRanges(): List<String>

    /**
     * Get vocabulary words with highest frequency scores
     */
    @Query("SELECT * FROM vocabulary_words WHERE isActive = 1 ORDER BY frequencyScore DESC LIMIT :limit")
    suspend fun getTopFrequencyVocabularyWords(limit: Int = 20): List<VocabularyWord>

    /**
     * Get vocabulary words by frequency score range
     */
    @Query("SELECT * FROM vocabulary_words WHERE frequencyScore BETWEEN :minFrequency AND :maxFrequency AND isActive = 1 ORDER BY frequencyScore DESC")
    suspend fun getVocabularyWordsByFrequencyRange(minFrequency: Int, maxFrequency: Int): List<VocabularyWord>

    // ==================== Insert Operations ====================

    /**
     * Insert a single vocabulary word
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabularyWord(word: VocabularyWord): Long

    /**
     * Insert multiple vocabulary words
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabularyWords(words: List<VocabularyWord>): List<Long>

    // ==================== Update Operations ====================

    /**
     * Update a vocabulary word
     */
    @Update
    suspend fun updateVocabularyWord(word: VocabularyWord): Int

    /**
     * Update multiple vocabulary words
     */
    @Update
    suspend fun updateVocabularyWords(words: List<VocabularyWord>): Int

    /**
     * Update vocabulary word active status
     */
    @Query("UPDATE vocabulary_words SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :wordId")
    suspend fun updateVocabularyWordActiveStatus(wordId: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis()): Int

    /**
     * Update vocabulary word difficulty level
     */
    @Query("UPDATE vocabulary_words SET difficultyLevel = :level, updatedAt = :updatedAt WHERE id = :wordId")
    suspend fun updateVocabularyWordDifficulty(wordId: String, level: Int, updatedAt: Long = System.currentTimeMillis()): Int

    /**
     * Update spaced repetition data
     */
    @Query("""
        UPDATE vocabulary_words
        SET repetitionInterval = :interval, nextReviewDate = :nextReviewDate, updatedAt = :updatedAt
        WHERE id = :wordId
    """)
    suspend fun updateSpacedRepetitionData(
        wordId: String,
        interval: Int,
        nextReviewDate: Long,
        updatedAt: Long = System.currentTimeMillis()
    ): Int

    // ==================== Delete Operations ====================

    /**
     * Delete a vocabulary word by ID
     */
    @Query("DELETE FROM vocabulary_words WHERE id = :wordId")
    suspend fun deleteVocabularyWordById(wordId: String): Int

    /**
     * Delete a vocabulary word entity
     */
    @Delete
    suspend fun deleteVocabularyWord(word: VocabularyWord): Int

    /**
     * Delete multiple vocabulary words
     */
    @Delete
    suspend fun deleteVocabularyWords(words: List<VocabularyWord>): Int

    /**
     * Delete all vocabulary words (for testing/reset)
     */
    @Query("DELETE FROM vocabulary_words")
    suspend fun deleteAllVocabularyWords(): Int

    /**
     * Soft delete vocabulary word (mark as inactive)
     */
    @Query("UPDATE vocabulary_words SET isActive = 0, updatedAt = :updatedAt WHERE id = :wordId")
    suspend fun softDeleteVocabularyWord(wordId: String, updatedAt: Long = System.currentTimeMillis()): Int

    // ==================== Utility Operations ====================

    /**
     * Check if vocabulary word exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM vocabulary_words WHERE id = :wordId)")
    suspend fun vocabularyWordExists(wordId: String): Boolean

    /**
     * Check if word text exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM vocabulary_words WHERE word = :word AND isActive = 1)")
    suspend fun wordTextExists(word: String): Boolean

    /**
     * Get total vocabulary word count
     */
    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE isActive = 1")
    suspend fun getTotalVocabularyWordCount(): Int

    /**
     * Get vocabulary words created after timestamp
     */
    @Query("SELECT * FROM vocabulary_words WHERE createdAt > :timestamp AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getVocabularyWordsCreatedAfter(timestamp: Long): List<VocabularyWord>

    /**
     * Get vocabulary words updated after timestamp
     */
    @Query("SELECT * FROM vocabulary_words WHERE updatedAt > :timestamp AND isActive = 1 ORDER BY updatedAt DESC")
    suspend fun getVocabularyWordsUpdatedAfter(timestamp: Long): List<VocabularyWord>
}
