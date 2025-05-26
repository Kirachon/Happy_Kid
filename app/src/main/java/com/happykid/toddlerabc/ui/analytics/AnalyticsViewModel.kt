package com.happykid.toddlerabc.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.repository.AnalyticsRepository
import com.happykid.toddlerabc.analytics.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Analytics Screen
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Manages analytics data, achievements, and parent dashboard insights
 * with reactive UI state management and error handling.
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    /**
     * Load comprehensive analytics data
     */
    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _uiState.value = AnalyticsUiState.Loading

                // Initialize sample data for new users to prevent NaN errors
                analyticsRepository.initializeSampleData()

                // Load all analytics data concurrently
                val overallStats = analyticsRepository.getOverallStatistics(30)
                val dashboardSummary = analyticsRepository.getDashboardSummary()
                val recentAchievements = analyticsRepository.getRecentAchievements(7)
                val parentRecommendations = analyticsRepository.getParentRecommendations()
                val engagementTrends = analyticsRepository.getEngagementTrends(14)

                // Convert to UI models with NaN/null safety
                val uiState = AnalyticsUiState.Success(
                    overallStats = OverallStatisticsUi(
                        totalSessions = overallStats.totalSessions,
                        averageAccuracy = if (overallStats.averageAccuracy.isNaN()) 0f else overallStats.averageAccuracy,
                        totalHours = if (overallStats.totalLearningTime > 0) (overallStats.totalLearningTime / 3600000).toInt() else 0,
                        masteryDistribution = overallStats.masteryDistribution.map {
                            MasteryDistributionUi(it.masteryLevel.name, it.count)
                        }
                    ),
                    weeklyProgress = WeeklyProgressUi(
                        sessionsThisWeek = dashboardSummary.weeklyProgress.totalSessions,
                        minutesThisWeek = dashboardSummary.weeklyProgress.totalMinutes,
                        improvementRate = if (dashboardSummary.weeklyProgress.improvementRate.isNaN()) 0f else dashboardSummary.weeklyProgress.improvementRate,
                        activeDays = dashboardSummary.weeklyProgress.activeDays
                    ),
                    recentAchievements = recentAchievements.map { achievement ->
                        AchievementUi(
                            title = achievement.title,
                            description = achievement.description,
                            category = achievement.category.name,
                            pointsAwarded = achievement.pointsAwarded,
                            unlockedDate = achievement.unlockedTimestamp ?: 0L
                        )
                    },
                    parentRecommendations = parentRecommendations.map { recommendation ->
                        RecommendationUi(
                            title = recommendation.title,
                            description = recommendation.description,
                            priority = recommendation.priority.name,
                            actionItems = recommendation.actionItems
                        )
                    },
                    engagementTrends = engagementTrends.map { trend ->
                        TrendUi(
                            date = trend.date,
                            value = if (trend.avgEngagement.isNaN()) 0f else trend.avgEngagement,
                            sessionCount = trend.sessionCount
                        )
                    }
                )

                _uiState.value = uiState

            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    /**
     * Initialize achievements for new users
     */
    fun initializeAchievements() {
        viewModelScope.launch {
            try {
                analyticsRepository.initializeAchievements()
                loadAnalytics() // Refresh data after initialization
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = "Failed to initialize achievements: ${e.message}"
                )
            }
        }
    }

    /**
     * Export learning data for educational assessment
     */
    fun exportLearningData(format: ExportFormat) {
        viewModelScope.launch {
            try {
                val exportData = analyticsRepository.exportLearningData(format)
                // In a real implementation, this would save the file or share it
                // For now, we'll just log the export
                println("Learning data exported: ${exportData.totalSessions} sessions")
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = "Failed to export data: ${e.message}"
                )
            }
        }
    }

    /**
     * Clean up old analytics data
     */
    fun cleanupOldData() {
        viewModelScope.launch {
            try {
                analyticsRepository.cleanupOldData(365) // Keep 1 year of data
                loadAnalytics() // Refresh after cleanup
            } catch (e: Exception) {
                _uiState.value = AnalyticsUiState.Error(
                    message = "Failed to cleanup data: ${e.message}"
                )
            }
        }
    }
}

// UI State classes are now defined in AnalyticsUiState.kt
