package com.happykid.toddlerabc.ui.analytics

/**
 * UI State classes for Analytics Screen
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Defines the UI state hierarchy and data models for the analytics screen
 * to ensure proper separation of concerns and type safety.
 */

// ==================== UI State Classes ====================

sealed class AnalyticsUiState {
    object Loading : AnalyticsUiState()
    
    data class Success(
        val overallStats: OverallStatisticsUi,
        val weeklyProgress: WeeklyProgressUi,
        val recentAchievements: List<AchievementUi>,
        val parentRecommendations: List<RecommendationUi>,
        val engagementTrends: List<TrendUi>
    ) : AnalyticsUiState()
    
    data class Error(val message: String) : AnalyticsUiState()
}

data class OverallStatisticsUi(
    val totalSessions: Int,
    val averageAccuracy: Float,
    val totalHours: Int,
    val masteryDistribution: List<MasteryDistributionUi>
)

data class MasteryDistributionUi(
    val level: String,
    val count: Int
)

data class WeeklyProgressUi(
    val sessionsThisWeek: Int,
    val minutesThisWeek: Int,
    val improvementRate: Float,
    val activeDays: Int
)

data class AchievementUi(
    val title: String,
    val description: String,
    val category: String,
    val pointsAwarded: Int,
    val unlockedDate: Long
)

data class RecommendationUi(
    val title: String,
    val description: String,
    val priority: String,
    val actionItems: List<String>
)

data class TrendUi(
    val date: String,
    val value: Float,
    val sessionCount: Int
)
