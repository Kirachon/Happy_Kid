package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.data.PhonicsDao
import com.happykid.toddlerabc.model.PhonicsWord
import com.happykid.toddlerabc.model.WordCategory
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Phonics Word data operations
 * Phase 9: Provides a clean API for phonics word management and vocabulary operations
 *
 * Handles phonics word CRUD operations, word family management, sight word tracking,
 * and vocabulary progression with optimized caching for Windows emulator performance.
 */
@Singleton
class PhonicsRepository @Inject constructor(
    private val phonicsDao: PhonicsDao
) {

    /**
     * Get all phonics words as Flow for reactive UI
     */
    fun getAllPhonicsWordsFlow(): Flow<List<PhonicsWord>> = phonicsDao.getAllPhonicsWordsFlow()

    /**
     * Get all phonics words as list
     */
    suspend fun getAllPhonicsWords(): List<PhonicsWord> = phonicsDao.getAllPhonicsWords()

    /**
     * Get a specific phonics word by ID
     */
    suspend fun getPhonicsWordById(id: Long): PhonicsWord? = phonicsDao.getPhonicsWordById(id)

    /**
     * Get a specific phonics word by text
     */
    suspend fun getPhonicsWordByText(word: String): PhonicsWord? = phonicsDao.getPhonicsWordByText(word)

    /**
     * Get words by category
     */
    fun getWordsByCategory(category: WordCategory): Flow<List<PhonicsWord>> = 
        phonicsDao.getWordsByCategory(category)

    /**
     * Get words by difficulty level
     */
    fun getWordsByDifficulty(level: Int): Flow<List<PhonicsWord>> = 
        phonicsDao.getWordsByDifficulty(level)

    /**
     * Get sight words
     */
    fun getSightWordsFlow(): Flow<List<PhonicsWord>> = phonicsDao.getSightWordsFlow()

    /**
     * Get high-frequency words
     */
    fun getHighFrequencyWordsFlow(): Flow<List<PhonicsWord>> = phonicsDao.getHighFrequencyWordsFlow()

    /**
     * Get CVC words
     */
    fun getCVCWordsFlow(): Flow<List<PhonicsWord>> = phonicsDao.getCVCWordsFlow()

    /**
     * Get words by word family
     */
    fun getWordsByFamily(family: String): Flow<List<PhonicsWord>> = 
        phonicsDao.getWordsByFamily(family)

    /**
     * Get mastered words
     */
    suspend fun getMasteredWords(): List<PhonicsWord> = phonicsDao.getMasteredWords()

    /**
     * Get words that need practice
     */
    suspend fun getWordsNeedingPractice(): List<PhonicsWord> = phonicsDao.getWordsNeedingPractice()

    /**
     * Get recently practiced words
     */
    suspend fun getRecentlyPracticedWords(hoursAgo: Int = 24): List<PhonicsWord> {
        val since = System.currentTimeMillis() - (hoursAgo * 60 * 60 * 1000L)
        return phonicsDao.getRecentlyPracticedWords(since)
    }

    /**
     * Record word presentation and update statistics
     */
    suspend fun recordWordPresentation(wordId: Long) {
        val timestamp = System.currentTimeMillis()
        phonicsDao.recordWordPresentation(wordId, timestamp)
    }

    /**
     * Record word recognition success
     */
    suspend fun recordWordRecognition(wordId: Long) {
        phonicsDao.recordWordRecognition(wordId)
    }

    /**
     * Practice word (presentation + recognition if successful)
     */
    suspend fun practiceWord(wordId: Long, wasRecognized: Boolean) {
        recordWordPresentation(wordId)
        if (wasRecognized) {
            recordWordRecognition(wordId)
        }
    }

    /**
     * Get phonics statistics
     */
    suspend fun getPhonicsStats(): PhonicsStats {
        val totalWords = phonicsDao.getTotalWordCount()
        val masteredWords = phonicsDao.getMasteredWordCount()
        val masteredSightWords = phonicsDao.getMasteredSightWordCount()
        val masteredCVCWords = phonicsDao.getMasteredCVCWordCount()
        val averageAccuracy = phonicsDao.getAverageRecognitionAccuracy() ?: 0f
        
        return PhonicsStats(
            totalWords = totalWords,
            masteredWords = masteredWords,
            masteredSightWords = masteredSightWords,
            masteredCVCWords = masteredCVCWords,
            averageAccuracy = averageAccuracy,
            progressPercentage = if (totalWords > 0) (masteredWords * 100) / totalWords else 0
        )
    }

    /**
     * Get word families with practice statistics
     */
    suspend fun getWordFamilyStats() = phonicsDao.getWordFamiliesByPractice()

    /**
     * Get most challenging words
     */
    suspend fun getMostChallengingWords(limit: Int = 5) = phonicsDao.getMostChallengingWords(limit)

    /**
     * Get words appropriate for age/difficulty level
     */
    suspend fun getWordsForAge(ageInYears: Int): List<PhonicsWord> {
        val maxLevel = when (ageInYears) {
            1, 2 -> 1  // Very simple words
            3 -> 2     // Simple CVC words
            4 -> 3     // More complex words
            else -> 5  // All words
        }
        return phonicsDao.getWordsForAge(maxLevel)
    }

    /**
     * Search words by partial text
     */
    suspend fun searchWords(searchText: String): List<PhonicsWord> = 
        phonicsDao.searchWords(searchText)

    /**
     * Get random words for practice session
     */
    suspend fun getRandomWordsForPractice(ageInYears: Int, count: Int = 5): List<PhonicsWord> {
        val maxLevel = when (ageInYears) {
            1, 2 -> 1
            3 -> 2
            4 -> 3
            else -> 5
        }
        return phonicsDao.getRandomWordsForPractice(maxLevel, count)
    }

    /**
     * Add new phonics word
     */
    suspend fun addPhonicsWord(word: PhonicsWord): Long = phonicsDao.insertPhonicsWord(word)

    /**
     * Update phonics word
     */
    suspend fun updatePhonicsWord(word: PhonicsWord) = phonicsDao.updatePhonicsWord(word)

    /**
     * Delete phonics word
     */
    suspend fun deletePhonicsWord(word: PhonicsWord) = phonicsDao.deletePhonicsWord(word)

    /**
     * Reset all word progress
     */
    suspend fun resetAllProgress() = phonicsDao.resetAllWordProgress()

    /**
     * Reset progress for specific word
     */
    suspend fun resetWordProgress(wordId: Long) = phonicsDao.resetWordProgress(wordId)

    /**
     * Initialize phonics words if database is empty
     */
    suspend fun initializeIfEmpty() {
        val wordCount = phonicsDao.getPhonicsWordCount()
        if (wordCount == 0) {
            val cvcWords = PhonicsWord.getDefaultCVCWords()
            val sightWords = PhonicsWord.getDefaultSightWords()
            phonicsDao.insertPhonicsWords(cvcWords + sightWords)
        }
    }

    /**
     * Get words by multiple criteria for advanced filtering
     */
    suspend fun getFilteredWords(
        category: WordCategory? = null,
        difficultyLevel: Int? = null,
        isSightWord: Boolean? = null,
        wordFamily: String? = null,
        onlyMastered: Boolean = false,
        onlyNeedsPractice: Boolean = false
    ): List<PhonicsWord> {
        // Start with all words
        var words = getAllPhonicsWords()
        
        // Apply filters
        category?.let { words = words.filter { it.category == category } }
        difficultyLevel?.let { words = words.filter { it.difficultyLevel == difficultyLevel } }
        isSightWord?.let { words = words.filter { it.isSightWord == isSightWord } }
        wordFamily?.let { words = words.filter { it.wordFamily == wordFamily } }
        
        if (onlyMastered) {
            words = words.filter { it.isMastered }
        }
        
        if (onlyNeedsPractice) {
            words = words.filter { it.timesPresented == 0 || it.recognitionAccuracy < 60f }
        }
        
        return words
    }
}

/**
 * Phonics statistics data class
 */
data class PhonicsStats(
    val totalWords: Int,
    val masteredWords: Int,
    val masteredSightWords: Int,
    val masteredCVCWords: Int,
    val averageAccuracy: Float,
    val progressPercentage: Int
)
