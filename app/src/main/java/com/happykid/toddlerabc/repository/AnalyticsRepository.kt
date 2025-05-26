package com.happykid.toddlerabc.repository

import com.happykid.toddlerabc.model.*
import com.happykid.toddlerabc.data.*
import com.happykid.toddlerabc.analytics.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Analytics Repository for Happy Kid app
 * Phase 10: Central repository for all analytics and adaptive learning operations
 *
 * Coordinates between analytics components and provides a unified interface
 * for learning analytics, achievements, and parent dashboard functionality.
 */
@Singleton
class AnalyticsRepository @Inject constructor(
    private val analyticsDao: LearningAnalyticsDao,
    private val adaptiveLearningEngine: AdaptiveLearningEngine,
    private val achievementManager: AchievementManager,
    private val parentDashboardAnalytics: ParentDashboardAnalytics
) {

    // ==================== Learning Analytics Operations ====================

    /**
     * Record a new learning session with automatic achievement checking
     */
    suspend fun recordLearningSession(session: LearningAnalytics): Long {
        val sessionId = analyticsDao.insertLearningSession(session)

        // Automatically check for achievement progress
        achievementManager.checkAchievements(session)

        return sessionId
    }

    /**
     * Get all learning sessions as Flow for reactive UI
     */
    fun getAllLearningSessionsFlow(): Flow<List<LearningAnalytics>> {
        return analyticsDao.getAllLearningSessionsFlow()
    }

    /**
     * Get learning sessions for specific activity type
     */
    fun getSessionsByActivityType(activityType: LearningActivityType): Flow<List<LearningAnalytics>> {
        return analyticsDao.getSessionsByActivityType(activityType)
    }

    /**
     * Get recent learning sessions
     */
    suspend fun getRecentSessions(daysBack: Int = 7): List<LearningAnalytics> {
        val since = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return analyticsDao.getRecentSessions(since)
    }

    /**
     * Get learning analytics for specific content
     */
    suspend fun getContentAnalytics(contentId: String): ContentAnalytics {
        val sessions = analyticsDao.getSessionsForContent(contentId)
        val avgAccuracy = analyticsDao.getAverageAccuracyForContent(contentId) ?: 0f
        val improvementRate = analyticsDao.getImprovementRateForContent(
            contentId,
            System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L),
            System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000L)
        ) ?: 0f

        return ContentAnalytics(
            contentId = contentId,
            totalSessions = sessions.size,
            averageAccuracy = avgAccuracy,
            improvementRate = improvementRate,
            lastSessionTimestamp = sessions.maxOfOrNull { it.sessionTimestamp } ?: 0L,
            masteryLevel = estimateMasteryLevel(avgAccuracy),
            recommendedDifficulty = adaptiveLearningEngine.recommendDifficultyAdjustment(contentId)
        )
    }

    // ==================== Adaptive Learning Operations ====================

    /**
     * Get difficulty recommendation for content
     */
    suspend fun getDifficultyRecommendation(contentId: String): com.happykid.toddlerabc.data.DifficultyRecommendation {
        return adaptiveLearningEngine.recommendDifficultyAdjustment(contentId)
    }

    /**
     * Generate personalized learning path
     */
    suspend fun generatePersonalizedLearningPath(
        pathType: LearningPathType,
        currentMasteryLevels: Map<String, MasteryLevel>
    ): LearningPath {
        val learningPath = adaptiveLearningEngine.generatePersonalizedLearningPath(
            pathType, currentMasteryLevels
        )

        // Save the learning path
        analyticsDao.insertLearningPath(learningPath)

        return learningPath
    }

    /**
     * Predict learning outcomes for content
     */
    suspend fun predictLearningOutcomes(contentId: String): LearningPrediction {
        return adaptiveLearningEngine.predictLearningOutcomes(contentId)
    }

    /**
     * Get engagement optimization recommendations
     */
    suspend fun getEngagementRecommendations(userId: String = "default_user"): List<EngagementRecommendation> {
        return adaptiveLearningEngine.optimizeEngagement(userId)
    }

    // ==================== Achievement Operations ====================

    /**
     * Initialize achievements for new user
     */
    suspend fun initializeAchievements() {
        achievementManager.initializeDefaultAchievements()
    }

    /**
     * Initialize sample analytics data for new users
     * Provides meaningful default data to prevent NaN errors
     */
    suspend fun initializeSampleData() {
        // Check if user already has analytics data
        val existingSessions = analyticsDao.getTotalSessionsCount(0L)
        if (existingSessions > 0) return // User already has data

        // Initialize achievements first
        initializeAchievements()

        // Create sample learning sessions for demonstration
        val currentTime = System.currentTimeMillis()
        val sampleSessions = listOf(
            LearningAnalytics(
                sessionId = "sample_1",
                userId = "default_user",
                activityType = LearningActivityType.ALPHABET_LEARNING,
                sessionTimestamp = currentTime - (2 * 24 * 60 * 60 * 1000L), // 2 days ago
                sessionDurationMs = 10 * 60 * 1000L, // 10 minutes
                accuracyPercentage = 75f,
                completionPercentage = 100f,
                attemptsCount = 3,
                hintsUsed = 1,
                errorsCount = 2,
                contentId = "letter_A",
                difficultyLevel = 1,
                adaptiveDifficultyAdjustment = 0,
                engagementScore = 80f,
                focusTimeMs = 8 * 60 * 1000L,
                distractionEvents = 1,
                masteryLevel = MasteryLevel.DEVELOPING,
                improvementRate = 5f,
                retentionScore = 70f,
                recommendedNextActivity = "letter_B",
                suggestedDifficultyLevel = 1,
                learningPathProgress = 10f,
                timeOfDay = 10, // 10 AM
                dayOfWeek = 2, // Tuesday
                sessionSequenceNumber = 1,
                frameRate = 60f,
                responseTimeMs = 250f,
                devicePerformanceScore = 85f
            )
        )

        // Insert sample sessions
        sampleSessions.forEach { session ->
            analyticsDao.insertLearningSession(session)
        }
    }

    /**
     * Get all achievements with progress
     */
    fun getAllAchievements(): Flow<List<Achievement>> {
        return achievementManager.getAllAchievements()
    }

    /**
     * Get recently unlocked achievements
     */
    suspend fun getRecentAchievements(daysBack: Int = 7): List<Achievement> {
        return achievementManager.getRecentlyUnlockedAchievements(daysBack)
    }

    /**
     * Get achievements close to completion
     */
    suspend fun getAchievementsNearCompletion(): List<Achievement> {
        return achievementManager.getAchievementsNearCompletion()
    }

    // ==================== Learning Path Operations ====================

    /**
     * Get all learning paths
     */
    fun getAllLearningPaths(): Flow<List<LearningPath>> {
        return analyticsDao.getAllLearningPathsFlow()
    }

    /**
     * Get active learning paths
     */
    suspend fun getActiveLearningPaths(): List<LearningPath> {
        return analyticsDao.getActiveLearningPaths()
    }

    /**
     * Update learning path progress
     */
    suspend fun updateLearningPathProgress(
        pathId: String,
        stepIndex: Int,
        percentage: Float
    ) {
        analyticsDao.updateLearningPathProgress(
            pathId, stepIndex, percentage, System.currentTimeMillis()
        )
    }

    /**
     * Complete learning path
     */
    suspend fun completeLearningPath(pathId: String) {
        analyticsDao.completeLearningPath(pathId, System.currentTimeMillis())
    }

    // ==================== Parent Dashboard Operations ====================

    /**
     * Generate comprehensive dashboard summary
     */
    suspend fun getDashboardSummary(): ParentDashboardSummary {
        return parentDashboardAnalytics.generateDashboardSummary()
    }

    /**
     * Generate detailed progress report
     */
    suspend fun generateProgressReport(timeframe: ReportTimeframe): ProgressReport {
        return parentDashboardAnalytics.generateProgressReport(timeframe)
    }

    /**
     * Get personalized recommendations for parents
     */
    suspend fun getParentRecommendations(): List<ParentRecommendation> {
        return parentDashboardAnalytics.generateParentRecommendations()
    }

    /**
     * Export learning data for educational assessment
     */
    suspend fun exportLearningData(format: ExportFormat): LearningDataExport {
        return parentDashboardAnalytics.exportLearningData(format)
    }

    // ==================== Analytics Summary Operations ====================

    /**
     * Get overall learning statistics
     */
    suspend fun getOverallStatistics(daysBack: Int = 30): OverallStatistics {
        val since = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)

        val totalTime = analyticsDao.getTotalLearningTime(since) ?: 0L
        val totalSessions = analyticsDao.getTotalSessionsCount(since)
        val avgDuration = analyticsDao.getAverageSessionDuration(since) ?: 0L
        val avgAccuracy = analyticsDao.getOverallAccuracy(since) ?: 0f
        val masteryDistribution = analyticsDao.getMasteryLevelDistribution()
        val contentMastery = analyticsDao.getContentMasterySummary(since)

        return OverallStatistics(
            totalLearningTime = totalTime,
            totalSessions = totalSessions,
            averageSessionDuration = avgDuration,
            averageAccuracy = avgAccuracy,
            masteryDistribution = masteryDistribution,
            contentMastery = contentMastery,
            analysisTimeframe = daysBack
        )
    }

    /**
     * Get engagement trends over time
     */
    suspend fun getEngagementTrends(daysBack: Int = 14): List<EngagementTrend> {
        val since = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return analyticsDao.getEngagementTrends(since)
    }

    /**
     * Get performance by time of day
     */
    suspend fun getPerformanceByTimeOfDay(daysBack: Int = 14): List<TimeOfDayPerformance> {
        val since = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return analyticsDao.getPerformanceByTimeOfDay(since)
    }

    /**
     * Get learning velocity (daily progress)
     */
    suspend fun getLearningVelocity(daysBack: Int = 14): List<DailyLearningStats> {
        val since = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return analyticsDao.getLearningVelocity(since)
    }

    // ==================== Data Management Operations ====================

    /**
     * Clean up old analytics data
     */
    suspend fun cleanupOldData(daysToKeep: Int = 365) {
        val cutoffTimestamp = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        analyticsDao.deleteOldAnalyticsData(cutoffTimestamp)
    }

    // ==================== Helper Methods ====================

    private fun estimateMasteryLevel(accuracy: Float): MasteryLevel {
        return when {
            accuracy >= 90f -> MasteryLevel.EXPERT
            accuracy >= 75f -> MasteryLevel.ADVANCED
            accuracy >= 60f -> MasteryLevel.PROFICIENT
            accuracy >= 40f -> MasteryLevel.DEVELOPING
            else -> MasteryLevel.BEGINNER
        }
    }
}

// ==================== Supporting Data Classes ====================

data class ContentAnalytics(
    val contentId: String,
    val totalSessions: Int,
    val averageAccuracy: Float,
    val improvementRate: Float,
    val lastSessionTimestamp: Long,
    val masteryLevel: MasteryLevel,
    val recommendedDifficulty: com.happykid.toddlerabc.data.DifficultyRecommendation
)

data class OverallStatistics(
    val totalLearningTime: Long,
    val totalSessions: Int,
    val averageSessionDuration: Long,
    val averageAccuracy: Float,
    val masteryDistribution: List<MasteryLevelCount>,
    val contentMastery: List<ContentMastery>,
    val analysisTimeframe: Int
)
