package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.Activity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Activity entities
 * Phase 12: Content Expansion - Activity management with Room database
 *
 * Provides database operations for learning activities including
 * CRUD operations, filtering by type/category, and adaptive difficulty queries.
 */
@Dao
interface ActivityDao {
    
    // ==================== Query Operations ====================
    
    /**
     * Get all active activities as Flow for reactive UI updates
     */
    @Query("SELECT * FROM activities WHERE isActive = 1 ORDER BY difficultyLevel ASC, type ASC, name ASC")
    fun getAllActivitiesFlow(): Flow<List<Activity>>
    
    /**
     * Get all active activities (non-reactive)
     */
    @Query("SELECT * FROM activities WHERE isActive = 1 ORDER BY difficultyLevel ASC, type ASC, name ASC")
    suspend fun getAllActivities(): List<Activity>
    
    /**
     * Get activity by ID
     */
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): Activity?
    
    /**
     * Get activity by ID as Flow
     */
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityByIdFlow(activityId: String): Flow<Activity?>
    
    /**
     * Get activities by type
     */
    @Query("SELECT * FROM activities WHERE type = :type AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getActivitiesByType(type: String): List<Activity>
    
    /**
     * Get activities by type as Flow
     */
    @Query("SELECT * FROM activities WHERE type = :type AND isActive = 1 ORDER BY difficultyLevel ASC")
    fun getActivitiesByTypeFlow(type: String): Flow<List<Activity>>
    
    /**
     * Get activities by category
     */
    @Query("SELECT * FROM activities WHERE category = :category AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getActivitiesByCategory(category: String): List<Activity>
    
    /**
     * Get activities by category as Flow
     */
    @Query("SELECT * FROM activities WHERE category = :category AND isActive = 1 ORDER BY difficultyLevel ASC")
    fun getActivitiesByCategoryFlow(category: String): Flow<List<Activity>>
    
    /**
     * Get activities by difficulty level
     */
    @Query("SELECT * FROM activities WHERE difficultyLevel = :level AND isActive = 1 ORDER BY type ASC, name ASC")
    suspend fun getActivitiesByDifficulty(level: Int): List<Activity>
    
    /**
     * Get activities by age range
     */
    @Query("SELECT * FROM activities WHERE ageRange = :ageRange AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getActivitiesByAgeRange(ageRange: String): List<Activity>
    
    /**
     * Get activities without prerequisites (unlocked by default)
     */
    @Query("SELECT * FROM activities WHERE prerequisites = '[]' AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getUnlockedActivities(): List<Activity>
    
    /**
     * Get activities without prerequisites as Flow
     */
    @Query("SELECT * FROM activities WHERE prerequisites = '[]' AND isActive = 1 ORDER BY difficultyLevel ASC")
    fun getUnlockedActivitiesFlow(): Flow<List<Activity>>
    
    /**
     * Get activities that support adaptive difficulty
     */
    @Query("SELECT * FROM activities WHERE supportsAdaptiveDifficulty = 1 AND isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getAdaptiveActivities(): List<Activity>
    
    /**
     * Get activities that support adaptive difficulty as Flow
     */
    @Query("SELECT * FROM activities WHERE supportsAdaptiveDifficulty = 1 AND isActive = 1 ORDER BY difficultyLevel ASC")
    fun getAdaptiveActivitiesFlow(): Flow<List<Activity>>
    
    /**
     * Get activities by target skill
     */
    @Query("SELECT * FROM activities WHERE targetSkills LIKE '%' || :skill || '%' AND isActive = 1")
    suspend fun getActivitiesByTargetSkill(skill: String): List<Activity>
    
    /**
     * Get activities by estimated time range
     */
    @Query("SELECT * FROM activities WHERE estimatedTime BETWEEN :minTime AND :maxTime AND isActive = 1 ORDER BY estimatedTime ASC")
    suspend fun getActivitiesByTimeRange(minTime: Int, maxTime: Int): List<Activity>
    
    /**
     * Search activities by name or description
     */
    @Query("""
        SELECT * FROM activities 
        WHERE (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%') 
        AND isActive = 1 
        ORDER BY difficultyLevel ASC
    """)
    suspend fun searchActivities(query: String): List<Activity>
    
    /**
     * Get activity count by type
     */
    @Query("SELECT COUNT(*) FROM activities WHERE type = :type AND isActive = 1")
    suspend fun getActivityCountByType(type: String): Int
    
    /**
     * Get activity count by category
     */
    @Query("SELECT COUNT(*) FROM activities WHERE category = :category AND isActive = 1")
    suspend fun getActivityCountByCategory(category: String): Int
    
    /**
     * Get activity count by difficulty level
     */
    @Query("SELECT COUNT(*) FROM activities WHERE difficultyLevel = :level AND isActive = 1")
    suspend fun getActivityCountByDifficulty(level: Int): Int
    
    /**
     * Get all activity types
     */
    @Query("SELECT DISTINCT type FROM activities WHERE isActive = 1 ORDER BY type ASC")
    suspend fun getAllActivityTypes(): List<String>
    
    /**
     * Get all activity categories
     */
    @Query("SELECT DISTINCT category FROM activities WHERE isActive = 1 ORDER BY category ASC")
    suspend fun getAllActivityCategories(): List<String>
    
    /**
     * Get all difficulty levels
     */
    @Query("SELECT DISTINCT difficultyLevel FROM activities WHERE isActive = 1 ORDER BY difficultyLevel ASC")
    suspend fun getAllDifficultyLevels(): List<Int>
    
    /**
     * Get all age ranges
     */
    @Query("SELECT DISTINCT ageRange FROM activities WHERE isActive = 1 ORDER BY ageRange ASC")
    suspend fun getAllAgeRanges(): List<String>
    
    /**
     * Get activities with highest completion points
     */
    @Query("SELECT * FROM activities WHERE isActive = 1 ORDER BY completionPoints DESC LIMIT :limit")
    suspend fun getTopPointActivities(limit: Int = 10): List<Activity>
    
    /**
     * Get activities by type and difficulty
     */
    @Query("SELECT * FROM activities WHERE type = :type AND difficultyLevel = :level AND isActive = 1 ORDER BY name ASC")
    suspend fun getActivitiesByTypeAndDifficulty(type: String, level: Int): List<Activity>
    
    /**
     * Get activities by category and difficulty
     */
    @Query("SELECT * FROM activities WHERE category = :category AND difficultyLevel = :level AND isActive = 1 ORDER BY name ASC")
    suspend fun getActivitiesByCategoryAndDifficulty(category: String, level: Int): List<Activity>
    
    // ==================== Insert Operations ====================
    
    /**
     * Insert a single activity
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity): Long
    
    /**
     * Insert multiple activities
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<Activity>): List<Long>
    
    // ==================== Update Operations ====================
    
    /**
     * Update an activity
     */
    @Update
    suspend fun updateActivity(activity: Activity): Int
    
    /**
     * Update multiple activities
     */
    @Update
    suspend fun updateActivities(activities: List<Activity>): Int
    
    /**
     * Update activity active status
     */
    @Query("UPDATE activities SET isActive = :isActive, updatedAt = :updatedAt WHERE id = :activityId")
    suspend fun updateActivityActiveStatus(activityId: String, isActive: Boolean, updatedAt: Long = System.currentTimeMillis()): Int
    
    /**
     * Update activity difficulty level
     */
    @Query("UPDATE activities SET difficultyLevel = :level, updatedAt = :updatedAt WHERE id = :activityId")
    suspend fun updateActivityDifficulty(activityId: String, level: Int, updatedAt: Long = System.currentTimeMillis()): Int
    
    /**
     * Update activity completion points
     */
    @Query("UPDATE activities SET completionPoints = :points, updatedAt = :updatedAt WHERE id = :activityId")
    suspend fun updateActivityCompletionPoints(activityId: String, points: Int, updatedAt: Long = System.currentTimeMillis()): Int
    
    // ==================== Delete Operations ====================
    
    /**
     * Delete an activity by ID
     */
    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivityById(activityId: String): Int
    
    /**
     * Delete an activity entity
     */
    @Delete
    suspend fun deleteActivity(activity: Activity): Int
    
    /**
     * Delete multiple activities
     */
    @Delete
    suspend fun deleteActivities(activities: List<Activity>): Int
    
    /**
     * Delete all activities (for testing/reset)
     */
    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities(): Int
    
    /**
     * Soft delete activity (mark as inactive)
     */
    @Query("UPDATE activities SET isActive = 0, updatedAt = :updatedAt WHERE id = :activityId")
    suspend fun softDeleteActivity(activityId: String, updatedAt: Long = System.currentTimeMillis()): Int
    
    // ==================== Utility Operations ====================
    
    /**
     * Check if activity exists
     */
    @Query("SELECT EXISTS(SELECT 1 FROM activities WHERE id = :activityId)")
    suspend fun activityExists(activityId: String): Boolean
    
    /**
     * Get total activity count
     */
    @Query("SELECT COUNT(*) FROM activities WHERE isActive = 1")
    suspend fun getTotalActivityCount(): Int
    
    /**
     * Get activities created after timestamp
     */
    @Query("SELECT * FROM activities WHERE createdAt > :timestamp AND isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActivitiesCreatedAfter(timestamp: Long): List<Activity>
    
    /**
     * Get activities updated after timestamp
     */
    @Query("SELECT * FROM activities WHERE updatedAt > :timestamp AND isActive = 1 ORDER BY updatedAt DESC")
    suspend fun getActivitiesUpdatedAfter(timestamp: Long): List<Activity>
}
