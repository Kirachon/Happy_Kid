package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.data.StoryDao
import com.happykid.toddlerabc.model.Story
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Story data management
 * Phase 12: Content Expansion - Story management with Room database integration
 *
 * Provides a clean API for story operations including CRUD operations,
 * filtering, and reactive data access with proper error handling.
 *
 * Now using Room database for persistent story storage.
 */
@Singleton
class StoryRepository @Inject constructor(
    private val storyDao: StoryDao
) {

    // ==================== Query Operations ====================

    /**
     * Get all active stories as Flow for reactive UI updates
     */
    fun getAllStoriesFlow(): Flow<List<Story>> {
        return storyDao.getAllStoriesFlow()
    }

    /**
     * Get all active stories (non-reactive)
     */
    suspend fun getAllStories(): List<Story> {
        return storyDao.getAllStories()
    }

    /**
     * Get story by ID
     */
    suspend fun getStoryById(storyId: String): Story? {
        return storyDao.getStoryById(storyId)
    }

    /**
     * Get story by ID as Flow
     */
    fun getStoryByIdFlow(storyId: String): Flow<Story?> {
        return storyDao.getStoryByIdFlow(storyId)
    }

    /**
     * Get stories by category
     */
    suspend fun getStoriesByCategory(category: String): List<Story> {
        return storyDao.getStoriesByCategory(category)
    }

    /**
     * Get stories by category as Flow
     */
    fun getStoriesByCategoryFlow(category: String): Flow<List<Story>> {
        return storyDao.getStoriesByCategoryFlow(category)
    }

    /**
     * Get stories by difficulty level
     */
    suspend fun getStoriesByDifficulty(level: Int): List<Story> {
        return storyDao.getStoriesByDifficulty(level)
    }

    /**
     * Get stories by age range
     */
    suspend fun getStoriesByAgeRange(ageRange: String): List<Story> {
        return storyDao.getStoriesByAgeRange(ageRange)
    }

    /**
     * Get stories without prerequisites (unlocked by default)
     */
    suspend fun getUnlockedStories(): List<Story> {
        return storyDao.getUnlockedStories()
    }

    /**
     * Get stories without prerequisites as Flow
     */
    fun getUnlockedStoriesFlow(): Flow<List<Story>> {
        return storyDao.getUnlockedStoriesFlow()
    }

    /**
     * Get stories by target letters
     */
    suspend fun getStoriesByTargetLetter(letter: String): List<Story> {
        return storyDao.getStoriesByTargetLetter(letter)
    }

    /**
     * Get stories by phonics words
     */
    suspend fun getStoriesByPhonicsWord(word: String): List<Story> {
        return storyDao.getStoriesByPhonicsWord(word)
    }

    /**
     * Search stories by title or content
     */
    suspend fun searchStories(query: String): List<Story> {
        return storyDao.searchStories(query)
    }

    /**
     * Get story count by category
     */
    suspend fun getStoryCountByCategory(category: String): Int {
        return storyDao.getStoryCountByCategory(category)
    }

    /**
     * Get story count by difficulty level
     */
    suspend fun getStoryCountByDifficulty(level: Int): Int {
        return storyDao.getStoryCountByDifficulty(level)
    }

    /**
     * Get all story categories
     */
    suspend fun getAllCategories(): List<String> {
        return storyDao.getAllCategories()
    }

    /**
     * Get all difficulty levels
     */
    suspend fun getAllDifficultyLevels(): List<Int> {
        return storyDao.getAllDifficultyLevels()
    }

    /**
     * Get all age ranges
     */
    suspend fun getAllAgeRanges(): List<String> {
        return storyDao.getAllAgeRanges()
    }

    // ==================== Insert Operations ====================

    /**
     * Insert a single story
     */
    suspend fun insertStory(story: Story): Long {
        return storyDao.insertStory(story)
    }

    /**
     * Insert multiple stories
     */
    suspend fun insertStories(stories: List<Story>): List<Long> {
        return storyDao.insertStories(stories)
    }

    // ==================== Update Operations ====================

    /**
     * Update a story
     */
    suspend fun updateStory(story: Story): Int {
        return storyDao.updateStory(story)
    }

    /**
     * Update multiple stories
     */
    suspend fun updateStories(stories: List<Story>): Int {
        return storyDao.updateStories(stories)
    }

    /**
     * Update story active status
     */
    suspend fun updateStoryActiveStatus(storyId: String, isActive: Boolean): Int {
        return storyDao.updateStoryActiveStatus(storyId, isActive)
    }

    /**
     * Update story difficulty level
     */
    suspend fun updateStoryDifficulty(storyId: String, level: Int): Int {
        return storyDao.updateStoryDifficulty(storyId, level)
    }

    // ==================== Delete Operations ====================

    /**
     * Delete a story by ID
     */
    suspend fun deleteStoryById(storyId: String): Int {
        return storyDao.deleteStoryById(storyId)
    }

    /**
     * Delete a story entity
     */
    suspend fun deleteStory(story: Story): Int {
        return storyDao.deleteStory(story)
    }

    /**
     * Delete multiple stories
     */
    suspend fun deleteStories(stories: List<Story>): Int {
        return storyDao.deleteStories(stories)
    }

    /**
     * Delete all stories (for testing/reset)
     */
    suspend fun deleteAllStories(): Int {
        return storyDao.deleteAllStories()
    }

    /**
     * Soft delete story (mark as inactive)
     */
    suspend fun softDeleteStory(storyId: String): Int {
        return storyDao.softDeleteStory(storyId)
    }

    // ==================== Utility Operations ====================

    /**
     * Check if story exists
     */
    suspend fun storyExists(storyId: String): Boolean {
        return storyDao.storyExists(storyId)
    }

    /**
     * Get total story count
     */
    suspend fun getTotalStoryCount(): Int {
        return storyDao.getTotalStoryCount()
    }

    /**
     * Get stories created after timestamp
     */
    suspend fun getStoriesCreatedAfter(timestamp: Long): List<Story> {
        return storyDao.getStoriesCreatedAfter(timestamp)
    }

    /**
     * Get stories updated after timestamp
     */
    suspend fun getStoriesUpdatedAfter(timestamp: Long): List<Story> {
        return storyDao.getStoriesUpdatedAfter(timestamp)
    }

    // ==================== Business Logic Operations ====================

    /**
     * Get recommended stories based on user progress and preferences
     */
    suspend fun getRecommendedStories(
        userAgeRange: String,
        completedStoryIds: List<String>,
        maxDifficultyLevel: Int = 3
    ): List<Story> {
        // Get stories appropriate for age range
        val ageAppropriateStories = getStoriesByAgeRange(userAgeRange)

        // Filter out completed stories
        val uncompletedStories = ageAppropriateStories.filter { story ->
            story.id !in completedStoryIds
        }

        // Filter by difficulty level
        val appropriateDifficultyStories = uncompletedStories.filter { story ->
            story.difficultyLevel <= maxDifficultyLevel
        }

        // Check prerequisites
        val availableStories = appropriateDifficultyStories.filter { story ->
            if (!story.hasPrerequisites) {
                true
            } else {
                story.prerequisites.all { prerequisiteId ->
                    prerequisiteId in completedStoryIds
                }
            }
        }

        // Sort by difficulty level and return
        return availableStories.sortedBy { it.difficultyLevel }
    }

    /**
     * Get stories that feature specific letters for targeted learning
     */
    suspend fun getStoriesForLetterPractice(letters: List<String>): List<Story> {
        val allStories = getAllStories()
        return allStories.filter { story ->
            letters.any { letter ->
                story.targetLetters.contains(letter)
            }
        }.sortedBy { it.difficultyLevel }
    }

    /**
     * Get stories that feature specific phonics words
     */
    suspend fun getStoriesForPhonicsWords(words: List<String>): List<Story> {
        val allStories = getAllStories()
        return allStories.filter { story ->
            words.any { word ->
                story.phonicsWords.contains(word)
            }
        }.sortedBy { it.difficultyLevel }
    }
}
