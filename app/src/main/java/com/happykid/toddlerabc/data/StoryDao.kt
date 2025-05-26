package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.Story
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Story entities
 * Phase 12: Content Expansion - Story management with Room database
 *
 * Provides database operations for interactive stories including
 * CRUD operations, filtering, and reactive data access.
 */
@Dao
interface StoryDao {
    
    // ==================== Query Operations ====================
    
    /**
     * Get all active stories as Flow for reactive UI updates
     */
    @Query("SELECT * FROM stories WHERE isActive = 1 ORDER BY difficultyLevel ASC, createdAt ASC")
    fun getAllStoriesFlow(): Flow<List<Story>>
    
    /**
     * Get all active stories (non-reactive)
     */
    @Query("SELECT * FROM stories WHERE isActive = 1 ORDER BY difficultyLevel ASC, createdAt ASC")
    suspend fun getAllStories(): List<Story>
    
    /**
     * Get story by ID
     */
    @Query("SELECT * FROM stories WHERE id = :storyId")
    suspend fun getStoryById(storyId: String): Story?
    
    /**
     * Get story by ID as Flow
     */
    @Query("SELECT * FROM stories WHERE id = :storyId")
    fun getStoryByIdFlow(storyId: String): Flow<Story?>
    
    /**
     * Get stories by category
     */
    @Query("SELECT * FROM stories WHERE category = :category AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getStoriesByCategory(category: String): List<Story>
    
    /**
     * Get stories by category as Flow
     */
    @Query("SELECT * FROM stories WHERE category = :category AND isActive = 1 ORDER BY difficultyLevel ASC")
    fun getStoriesByCategoryFlow(category: String): Flow<List<Story>>
    
    /**
     * Get stories by difficulty level
     */
    @Query("SELECT * FROM stories WHERE difficultyLevel = :level AND isActive = 1 ORDER BY createdAt ASC")
    suspend fun getStoriesByDifficulty(level: Int): List<Story>
    
    /**
     * Get stories by age range
     */
    @Query("SELECT * FROM stories WHERE ageRange = :ageRange AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getStoriesByAgeRange(ageRange: String): List<Story>
    
    /**
     * Get stories without prerequisites (unlocked by default)
     */
    @Query("SELECT * FROM stories WHERE hasPrerequisites = 0 AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getUnlockedStories(): List<Story>
    
    /**
     * Get stories without prerequisites as Flow
     */
    @Query("SELECT * FROM stories WHERE hasPrerequisites = 0 AND isActive = 1 ORDER BY difficultyLevel ASC")
    fun getUnlockedStoriesFlow(): Flow<List<Story>>
    
    /**
     * Get stories by target letters
     */
    @Query("SELECT * FROM stories WHERE targetLetters LIKE '%' || :letter || '%' AND isActive = 1")
    suspend fun getStoriesByTargetLetter(letter: String): List<Story>
    
    /**
     * Get stories by phonics words
     */
    @Query("SELECT * FROM stories WHERE phonicsWords LIKE '%' || :word || '%' AND isActive = 1")
    suspend fun getStoriesByPhonicsWord(word: String): List<Story>
    
    /**
     * Search stories by title or content
     */
    @Query("""
        SELECT * FROM stories 
        WHERE (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') 
        AND isActive = 1 
        ORDER BY difficultyLevel ASC
    """)
    suspend fun searchStories(query: String): List<Story>
    
    /**
     * Get story count by category
     */
    @Query("SELECT COUNT(*) FROM stories WHERE category = :category AND isActive = 1")
    suspend fun getStoryCountByCategory(category: String): Int
    
    /**
     * Get story count by difficulty level
     */
    @Query("SELECT COUNT(*) FROM stories WHERE difficultyLevel = :level AND isActive = 1")
    suspend fun getStoryCountByDifficulty(level: Int): Int
    
    /**
     * Get all story categories
     */
    @Query("SELECT DISTINCT category FROM stories WHERE isActive = 1 ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
    
    /**
     * Get all difficulty levels
     */
    @Query("SELECT DISTINCT difficultyLevel FROM stories WHERE isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getAllDifficultyLevels(): List<Int>
    
    /**
     * Get all age ranges
     */
    @Query("SELECT DISTINCT ageRange FROM stories WHERE isActive = 1 ORDER BY ageRange ASC")
    suspend fun getAllAgeRanges(): List<String>
    
    // ==================== Insert Operations ====================
    
    /**
     * Insert a single story
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: Story): Long
    
    /**
     * Insert multiple stories
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<Story>): List<Long>
    
    // ==================== Update Operations ====================
    
    /**
     * Update a story
     */
    @Update
    suspend fun updateStory(story: Story): Int
    
    /**
     * Update multiple stories
     */
    @Update
    suspend fun updateStories(stories: List<Story>): Int
    
    /**
     * Update story active status
     */
    @Query("UPDATE stories SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :storyId")
    suspend fun updateStoryActiveStatus(storyId: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis()): Int
    
    /**
     * Update story difficulty level
     */
    @Query("UPDATE stories SET difficultyLevel = :level, updatedAt = :updatedAt WHERE id = :storyId")
    suspend fun updateStoryDifficulty(storyId: String, level: Int, updatedAt: Long = System.currentTimeMillis()): Int
    
    // ==================== Delete Operations ====================
    
    /**
     * Delete a story by ID
     */
    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStoryById(storyId: String): Int
    
    /**
     * Delete a story entity
     */
    @Delete
    suspend fun deleteStory(story: Story): Int
    
    /**
     * Delete multiple stories
     */
    @Delete
    suspend fun deleteStories(stories: List<Story>): Int
    
    /**
     * Delete all stories (for testing/reset)
     */
    @Query("DELETE FROM stories")
    suspend fun deleteAllStories(): Int
    
    /**
     * Soft delete story (mark as inactive)
     */
    @Query("UPDATE stories SET isActive = 0, updatedAt = :updatedAt WHERE id = :storyId")
    suspend fun softDeleteStory(storyId: String, updatedAt: Long = System.currentTimeMillis()): Int
    
    // ==================== Utility Operations ====================
    
    /**
     * Check if story exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM stories WHERE id = :storyId)")
    suspend fun storyExists(storyId: String): Boolean
    
    /**
     * Get total story count
     */
    @Query("SELECT COUNT(*) FROM stories WHERE isActive = 1")
    suspend fun getTotalStoryCount(): Int
    
    /**
     * Get stories created after timestamp
     */
    @Query("SELECT * FROM stories WHERE createdAt > :timestamp AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getStoriesCreatedAfter(timestamp: Long): List<Story>
    
    /**
     * Get stories updated after timestamp
     */
    @Query("SELECT * FROM stories WHERE updatedAt > :timestamp AND isActive = 1 ORDER BY updatedAt DESC")
    suspend fun getStoriesUpdatedAfter(timestamp: Long): List<Story>
}
