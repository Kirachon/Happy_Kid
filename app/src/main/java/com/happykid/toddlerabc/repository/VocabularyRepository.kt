package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.data.VocabularyDao
import com.happykid.toddlerabc.model.VocabularyWord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Vocabulary data management
 * Phase 12: Content Expansion - Vocabulary management with Room database integration
 *
 * Provides a clean API for vocabulary operations including CRUD operations,
 * spaced repetition algorithms, and reactive data access with proper error handling.
 */
@Singleton
class VocabularyRepository @Inject constructor(
    private val vocabularyDao: VocabularyDao
) {

    // ==================== Query Operations ====================

    /**
     * Get all active vocabulary words as Flow for reactive UI updates
     */
    fun getAllVocabularyWordsFlow(): Flow<List<VocabularyWord>> {
        return vocabularyDao.getAllVocabularyWordsFlow()
    }

    /**
     * Get all active vocabulary words (non-reactive)
     */
    suspend fun getAllVocabularyWords(): List<VocabularyWord> {
        return vocabularyDao.getAllVocabularyWords()
    }

    /**
     * Get vocabulary word by ID
     */
    suspend fun getVocabularyWordById(wordId: String): VocabularyWord? {
        return vocabularyDao.getVocabularyWordById(wordId)
    }

    /**
     * Get vocabulary word by ID as Flow
     */
    fun getVocabularyWordByIdFlow(wordId: String): Flow<VocabularyWord?> {
        return vocabularyDao.getVocabularyWordByIdFlow(wordId)
    }

    /**
     * Get vocabulary words by category
     */
    suspend fun getVocabularyWordsByCategory(category: String): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsByCategory(category)
    }

    /**
     * Get vocabulary words by category as Flow
     */
    fun getVocabularyWordsByCategoryFlow(category: String): Flow<List<VocabularyWord>> {
        return vocabularyDao.getVocabularyWordsByCategoryFlow(category)
    }

    /**
     * Get vocabulary words by difficulty level
     */
    suspend fun getVocabularyWordsByDifficulty(level: Int): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsByDifficulty(level)
    }

    /**
     * Get vocabulary words by age range
     */
    suspend fun getVocabularyWordsByAgeRange(ageRange: String): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsByAgeRange(ageRange)
    }

    /**
     * Get core vocabulary words (essential words for age group)
     */
    suspend fun getCoreVocabularyWords(): List<VocabularyWord> {
        return vocabularyDao.getCoreVocabularyWords()
    }

    /**
     * Get core vocabulary words as Flow
     */
    fun getCoreVocabularyWordsFlow(): Flow<List<VocabularyWord>> {
        return vocabularyDao.getCoreVocabularyWordsFlow()
    }

    /**
     * Get vocabulary words by associated letters
     */
    suspend fun getVocabularyWordsByLetter(letter: String): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsByLetter(letter)
    }

    /**
     * Search vocabulary words by word or definition
     */
    suspend fun searchVocabularyWords(query: String): List<VocabularyWord> {
        return vocabularyDao.searchVocabularyWords(query)
    }

    /**
     * Get vocabulary word count by category
     */
    suspend fun getVocabularyWordCountByCategory(category: String): Int {
        return vocabularyDao.getVocabularyCountByCategory(category)
    }

    /**
     * Get vocabulary word count by difficulty level
     */
    suspend fun getVocabularyWordCountByDifficulty(level: Int): Int {
        return vocabularyDao.getVocabularyCountByDifficulty(level)
    }

    /**
     * Get all vocabulary categories
     */
    suspend fun getAllCategories(): List<String> {
        return vocabularyDao.getAllVocabularyCategories()
    }

    /**
     * Get all difficulty levels
     */
    suspend fun getAllDifficultyLevels(): List<Int> {
        return vocabularyDao.getAllDifficultyLevels()
    }

    /**
     * Get all age ranges
     */
    suspend fun getAllAgeRanges(): List<String> {
        return vocabularyDao.getAllAgeRanges()
    }

    // ==================== Spaced Repetition Operations ====================

    /**
     * Get vocabulary words due for review based on spaced repetition algorithm
     */
    suspend fun getWordsForReview(): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsDueForReview()
    }

    /**
     * Get vocabulary words due for review as Flow
     */
    fun getWordsForReviewFlow(): Flow<List<VocabularyWord>> {
        return vocabularyDao.getVocabularyWordsDueForReviewFlow()
    }

    /**
     * Update vocabulary word review schedule after practice session
     */
    suspend fun updateWordReviewSchedule(
        wordId: String,
        wasCorrect: Boolean,
        currentInterval: Int
    ): Int {
        val newInterval = calculateNextInterval(currentInterval, wasCorrect)
        val nextReviewDate = System.currentTimeMillis() + (newInterval * 24 * 60 * 60 * 1000L)
        return vocabularyDao.updateSpacedRepetitionData(wordId, newInterval, nextReviewDate)
    }

    /**
     * Calculate next review interval using spaced repetition algorithm
     * Based on SuperMemo SM-2 algorithm adapted for toddlers
     */
    private fun calculateNextInterval(currentInterval: Int, wasCorrect: Boolean): Int {
        return if (wasCorrect) {
            when (currentInterval) {
                0 -> 1      // First correct: review tomorrow
                1 -> 3      // Second correct: review in 3 days
                else -> (currentInterval * 1.5).toInt().coerceAtMost(30) // Max 30 days for toddlers
            }
        } else {
            1 // Reset to 1 day if incorrect
        }
    }

    // ==================== Insert Operations ====================

    /**
     * Insert a single vocabulary word
     */
    suspend fun insertVocabularyWord(word: VocabularyWord): Long {
        return vocabularyDao.insertVocabularyWord(word)
    }

    /**
     * Insert multiple vocabulary words
     */
    suspend fun insertVocabularyWords(words: List<VocabularyWord>): List<Long> {
        return vocabularyDao.insertVocabularyWords(words)
    }

    // ==================== Update Operations ====================

    /**
     * Update a vocabulary word
     */
    suspend fun updateVocabularyWord(word: VocabularyWord): Int {
        return vocabularyDao.updateVocabularyWord(word)
    }

    /**
     * Update multiple vocabulary words
     */
    suspend fun updateVocabularyWords(words: List<VocabularyWord>): Int {
        return vocabularyDao.updateVocabularyWords(words)
    }

    /**
     * Update vocabulary word active status
     */
    suspend fun updateVocabularyWordActiveStatus(wordId: String, isActive: Boolean): Int {
        return vocabularyDao.updateVocabularyWordActiveStatus(wordId, isActive)
    }

    /**
     * Update vocabulary word difficulty level
     */
    suspend fun updateVocabularyWordDifficulty(wordId: String, level: Int): Int {
        return vocabularyDao.updateVocabularyWordDifficulty(wordId, level)
    }

    // ==================== Delete Operations ====================

    /**
     * Delete a vocabulary word by ID
     */
    suspend fun deleteVocabularyWordById(wordId: String): Int {
        return vocabularyDao.deleteVocabularyWordById(wordId)
    }

    /**
     * Delete a vocabulary word entity
     */
    suspend fun deleteVocabularyWord(word: VocabularyWord): Int {
        return vocabularyDao.deleteVocabularyWord(word)
    }

    /**
     * Delete multiple vocabulary words
     */
    suspend fun deleteVocabularyWords(words: List<VocabularyWord>): Int {
        return vocabularyDao.deleteVocabularyWords(words)
    }

    /**
     * Delete all vocabulary words (for testing/reset)
     */
    suspend fun deleteAllVocabularyWords(): Int {
        return vocabularyDao.deleteAllVocabularyWords()
    }

    /**
     * Soft delete vocabulary word (mark as inactive)
     */
    suspend fun softDeleteVocabularyWord(wordId: String): Int {
        return vocabularyDao.softDeleteVocabularyWord(wordId)
    }

    // ==================== Utility Operations ====================

    /**
     * Check if vocabulary word exists
     */
    suspend fun vocabularyWordExists(wordId: String): Boolean {
        return vocabularyDao.vocabularyWordExists(wordId)
    }

    /**
     * Get total vocabulary word count
     */
    suspend fun getTotalVocabularyWordCount(): Int {
        return vocabularyDao.getTotalVocabularyWordCount()
    }

    /**
     * Get vocabulary words created after timestamp
     */
    suspend fun getVocabularyWordsCreatedAfter(timestamp: Long): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsCreatedAfter(timestamp)
    }

    /**
     * Get vocabulary words updated after timestamp
     */
    suspend fun getVocabularyWordsUpdatedAfter(timestamp: Long): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsUpdatedAfter(timestamp)
    }

    // ==================== Business Logic Operations ====================

    /**
     * Get recommended vocabulary words based on user progress and age
     */
    suspend fun getRecommendedVocabularyWords(
        userAgeRange: String,
        masteredWordIds: List<String>,
        maxDifficultyLevel: Int = 3,
        limit: Int = 10
    ): List<VocabularyWord> {
        // Get age-appropriate words
        val ageAppropriateWords = getVocabularyWordsByAgeRange(userAgeRange)

        // Filter out mastered words
        val unmasteredWords = ageAppropriateWords.filter { word ->
            word.id !in masteredWordIds
        }

        // Filter by difficulty level
        val appropriateDifficultyWords = unmasteredWords.filter { word ->
            word.difficultyLevel <= maxDifficultyLevel
        }

        // Prioritize core vocabulary and high frequency words
        val prioritizedWords = appropriateDifficultyWords.sortedWith(
            compareByDescending<VocabularyWord> { it.isCoreVocabulary }
                .thenByDescending { it.frequencyScore }
                .thenBy { it.difficultyLevel }
        )

        return prioritizedWords.take(limit)
    }

    /**
     * Get vocabulary words for letter practice
     */
    suspend fun getVocabularyWordsForLetterPractice(letters: List<String>): List<VocabularyWord> {
        val allWords = getAllVocabularyWords()
        return allWords.filter { word ->
            letters.any { letter ->
                word.associatedLetters.contains(letter)
            }
        }.sortedBy { it.difficultyLevel }
    }

    /**
     * Get vocabulary words by frequency score range
     */
    suspend fun getVocabularyWordsByFrequencyRange(
        minFrequency: Int,
        maxFrequency: Int
    ): List<VocabularyWord> {
        return vocabularyDao.getVocabularyWordsByFrequencyRange(minFrequency, maxFrequency)
    }

    /**
     * Get daily vocabulary practice set based on spaced repetition
     */
    suspend fun getDailyPracticeSet(
        userAgeRange: String,
        targetCount: Int = 5
    ): List<VocabularyWord> {
        // Get words due for review
        val reviewWords = getWordsForReview()

        // If we have enough review words, return them
        if (reviewWords.size >= targetCount) {
            return reviewWords.take(targetCount)
        }

        // Otherwise, supplement with new words
        val newWordsNeeded = targetCount - reviewWords.size
        val newWords = getRecommendedVocabularyWords(
            userAgeRange = userAgeRange,
            masteredWordIds = emptyList(), // Get all available words
            limit = newWordsNeeded
        )

        return reviewWords + newWords.take(newWordsNeeded)
    }

    /**
     * Update vocabulary word statistics after practice session
     */
    suspend fun updateWordStatistics(
        wordId: String,
        wasCorrect: Boolean,
        responseTimeMs: Long
    ): Int {
        val word = getVocabularyWordById(wordId) ?: return 0

        // Update frequency score based on performance
        val newFrequencyScore = if (wasCorrect) {
            (word.frequencyScore + 1).coerceAtMost(100)
        } else {
            (word.frequencyScore - 1).coerceAtLeast(1)
        }

        // Update review schedule
        updateWordReviewSchedule(wordId, wasCorrect, word.repetitionInterval)

        // Update the word with new statistics
        val updatedWord = word.copy(
            frequencyScore = newFrequencyScore,
            updatedAt = System.currentTimeMillis()
        )

        return updateVocabularyWord(updatedWord)
    }
}
