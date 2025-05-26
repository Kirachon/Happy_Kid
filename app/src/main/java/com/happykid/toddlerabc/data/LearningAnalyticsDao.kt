package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Learning Analytics
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Provides database operations for learning analytics, achievements,
 * and adaptive learning data with optimized queries for insights generation.
 */
@Dao
interface LearningAnalyticsDao {

    // ==================== Learning Analytics Operations ====================

    /**
     * Insert a new learning analytics session
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningSession(session: LearningAnalytics): Long

    /**
     * Get all learning sessions as Flow for reactive UI
     */
    @Query("SELECT * FROM learning_analytics ORDER BY sessionTimestamp DESC")
    fun getAllLearningSessionsFlow(): Flow<List<LearningAnalytics>>

    /**
     * Get learning sessions for a specific activity type
     */
    @Query("SELECT * FROM learning_analytics WHERE activityType = :activityType ORDER BY sessionTimestamp DESC")
    fun getSessionsByActivityType(activityType: LearningActivityType): Flow<List<LearningAnalytics>>

    /**
     * Get recent learning sessions (last N days)
     */
    @Query("SELECT * FROM learning_analytics WHERE sessionTimestamp > :since ORDER BY sessionTimestamp DESC")
    suspend fun getRecentSessions(since: Long): List<LearningAnalytics>

    /**
     * Get learning sessions for specific content
     */
    @Query("SELECT * FROM learning_analytics WHERE contentId = :contentId ORDER BY sessionTimestamp DESC")
    suspend fun getSessionsForContent(contentId: String): List<LearningAnalytics>

    /**
     * Get average accuracy for content
     */
    @Query("SELECT AVG(accuracyPercentage) FROM learning_analytics WHERE contentId = :contentId")
    suspend fun getAverageAccuracyForContent(contentId: String): Float?

    /**
     * Get improvement rate for content over time
     */
    @Query("""
        SELECT
            (AVG(CASE WHEN sessionTimestamp > :recentThreshold THEN accuracyPercentage END) -
             AVG(CASE WHEN sessionTimestamp <= :recentThreshold THEN accuracyPercentage END)) as improvement
        FROM learning_analytics
        WHERE contentId = :contentId AND sessionTimestamp > :oldestThreshold
    """)
    suspend fun getImprovementRateForContent(contentId: String, recentThreshold: Long, oldestThreshold: Long): Float?

    /**
     * Get mastery level distribution
     */
    @Query("SELECT masteryLevel, COUNT(*) as count FROM learning_analytics GROUP BY masteryLevel")
    suspend fun getMasteryLevelDistribution(): List<MasteryLevelCount>

