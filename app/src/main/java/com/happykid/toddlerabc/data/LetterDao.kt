package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.Letter
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Letter entity
 * Provides database operations for letter progress tracking
 */
@Dao
interface LetterDao {

    /**
     * Get all letters as a Flow for reactive UI updates
     */
    @Query("SELECT * FROM letters ORDER BY character ASC")
    fun getAllLettersFlow(): Flow<List<Letter>>

    /**
     * Get all letters as a list (for one-time operations)
     */
    @Query("SELECT * FROM letters ORDER BY character ASC")
    suspend fun getAllLetters(): List<Letter>

    /**
     * Get a specific letter by character
     */
    @Query("SELECT * FROM letters WHERE character = :character")
    suspend fun getLetter(character: Char): Letter?

    /**
     * Get letters that have been practiced (practiceCount > 0)
     */
    @Query("SELECT * FROM letters WHERE practiceCount > 0 ORDER BY lastPracticedTimestamp DESC")
    fun getPracticedLettersFlow(): Flow<List<Letter>>

    /**
     * Get letters marked as learned
     */
    @Query("SELECT * FROM letters WHERE isLearned = 1 ORDER BY character ASC")
    fun getLearnedLettersFlow(): Flow<List<Letter>>

    /**
     * Insert a single letter
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetter(letter: Letter)

    /**
     * Insert multiple letters (for initial setup)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLetters(letters: List<Letter>)

    /**
     * Update a letter's progress
     */
    @Update
    suspend fun updateLetter(letter: Letter)

    /**
     * Update letter practice count and timestamp
     */
    @Query("UPDATE letters SET practiceCount = practiceCount + 1, lastPracticedTimestamp = :timestamp WHERE character = :character")
    suspend fun incrementPracticeCount(character: Char, timestamp: Long)

    /**
     * Mark a letter as learned
     */
    @Query("UPDATE letters SET isLearned = :isLearned WHERE character = :character")
    suspend fun markLetterAsLearned(character: Char, isLearned: Boolean)

    /**
     * Get total practice count across all letters
     */
    @Query("SELECT SUM(practiceCount) FROM letters")
    suspend fun getTotalPracticeCount(): Int

    /**
     * Get count of learned letters
     */
    @Query("SELECT COUNT(*) FROM letters WHERE isLearned = 1")
    suspend fun getLearnedLetterCount(): Int

    /**
     * Reset all progress (for testing or reset functionality)
     */
    @Query("UPDATE letters SET practiceCount = 0, isLearned = 0, lastPracticedTimestamp = 0")
    suspend fun resetAllProgress()

    /**
     * Delete all letters (for testing)
     */
    @Query("DELETE FROM letters")
    suspend fun deleteAllLetters()
}
