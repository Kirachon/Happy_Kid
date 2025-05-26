package com.happykid.toddlerabc.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.analytics.ParentDashboardAnalytics
import com.happykid.toddlerabc.analytics.ParentDashboardSummary
import com.happykid.toddlerabc.auth.ParentalAuthManager
import com.happykid.toddlerabc.ui.dashboard.ParentDashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Parent Dashboard Screen
 * Phase 11b: Enhanced Parent Dashboard with Advanced Analytics Visualization
 *
 * Manages dashboard data loading, analytics visualization,
 * data export functionality, and session management.
 */
@HiltViewModel
class ParentDashboardViewModel @Inject constructor(
    private val parentDashboardAnalytics: ParentDashboardAnalytics,
    private val parentalAuthManager: ParentalAuthManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI state for the dashboard
    private val _uiState = MutableStateFlow<ParentDashboardUiState>(ParentDashboardUiState.Loading)
    val uiState: StateFlow<ParentDashboardUiState> = _uiState.asStateFlow()

    /**
     * Load comprehensive dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            try {
                _uiState.value = ParentDashboardUiState.Loading

                // Generate comprehensive dashboard summary
                val dashboardSummary = parentDashboardAnalytics.generateDashboardSummary()

                _uiState.value = ParentDashboardUiState.Success(
                    dashboardSummary = dashboardSummary
                )
            } catch (e: Exception) {
                _uiState.value = ParentDashboardUiState.Error(
                    message = "Failed to load dashboard data: ${e.message}"
                )
            }
        }
    }

    /**
     * Export learning data to CSV file
     */
    fun exportData() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState !is ParentDashboardUiState.Success) {
                    return@launch
                }

                // Generate detailed progress report
                val weeklyReport = parentDashboardAnalytics.generateProgressReport(
                    com.happykid.toddlerabc.analytics.ReportTimeframe.WEEKLY
                )
                val monthlyReport = parentDashboardAnalytics.generateProgressReport(
                    com.happykid.toddlerabc.analytics.ReportTimeframe.MONTHLY
                )

                // Create CSV content
                val csvContent = generateCsvContent(
                    currentState.dashboardSummary,
                    weeklyReport,
                    monthlyReport
                )

                // Save to file and share
                val file = saveCsvFile(csvContent)
                shareFile(file)

            } catch (e: Exception) {
                // Handle export error - could show a toast or update UI state
                _uiState.value = ParentDashboardUiState.Error(
                    message = "Failed to export data: ${e.message}"
                )
            }
        }
    }

    /**
     * End the current parental session
     */
    fun endSession() {
        parentalAuthManager.endSession()
    }

    /**
     * Generate CSV content for export
     */
    private fun generateCsvContent(
        summary: ParentDashboardSummary,
        weeklyReport: com.happykid.toddlerabc.analytics.ProgressReport,
        monthlyReport: com.happykid.toddlerabc.analytics.ProgressReport
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        return buildString {
            appendLine("Happy Kid Learning Report")
            appendLine("Generated: $currentDate")
            appendLine()

            // Summary section
            appendLine("LEARNING SUMMARY")
            appendLine("Overall Engagement,${summary.overallEngagement}%")
            appendLine("Learning Streak,${summary.learningStreak} days")
            appendLine("Active Learning Paths,${summary.activeLearningPaths.size}")
            appendLine()

            // Weekly progress
            appendLine("WEEKLY PROGRESS")
            appendLine("Total Sessions,${summary.weeklyProgress.totalSessions}")
            appendLine("Total Minutes,${summary.weeklyProgress.totalMinutes}")
            appendLine("Average Accuracy,${summary.weeklyProgress.averageAccuracy}%")
            appendLine("Active Days,${summary.weeklyProgress.activeDays}")
            appendLine("New Achievements,${summary.weeklyProgress.newAchievements}")
            appendLine("Improvement Rate,${summary.weeklyProgress.improvementRate}%")
            appendLine()

            // Monthly progress
            appendLine("MONTHLY PROGRESS")
            appendLine("Total Sessions,${summary.monthlyProgress.totalSessions}")
            appendLine("Active Days,${summary.monthlyProgress.activeDays}")
            appendLine("Skills Mastered,${summary.monthlyProgress.masteredSkills}")
            appendLine("Average Engagement,${summary.monthlyProgress.averageEngagement}%")
            appendLine()

            // Recent achievements
            appendLine("RECENT ACHIEVEMENTS")
            appendLine("Achievement,Description,Date Unlocked")
            summary.recentAchievements.forEach { achievement ->
                val achievementDate = dateFormat.format(Date(achievement.unlockedAt))
                appendLine("\"${achievement.title}\",\"${achievement.description}\",$achievementDate")
            }
            appendLine()

            // Key insights
            appendLine("KEY INSIGHTS")
            appendLine("Priority,Title,Description")
            summary.keyInsights.forEach { insight ->
                appendLine("${insight.priority},\"${insight.title}\",\"${insight.description}\"")
            }
            appendLine()

            // Recommended actions
            appendLine("RECOMMENDED ACTIONS")
            summary.recommendedActions.forEachIndexed { index, action ->
                appendLine("${index + 1},\"$action\"")
            }
            appendLine()

            // Next milestones
            appendLine("UPCOMING MILESTONES")
            summary.nextMilestones.forEachIndexed { index, milestone ->
                appendLine("${index + 1},\"$milestone\"")
            }
        }
    }

    /**
     * Save CSV content to file
     */
    private fun saveCsvFile(csvContent: String): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val fileName = "happy_kid_report_$timestamp.csv"

        val file = File(context.getExternalFilesDir(null), fileName)
        FileWriter(file).use { writer ->
            writer.write(csvContent)
        }

        return file
    }

    /**
     * Share the exported file
     */
    private fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Happy Kid Learning Report")
            putExtra(Intent.EXTRA_TEXT, "Learning progress report for your child's educational journey.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Share Learning Report")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }

    /**
     * Refresh dashboard data
     */
    fun refreshData() {
        loadDashboardData()
    }
}