    /**
     * Get engagement trends over time
     */
    @Query("""
        SELECT
            DATE(sessionTimestamp/1000, 'unixepoch') as date,
            AVG(engagementScore) as avgEngagement,
            COUNT(*) as sessionCount
        FROM learning_analytics
        WHERE sessionTimestamp > :since
        GROUP BY DATE(sessionTimestamp/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getEngagementTrends(since: Long): List<EngagementTrend>

    /**
     * Get performance by time of day
     */
    @Query("""
        SELECT
            timeOfDay,
            AVG(accuracyPercentage) as avgAccuracy,
            AVG(engagementScore) as avgEngagement,
            COUNT(*) as sessionCount
        FROM learning_analytics
        WHERE sessionTimestamp > :since
        GROUP BY timeOfDay
        ORDER BY timeOfDay
    """)
    suspend fun getPerformanceByTimeOfDay(since: Long): List<TimeOfDayPerformance>

    /**
     * Get learning velocity (sessions per day)
     */
    @Query("""
        SELECT
            DATE(sessionTimestamp/1000, 'unixepoch') as date,
            COUNT(*) as sessionCount,
            SUM(sessionDurationMs) as totalTimeMs
        FROM learning_analytics
        WHERE sessionTimestamp > :since
        GROUP BY DATE(sessionTimestamp/1000, 'unixepoch')
        ORDER BY date DESC
    """)
    suspend fun getLearningVelocity(since: Long): List<DailyLearningStats>

    // ==================== Achievement Operations ====================

    /**
     * Insert a new achievement
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement): Long

    /**
     * Get all achievements as Flow
     */
    @Query("SELECT * FROM achievements ORDER BY unlockedTimestamp DESC, achievementId ASC")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    /**
     * Get unlocked achievements
     */
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 ORDER BY unlockedTimestamp DESC")
    suspend fun getUnlockedAchievements(): List<Achievement>

    /**
     * Get achievements by category
     */
    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY progressPercentage DESC")
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement>

    /**
     * Get achievements close to completion (80%+ progress)
     */
    @Query("SELECT * FROM achievements WHERE progressPercentage >= 80 AND isUnlocked = 0 ORDER BY progressPercentage DESC")
    suspend fun getAchievementsNearCompletion(): List<Achievement>

    /**
     * Get recently unlocked achievements
     */
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1 AND unlockedTimestamp > :since ORDER BY unlockedTimestamp DESC")
    suspend fun getRecentlyUnlockedAchievements(since: Long): List<Achievement>

    /**
     * Update achievement progress
     */
    @Query("UPDATE achievements SET progressCurrent = :progress, progressPercentage = :percentage WHERE achievementId = :achievementId")
    suspend fun updateAchievementProgress(achievementId: String, progress: Int, percentage: Float)

    /**
     * Unlock achievement
     */
    @Query("UPDATE achievements SET isUnlocked = 1, unlockedTimestamp = :timestamp WHERE achievementId = :achievementId")
    suspend fun unlockAchievement(achievementId: String, timestamp: Long)

    // ==================== Learning Path Operations ====================

    /**
     * Insert or update learning path
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLearningPath(path: LearningPath): Long

    /**
     * Get all learning paths as Flow
     */
    @Query("SELECT * FROM learning_paths ORDER BY lastAccessedTimestamp DESC")
    fun getAllLearningPathsFlow(): Flow<List<LearningPath>>

    /**
     * Get active learning paths (started but not completed)
     */
    @Query("SELECT * FROM learning_paths WHERE startedTimestamp IS NOT NULL AND completedTimestamp IS NULL ORDER BY lastAccessedTimestamp DESC")
    suspend fun getActiveLearningPaths(): List<LearningPath>

    /**
     * Get learning path by ID
     */
    @Query("SELECT * FROM learning_paths WHERE pathId = :pathId")
    suspend fun getLearningPath(pathId: String): LearningPath?

    /**
     * Update learning path progress
     */
    @Query("""
        UPDATE learning_paths
        SET currentStepIndex = :stepIndex,
            progressPercentage = :percentage,
            lastAccessedTimestamp = :timestamp
        WHERE pathId = :pathId
    """)
    suspend fun updateLearningPathProgress(pathId: String, stepIndex: Int, percentage: Float, timestamp: Long)

    /**
     * Complete learning path
     */
    @Query("UPDATE learning_paths SET completedTimestamp = :timestamp, progressPercentage = 100 WHERE pathId = :pathId")
    suspend fun completeLearningPath(pathId: String, timestamp: Long)

    // ==================== Analytics Queries ====================

    /**
     * Get total learning time
     */
    @Query("SELECT SUM(sessionDurationMs) FROM learning_analytics WHERE sessionTimestamp > :since")
    suspend fun getTotalLearningTime(since: Long): Long?

    /**
     * Get total sessions count
     */
    @Query("SELECT COUNT(*) FROM learning_analytics WHERE sessionTimestamp > :since")
    suspend fun getTotalSessionsCount(since: Long): Int

    /**
     * Get average session duration
     */
    @Query("SELECT AVG(sessionDurationMs) FROM learning_analytics WHERE sessionTimestamp > :since")
    suspend fun getAverageSessionDuration(since: Long): Long?

    /**
     * Get overall accuracy trend
     */
    @Query("SELECT AVG(accuracyPercentage) FROM learning_analytics WHERE sessionTimestamp > :since")
    suspend fun getOverallAccuracy(since: Long): Float?

    /**
     * Get content mastery summary
     */
    @Query("""
        SELECT
            contentId,
            COUNT(*) as sessionCount,
            AVG(accuracyPercentage) as avgAccuracy,
            MAX(masteryLevel) as highestMastery,
            AVG(difficultyLevel) as avgDifficulty
        FROM learning_analytics
        WHERE sessionTimestamp > :since
        GROUP BY contentId
        ORDER BY avgAccuracy DESC
    """)
    suspend fun getContentMasterySummary(since: Long): List<ContentMastery>

    /**
     * Get learning streaks (consecutive days with sessions)
     */
    @Query("""
        SELECT
            DATE(sessionTimestamp/1000, 'unixepoch') as date,
            COUNT(*) as sessionCount
        FROM learning_analytics
        WHERE sessionTimestamp > :since
        GROUP BY DATE(sessionTimestamp/1000, 'unixepoch')
        HAVING sessionCount > 0
        ORDER BY date DESC
    """)
    suspend fun getLearningStreakData(since: Long): List<DailySessionCount>

    /**
     * Delete old analytics data (for cleanup)
     */
    @Query("DELETE FROM learning_analytics WHERE sessionTimestamp < :cutoffTimestamp")
    suspend fun deleteOldAnalyticsData(cutoffTimestamp: Long)
}

// ==================== Data Classes for Query Results ====================

data class MasteryLevelCount(
    val masteryLevel: MasteryLevel,
    val count: Int
)

data class EngagementTrend(
    val date: String,
    val avgEngagement: Float,
    val sessionCount: Int
)

data class TimeOfDayPerformance(
    val timeOfDay: Int,
    val avgAccuracy: Float,
    val avgEngagement: Float,
    val sessionCount: Int
)

data class DailyLearningStats(
    val date: String,
    val sessionCount: Int,
    val totalTimeMs: Long
)

data class ContentMastery(
    val contentId: String,
    val sessionCount: Int,
    val avgAccuracy: Float,
    val highestMastery: MasteryLevel,
    val avgDifficulty: Float
)

data class DailySessionCount(
    val date: String,
    val sessionCount: Int
)
