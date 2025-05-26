package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.data.LetterDao
import com.happykid.toddlerabc.model.Letter
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Letter data operations
 * Provides a clean API for the UI layer to interact with letter data
 * Phase 2: Enhanced with Hilt dependency injection
 */
@Singleton
class LetterRepository @Inject constructor(
    private val letterDao: LetterDao
) {

    /**
     * Get all letters as Flow for reactive UI
     */
    fun getAllLettersFlow(): Flow<List<Letter>> = letterDao.getAllLettersFlow()

    /**
     * Get all letters as list
     */
    suspend fun getAllLetters(): List<Letter> = letterDao.getAllLetters()

    /**
     * Get a specific letter
     */
    suspend fun getLetter(character: Char): Letter? = letterDao.getLetter(character)

    /**
     * Get practiced letters
     */
    fun getPracticedLettersFlow(): Flow<List<Letter>> = letterDao.getPracticedLettersFlow()

    /**
     * Get learned letters
     */
    fun getLearnedLettersFlow(): Flow<List<Letter>> = letterDao.getLearnedLettersFlow()

    /**
     * Record letter practice
     */
    suspend fun practiceLetterClicked(character: Char) {
        val currentTime = System.currentTimeMillis()
        letterDao.incrementPracticeCount(character, currentTime)

        // Auto-mark as learned after 5 practices
        val letter = letterDao.getLetter(character)
        if (letter != null && letter.practiceCount >= 4 && !letter.isLearned) {
            letterDao.markLetterAsLearned(character, true)
        }
    }

    /**
     * Mark letter as learned/unlearned
     */
    suspend fun markLetterAsLearned(character: Char, isLearned: Boolean) {
        letterDao.markLetterAsLearned(character, isLearned)
    }

    /**
     * Get learning statistics
     */
    suspend fun getLearningStats(): LearningStats {
        val totalPracticeCount = letterDao.getTotalPracticeCount()
        val learnedCount = letterDao.getLearnedLetterCount()
        return LearningStats(
            totalPractices = totalPracticeCount,
            learnedLetters = learnedCount,
            totalLetters = 26,
            progressPercentage = (learnedCount * 100) / 26
        )
    }

    /**
     * Reset all progress
     */
    suspend fun resetAllProgress() {
        letterDao.resetAllProgress()
    }

    /**
     * Initialize database with default letters if empty
     */
    suspend fun initializeIfEmpty() {
        val letters = letterDao.getAllLetters()
        if (letters.isEmpty()) {
            val defaultLetters = Letter.getAllLetters()
            letterDao.insertLetters(defaultLetters)
        }
    }
}

/**
 * Data class for learning statistics
 */
data class LearningStats(
    val totalPractices: Int,
    val learnedLetters: Int,
    val totalLetters: Int,
    val progressPercentage: Int
)
