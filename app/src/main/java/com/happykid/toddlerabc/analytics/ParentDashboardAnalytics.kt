package com.happykid.toddlerabc.analytics

import com.happykid.toddlerabc.model.*
import com.happykid.toddlerabc.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Parent Dashboard Analytics for Happy Kid app
 * Phase 10: Comprehensive insights and recommendations for parents
 *
 * Provides actionable insights, progress summaries, and educational
 * recommendations to help parents support their child's learning journey.
 */
@Singleton
class ParentDashboardAnalytics @Inject constructor(
    private val analyticsDao: LearningAnalyticsDao,
    private val adaptiveLearningEngine: AdaptiveLearningEngine
) {

    companion object {
        private const val WEEKLY_ANALYSIS_WINDOW = 7 * 24 * 60 * 60 * 1000L
        private const val MONTHLY_ANALYSIS_WINDOW = 30 * 24 * 60 * 60 * 1000L
        private const val ENGAGEMENT_CONCERN_THRESHOLD = 50f
        private const val ACCURACY_CONCERN_THRESHOLD = 60f
        private const val RECOMMENDED_DAILY_MINUTES = 15
    }

    /**
     * Generate comprehensive dashboard summary
     */
    suspend fun generateDashboardSummary(): ParentDashboardSummary {
        val weeklyData = getWeeklyAnalytics()
        val monthlyData = getMonthlyAnalytics()
        val insights = generateParentInsights()
        val achievements = analyticsDao.getRecentlyUnlockedAchievements(System.currentTimeMillis() - WEEKLY_ANALYSIS_WINDOW)
        val learningPaths = analyticsDao.getActiveLearningPaths()

        return ParentDashboardSummary(
            weeklyProgress = weeklyData,
            monthlyProgress = monthlyData,
            keyInsights = insights.take(5),
            recentAchievements = achievements.take(3),
            activeLearningPaths = learningPaths,
            recommendedActions = generateRecommendedActions(insights),
            nextMilestones = getUpcomingMilestones(),
            overallEngagement = calculateOverallEngagement(weeklyData),
            learningStreak = calculateCurrentLearningStreak()
        )
    }

    /**
     * Generate detailed progress report
     */
    suspend fun generateProgressReport(timeframe: ReportTimeframe): ProgressReport {
        val timeWindow = when (timeframe) {
            ReportTimeframe.WEEKLY -> WEEKLY_ANALYSIS_WINDOW
            ReportTimeframe.MONTHLY -> MONTHLY_ANALYSIS_WINDOW
            ReportTimeframe.QUARTERLY -> 90 * 24 * 60 * 60 * 1000L
        }

        val since = System.currentTimeMillis() - timeWindow
        val sessions = analyticsDao.getRecentSessions(since)

        return ProgressReport(
            timeframe = timeframe,
            totalSessions = sessions.size,
            totalLearningTime = sessions.sumOf { it.sessionDurationMs },
            averageAccuracy = sessions.map { it.accuracyPercentage }.average().toFloat(),
            averageEngagement = sessions.map { it.engagementScore }.average().toFloat(),
            activitiesExplored = sessions.map { it.activityType }.distinct().size,
            masteryProgress = calculateMasteryProgress(sessions),
            strengthAreas = identifyStrengthAreas(sessions),
            improvementAreas = identifyImprovementAreas(sessions),
            learningVelocity = calculateLearningVelocity(sessions),
            consistencyScore = calculateConsistencyScore(sessions),
            engagementTrends = getEngagementTrends(since),
            accuracyTrends = getAccuracyTrends(since)
        )
    }

    /**
     * Generate personalized recommendations for parents
     */
    suspend fun generateParentRecommendations(): List<ParentRecommendation> {
        val recentSessions = analyticsDao.getRecentSessions(
            System.currentTimeMillis() - WEEKLY_ANALYSIS_WINDOW
        )

        val recommendations = mutableListOf<ParentRecommendation>()

        // Analyze engagement patterns (only if there are sessions)
        if (recentSessions.isNotEmpty()) {
            val avgEngagement = recentSessions.map { it.engagementScore }.average().toFloat()
            if (avgEngagement < ENGAGEMENT_CONCERN_THRESHOLD) {
            recommendations.add(
                ParentRecommendation(
                    type = RecommendationType.ENGAGEMENT_BOOST,
                    title = "Boost Learning Engagement",
                    description = "Your child's engagement has been lower recently. Try shorter, more varied sessions.",
                    priority = RecommendationPriority.HIGH,
                    actionItems = listOf(
                        "Try 10-15 minute sessions instead of longer ones",
                        "Mix different activity types in each session",
                        "Celebrate small wins with praise and encouragement",
                        "Let your child choose which letter to practice"
                    ),
                    expectedOutcome = "Increased engagement and enjoyment in learning"
                )
                )
            }

            // Analyze accuracy patterns
            val avgAccuracy = recentSessions.map { it.accuracyPercentage }.average().toFloat()
            if (avgAccuracy < ACCURACY_CONCERN_THRESHOLD) {
            recommendations.add(
                ParentRecommendation(
                    type = RecommendationType.SKILL_SUPPORT,
                    title = "Additional Practice Support",
                    description = "Your child might benefit from additional support with current activities.",
                    priority = RecommendationPriority.MEDIUM,
                    actionItems = listOf(
                        "Practice together during sessions",
                        "Use physical letter cards alongside the app",
                        "Encourage effort over perfection",
                        "Take breaks if frustration occurs"
                    ),
                    expectedOutcome = "Improved accuracy and confidence"
                )
                )
            }
        }

        // Analyze learning consistency
        val dailyStats = analyticsDao.getLearningVelocity(
            System.currentTimeMillis() - WEEKLY_ANALYSIS_WINDOW
        )
        val activeDays = dailyStats.count { it.sessionCount > 0 }

        if (activeDays < 4) {
            recommendations.add(
                ParentRecommendation(
                    type = RecommendationType.CONSISTENCY,
                    title = "Build Learning Routine",
                    description = "Regular practice helps reinforce learning. Aim for 4-5 days per week.",
                    priority = RecommendationPriority.MEDIUM,
                    actionItems = listOf(
                        "Set a consistent daily learning time",
                        "Start with just 10 minutes per day",
                        "Use visual reminders or calendar stickers",
                        "Make it part of your daily routine"
                    ),
                    expectedOutcome = "Better retention and steady progress"
                )
            )
        }

        // Analyze optimal learning times
        val timePerformance = analyticsDao.getPerformanceByTimeOfDay(
            System.currentTimeMillis() - WEEKLY_ANALYSIS_WINDOW
        )
        val bestTime = timePerformance.maxByOrNull { it.avgAccuracy }

        if (bestTime != null && timePerformance.size > 1) {
            recommendations.add(
                ParentRecommendation(
                    type = RecommendationType.TIMING_OPTIMIZATION,
                    title = "Optimal Learning Time",
                    description = "Your child performs best around ${formatTimeOfDay(bestTime.timeOfDay)}.",
                    priority = RecommendationPriority.LOW,
                    actionItems = listOf(
                        "Schedule learning sessions around ${formatTimeOfDay(bestTime.timeOfDay)}",
                        "Ensure your child is well-rested during this time",
                        "Minimize distractions during peak performance hours"
                    ),
                    expectedOutcome = "Maximized learning effectiveness"
                )
            )
        }

        // Add default recommendation for new users
        if (recommendations.isEmpty()) {
            recommendations.add(
                ParentRecommendation(
                    type = RecommendationType.ENGAGEMENT_BOOST,
                    title = "Start Your Learning Journey",
                    description = "Welcome to Happy Kid! Let's begin with some fun alphabet activities.",
                    priority = RecommendationPriority.HIGH,
                    actionItems = listOf(
                        "Start with the Alphabet Learning section",
                        "Try 10-15 minute sessions to begin",
                        "Explore different letters together",
                        "Celebrate every small achievement"
                    ),
                    expectedOutcome = "Build foundation skills and learning confidence"
                )
            )
        }

        return recommendations
    }

    /**
     * Export learning data for educational assessment
     */
    suspend fun exportLearningData(format: ExportFormat): LearningDataExport {
        val allSessions = analyticsDao.getRecentSessions(
            System.currentTimeMillis() - MONTHLY_ANALYSIS_WINDOW
        )

        val achievements = analyticsDao.getUnlockedAchievements()
        val learningPaths = analyticsDao.getActiveLearningPaths()

        return LearningDataExport(
            format = format,
            exportTimestamp = System.currentTimeMillis(),
            totalSessions = allSessions.size,
            dateRange = DateRange(
                start = allSessions.minOfOrNull { it.sessionTimestamp } ?: 0L,
                end = allSessions.maxOfOrNull { it.sessionTimestamp } ?: System.currentTimeMillis()
            ),
            sessionData = allSessions.map { session ->
                SessionExportData(
                    date = session.sessionTimestamp,
                    activityType = session.activityType.name,
                    contentId = session.contentId,
                    accuracy = session.accuracyPercentage,
                    duration = session.sessionDurationMs,
                    engagement = session.engagementScore,
                    masteryLevel = session.masteryLevel.name
                )
            },
            achievementData = achievements.map { achievement ->
                AchievementExportData(
                    title = achievement.title,
                    category = achievement.category.name,
                    unlockedDate = achievement.unlockedTimestamp ?: 0L,
                    pointsAwarded = achievement.pointsAwarded
                )
            },
            summaryStatistics = calculateSummaryStatistics(allSessions)
        )
    }

    // ==================== Private Helper Methods ====================

    private suspend fun getWeeklyAnalytics(): WeeklyAnalytics {
        val since = System.currentTimeMillis() - WEEKLY_ANALYSIS_WINDOW
        val sessions = analyticsDao.getRecentSessions(since)

        return WeeklyAnalytics(
            totalSessions = sessions.size,
            totalMinutes = (sessions.sumOf { it.sessionDurationMs } / 60000).toInt(),
            averageAccuracy = if (sessions.isEmpty()) 0f else sessions.map { it.accuracyPercentage }.average().toFloat(),
            activeDays = sessions.map { it.sessionTimestamp }.distinct().size,
            newAchievements = analyticsDao.getRecentlyUnlockedAchievements(since).size,
            improvementRate = calculateWeeklyImprovement(sessions)
        )
    }

    private suspend fun getMonthlyAnalytics(): MonthlyAnalytics {
        val since = System.currentTimeMillis() - MONTHLY_ANALYSIS_WINDOW
        val sessions = analyticsDao.getRecentSessions(since)

        return MonthlyAnalytics(
            totalSessions = sessions.size,
            totalHours = (sessions.sumOf { it.sessionDurationMs } / 3600000.0).roundToInt(),
            averageAccuracy = if (sessions.isEmpty()) 0f else sessions.map { it.accuracyPercentage }.average().toFloat(),
            activeDays = sessions.map { it.sessionTimestamp }.distinct().size,
            contentMastered = sessions.count { it.masteryLevel >= MasteryLevel.PROFICIENT },
            consistencyScore = calculateConsistencyScore(sessions)
        )
    }

    private suspend fun generateParentInsights(): List<ParentInsight> {
        val insights = mutableListOf<ParentInsight>()
        val recentSessions = analyticsDao.getRecentSessions(
            System.currentTimeMillis() - WEEKLY_ANALYSIS_WINDOW
        )

        // Progress insight
        val avgAccuracy = if (recentSessions.isEmpty()) 0f else recentSessions.map { it.accuracyPercentage }.average().toFloat()
        insights.add(
            ParentInsight(
                insightId = "weekly_progress",
                title = if (recentSessions.isEmpty()) "Getting Started" else "Weekly Progress Summary",
                description = if (recentSessions.isEmpty())
                    "Ready to begin the learning journey! Start with alphabet exploration."
                else
                    "Your child completed ${recentSessions.size} sessions with ${avgAccuracy.roundToInt()}% average accuracy",
                category = InsightCategory.PROGRESS_SUMMARY,
                priority = InsightPriority.HIGH,
                recommendations = if (recentSessions.isEmpty())
                    listOf("Start with alphabet learning", "Try 10-15 minute sessions", "Make learning fun and interactive")
                else
                    listOf("Continue current learning pace", "Celebrate progress achievements")
            )
        )

        // Engagement insight (only if there are sessions)
        if (recentSessions.isNotEmpty()) {
            val avgEngagement = recentSessions.map { it.engagementScore }.average().toFloat()
            if (avgEngagement > 70f) {
                insights.add(
                    ParentInsight(
                        insightId = "high_engagement",
                        title = "Excellent Engagement",
                        description = "Your child is highly engaged with learning activities (${avgEngagement.roundToInt()}% engagement)",
                        category = InsightCategory.ENGAGEMENT_TRENDS,
                        priority = InsightPriority.MEDIUM,
                        recommendations = listOf("Maintain current approach", "Consider introducing new challenges")
                    )
                )
            }
        }

        return insights
    }

    private fun formatTimeOfDay(hour: Int): String {
        return when {
            hour < 12 -> "${hour}:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }

    private fun calculateWeeklyImprovement(sessions: List<LearningAnalytics>): Float {
        if (sessions.size < 2) return 0f
        val sortedSessions = sessions.sortedBy { it.sessionTimestamp }
        val firstHalf = sortedSessions.take(sessions.size / 2)
        val secondHalf = sortedSessions.drop(sessions.size / 2)
        val firstAvg = firstHalf.map { it.accuracyPercentage }.average().toFloat()
        val secondAvg = secondHalf.map { it.accuracyPercentage }.average().toFloat()
        return secondAvg - firstAvg
    }

    private fun calculateConsistencyScore(sessions: List<LearningAnalytics>): Float {
        // Calculate based on regular practice patterns
        val dailyStats = sessions.groupBy {
            it.sessionTimestamp / (24 * 60 * 60 * 1000L)
        }.values.map { it.size }

        if (dailyStats.isEmpty()) return 0f

        val activeDays = dailyStats.size
        val totalDays = 7 // Week analysis
        return (activeDays.toFloat() / totalDays * 100f).coerceAtMost(100f)
    }

    private fun generateRecommendedActions(insights: List<ParentInsight>): List<String> {
        return insights.flatMap { it.recommendations }.take(5)
    }

    private fun getUpcomingMilestones(): List<String> {
        return listOf(
            "Complete 10 tracing sessions",
            "Master 5 new letters",
            "Achieve 7-day learning streak"
        )
    }

    private fun calculateOverallEngagement(weeklyData: WeeklyAnalytics): Float {
        // Simplified calculation based on session frequency and accuracy
        val sessionScore = (weeklyData.totalSessions / 7f * 100f).coerceAtMost(100f)
        val accuracyScore = weeklyData.averageAccuracy
        return (sessionScore * 0.4f + accuracyScore * 0.6f)
    }

    private suspend fun calculateCurrentLearningStreak(): Int {
        val dailyStats = analyticsDao.getLearningStreakData(
            System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        )
        return calculateStreakFromDailyStats(dailyStats)
    }

    private fun calculateStreakFromDailyStats(dailyStats: List<DailySessionCount>): Int {
        val sortedDays = dailyStats.sortedByDescending { it.date }
        var streak = 0
        for (day in sortedDays) {
            if (day.sessionCount > 0) {
                streak++
            } else {
                break
            }
        }
        return streak
    }

    private fun calculateMasteryProgress(sessions: List<LearningAnalytics>): Map<String, Float> {
        return sessions.groupBy { it.contentId }
            .mapValues { (_, contentSessions) ->
                contentSessions.map { it.accuracyPercentage }.average().toFloat()
            }
    }

    private fun identifyStrengthAreas(sessions: List<LearningAnalytics>): List<String> {
        return sessions.groupBy { it.contentId }
            .filter { (_, contentSessions) ->
                contentSessions.map { it.accuracyPercentage }.average() >= 80f
            }
            .keys.toList()
    }

    private fun identifyImprovementAreas(sessions: List<LearningAnalytics>): List<String> {
        return sessions.groupBy { it.contentId }
            .filter { (_, contentSessions) ->
                contentSessions.map { it.accuracyPercentage }.average() < 60f
            }
            .keys.toList()
    }

    private fun calculateLearningVelocity(sessions: List<LearningAnalytics>): Float {
        if (sessions.size < 2) return 0f
        val sortedSessions = sessions.sortedBy { it.sessionTimestamp }
        val timeSpan = sortedSessions.last().sessionTimestamp - sortedSessions.first().sessionTimestamp
        val accuracyImprovement = sortedSessions.last().accuracyPercentage - sortedSessions.first().accuracyPercentage
        return if (timeSpan > 0) accuracyImprovement / (timeSpan / (24 * 60 * 60 * 1000f)) else 0f
    }

    private suspend fun getEngagementTrends(since: Long): List<EngagementTrend> {
        return analyticsDao.getEngagementTrends(since)
    }

    private suspend fun getAccuracyTrends(since: Long): List<AccuracyTrend> {
        // Simplified - would need to add this query to DAO
        return emptyList()
    }

    private fun calculateSummaryStatistics(sessions: List<LearningAnalytics>): SummaryStatistics {
        return SummaryStatistics(
            totalSessions = sessions.size,
            averageAccuracy = sessions.map { it.accuracyPercentage }.average().toFloat(),
            averageEngagement = sessions.map { it.engagementScore }.average().toFloat(),
            totalLearningTime = sessions.sumOf { it.sessionDurationMs },
            activitiesCount = sessions.map { it.activityType }.distinct().size,
            masteredContent = sessions.count { it.masteryLevel >= MasteryLevel.PROFICIENT }
        )
    }
}

// ==================== Data Classes ====================

data class ParentDashboardSummary(
    val weeklyProgress: WeeklyAnalytics,
    val monthlyProgress: MonthlyAnalytics,
    val keyInsights: List<ParentInsight>,
    val recentAchievements: List<Achievement>,
    val activeLearningPaths: List<LearningPath>,
    val recommendedActions: List<String>,
    val nextMilestones: List<String>,
    val overallEngagement: Float,
    val learningStreak: Int
)

data class WeeklyAnalytics(
    val totalSessions: Int,
    val totalMinutes: Int,
    val averageAccuracy: Float,
    val activeDays: Int,
    val newAchievements: Int,
    val improvementRate: Float
)

data class MonthlyAnalytics(
    val totalSessions: Int,
    val totalHours: Int,
    val averageAccuracy: Float,
    val activeDays: Int,
    val contentMastered: Int,
    val consistencyScore: Float,
    val masteredSkills: Int = contentMastered,
    val averageEngagement: Float = 0f
)

data class ParentRecommendation(
    val type: RecommendationType,
    val title: String,
    val description: String,
    val priority: RecommendationPriority,
    val actionItems: List<String>,
    val expectedOutcome: String
)

enum class RecommendationType {
    ENGAGEMENT_BOOST,
    SKILL_SUPPORT,
    CONSISTENCY,
    TIMING_OPTIMIZATION,
    CONTENT_VARIETY,
    DIFFICULTY_ADJUSTMENT
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class ReportTimeframe {
    WEEKLY,
    MONTHLY,
    QUARTERLY
}

enum class ExportFormat {
    JSON,
    CSV,
    PDF
}



// Additional data classes
data class ProgressReport(
    val timeframe: ReportTimeframe,
    val totalSessions: Int,
    val totalLearningTime: Long,
    val averageAccuracy: Float,
    val averageEngagement: Float,
    val activitiesExplored: Int,
    val masteryProgress: Map<String, Float>,
    val strengthAreas: List<String>,
    val improvementAreas: List<String>,
    val learningVelocity: Float,
    val consistencyScore: Float,
    val engagementTrends: List<EngagementTrend>,
    val accuracyTrends: List<AccuracyTrend>
)

data class LearningDataExport(
    val format: ExportFormat,
    val exportTimestamp: Long,
    val totalSessions: Int,
    val dateRange: DateRange,
    val sessionData: List<SessionExportData>,
    val achievementData: List<AchievementExportData>,
    val summaryStatistics: SummaryStatistics
)

data class DateRange(
    val start: Long,
    val end: Long
)

data class SessionExportData(
    val date: Long,
    val activityType: String,
    val contentId: String,
    val accuracy: Float,
    val duration: Long,
    val engagement: Float,
    val masteryLevel: String
)

data class AchievementExportData(
    val title: String,
    val category: String,
    val unlockedDate: Long,
    val pointsAwarded: Int
)

data class SummaryStatistics(
    val totalSessions: Int,
    val averageAccuracy: Float,
    val averageEngagement: Float,
    val totalLearningTime: Long,
    val activitiesCount: Int,
    val masteredContent: Int
)

data class AccuracyTrend(
    val date: String,
    val avgAccuracy: Float,
    val sessionCount: Int
)
