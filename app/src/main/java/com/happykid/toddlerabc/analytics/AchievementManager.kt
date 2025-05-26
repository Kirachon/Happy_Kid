package com.happykid.toddlerabc.analytics

import com.happykid.toddlerabc.model.*
import com.happykid.toddlerabc.data.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log

/**
 * Achievement Manager for Happy Kid app
 * Phase 10: Milestone tracking and celebration system
 *
 * Manages achievement unlocking, progress tracking, and celebration events
 * to motivate and engage young learners through positive reinforcement.
 */
@Singleton
class AchievementManager @Inject constructor(
    private val analyticsDao: LearningAnalyticsDao
) {

    companion object {
        private const val TAG = "AchievementManager"

        // Achievement thresholds
        private const val FIRST_SESSION_THRESHOLD = 1
        private const val CONSISTENCY_THRESHOLD = 7 // days
        private const val ACCURACY_EXCELLENCE_THRESHOLD = 90f
        private const val SPEED_THRESHOLD = 30000L // 30 seconds
        private const val MASTERY_THRESHOLD = 5 // letters/words
    }

    /**
     * Initialize default achievements for new users
     */
    suspend fun initializeDefaultAchievements() {
        val defaultAchievements = createDefaultAchievements()
        defaultAchievements.forEach { achievement ->
            analyticsDao.insertAchievement(achievement)
        }
        Log.d(TAG, "Initialized ${defaultAchievements.size} default achievements")
    }

    /**
     * Check and update achievement progress based on learning session
     */
    suspend fun checkAchievements(session: LearningAnalytics) {
        Log.d(TAG, "Checking achievements for session: ${session.sessionId}")

        // Get all achievements that might be affected
        val allAchievements = analyticsDao.getAllAchievementsFlow()

        // Check each achievement type
        checkFirstSessionAchievements(session)
        checkAccuracyAchievements(session)
        checkConsistencyAchievements(session)
        checkSpeedAchievements(session)
        checkMasteryAchievements(session)
        checkSpecialAchievements(session)
    }

    /**
     * Get all achievements with current progress
     */
    fun getAllAchievements(): Flow<List<Achievement>> {
        return analyticsDao.getAllAchievementsFlow()
    }

    /**
     * Get recently unlocked achievements
     */
    suspend fun getRecentlyUnlockedAchievements(daysBack: Int = 7): List<Achievement> {
        val since = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000L)
        return analyticsDao.getUnlockedAchievements()
            .filter { it.unlockedTimestamp != null && it.unlockedTimestamp!! > since }
    }

    /**
     * Get achievements close to completion for motivation
     */
    suspend fun getAchievementsNearCompletion(): List<Achievement> {
        return analyticsDao.getAchievementsNearCompletion()
    }

    /**
     * Unlock achievement and trigger celebration
     */
    suspend fun unlockAchievement(achievementId: String): Achievement? {
        val timestamp = System.currentTimeMillis()
        analyticsDao.unlockAchievement(achievementId, timestamp)

        // Get the unlocked achievement for celebration
        val achievements = analyticsDao.getAllAchievementsFlow()
        // Note: In a real implementation, we'd get the specific achievement
        Log.d(TAG, "Achievement unlocked: $achievementId")

        return null // Simplified for now
    }

    // ==================== Private Achievement Checking Methods ====================

    private suspend fun checkFirstSessionAchievements(session: LearningAnalytics) {
        when (session.activityType) {
            LearningActivityType.ALPHABET_LEARNING -> {
                updateAchievementProgress("first_alphabet_session", 1, 100f)
            }
            LearningActivityType.LETTER_TRACING -> {
                updateAchievementProgress("first_tracing_session", 1, 100f)
            }
            LearningActivityType.PHONICS_PRACTICE -> {
                updateAchievementProgress("first_phonics_session", 1, 100f)
            }
            else -> { /* Other activity types */ }
        }
    }

    private suspend fun checkAccuracyAchievements(session: LearningAnalytics) {
        if (session.accuracyPercentage >= ACCURACY_EXCELLENCE_THRESHOLD) {
            // Perfect session achievement
            incrementAchievementProgress("perfect_sessions", 1)

            // High accuracy streak
            val recentSessions = analyticsDao.getRecentSessions(
                System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
            )
            val consecutivePerfect = countConsecutivePerfectSessions(recentSessions)
            updateAchievementProgress("accuracy_streak_5", consecutivePerfect,
                (consecutivePerfect / 5f * 100f).coerceAtMost(100f))
        }
    }

    private suspend fun checkConsistencyAchievements(session: LearningAnalytics) {
        val recentDays = analyticsDao.getLearningStreakData(
            System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        )

        val currentStreak = calculateCurrentStreak(recentDays)

        // Update consistency achievements
        when {
            currentStreak >= 3 -> updateAchievementProgress("consistency_3_days", currentStreak, 100f)
            currentStreak >= 7 -> updateAchievementProgress("consistency_week", currentStreak, 100f)
            currentStreak >= 14 -> updateAchievementProgress("consistency_2_weeks", currentStreak, 100f)
            currentStreak >= 30 -> updateAchievementProgress("consistency_month", currentStreak, 100f)
        }
    }

    private suspend fun checkSpeedAchievements(session: LearningAnalytics) {
        if (session.sessionDurationMs <= SPEED_THRESHOLD && session.accuracyPercentage >= 80f) {
            incrementAchievementProgress("speed_demon", 1)
        }
    }

    private suspend fun checkMasteryAchievements(session: LearningAnalytics) {
        if (session.masteryLevel >= MasteryLevel.PROFICIENT) {
            when (session.activityType) {
                LearningActivityType.ALPHABET_LEARNING -> {
                    incrementAchievementProgress("alphabet_master", 1)
                }
                LearningActivityType.LETTER_TRACING -> {
                    incrementAchievementProgress("tracing_master", 1)
                }
                LearningActivityType.PHONICS_PRACTICE -> {
                    incrementAchievementProgress("phonics_master", 1)
                }
                else -> { /* Other types */ }
            }
        }
    }

    private suspend fun checkSpecialAchievements(session: LearningAnalytics) {
        // Check for exploration achievement
        val activityTypes = analyticsDao.getRecentSessions(
            System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        ).map { it.activityType }.distinct()

        if (activityTypes.size >= 3) {
            updateAchievementProgress("explorer", activityTypes.size,
                (activityTypes.size / 5f * 100f).coerceAtMost(100f))
        }
    }

    // ==================== Helper Methods ====================

    private suspend fun updateAchievementProgress(achievementId: String, progress: Int, percentage: Float) {
        analyticsDao.updateAchievementProgress(achievementId, progress, percentage)

        // Check if achievement should be unlocked
        if (percentage >= 100f) {
            unlockAchievement(achievementId)
        }
    }

    private suspend fun incrementAchievementProgress(achievementId: String, increment: Int) {
        // In a real implementation, we'd get current progress and increment it
        // For now, simplified
        Log.d(TAG, "Incrementing achievement progress: $achievementId by $increment")
    }

    private fun countConsecutivePerfectSessions(sessions: List<LearningAnalytics>): Int {
        val sortedSessions = sessions.sortedByDescending { it.sessionTimestamp }
        var count = 0
        for (session in sortedSessions) {
            if (session.accuracyPercentage >= ACCURACY_EXCELLENCE_THRESHOLD) {
                count++
            } else {
                break
            }
        }
        return count
    }

    private fun calculateCurrentStreak(dailyStats: List<DailySessionCount>): Int {
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

    private fun createDefaultAchievements(): List<Achievement> {
        return listOf(
            // First session achievements
            Achievement(
                achievementId = "first_alphabet_session",
                title = "First Steps!",
                description = "Complete your first alphabet learning session",
                category = AchievementCategory.ALPHABET_MASTERY,
                iconResource = "ic_first_steps",
                pointsAwarded = 10,
                progressTarget = 1,
                requirementType = AchievementRequirement.COMPLETE_SESSIONS,
                requirementValue = "1",
                celebrationMessage = "Welcome to learning! You've taken your first step! 🎉"
            ),

            Achievement(
                achievementId = "first_tracing_session",
                title = "Tracing Beginner!",
                description = "Complete your first letter tracing session",
                category = AchievementCategory.TRACING_SKILLS,
                iconResource = "ic_tracing_start",
                pointsAwarded = 10,
                progressTarget = 1,
                requirementType = AchievementRequirement.COMPLETE_SESSIONS,
                requirementValue = "1",
                celebrationMessage = "Great job tracing your first letter! ✏️"
            ),

            // Accuracy achievements
            Achievement(
                achievementId = "perfect_sessions",
                title = "Perfect Practice!",
                description = "Complete 10 sessions with 90%+ accuracy",
                category = AchievementCategory.SPEED_ACCURACY,
                iconResource = "ic_perfect",
                pointsAwarded = 50,
                progressTarget = 10,
                requirementType = AchievementRequirement.ACCURACY_THRESHOLD,
                requirementValue = "90",
                celebrationMessage = "Amazing accuracy! You're becoming an expert! ⭐"
            ),

            // Consistency achievements
            Achievement(
                achievementId = "consistency_week",
                title = "Week Warrior!",
                description = "Practice for 7 consecutive days",
                category = AchievementCategory.CONSISTENCY,
                iconResource = "ic_calendar_week",
                pointsAwarded = 30,
                progressTarget = 7,
                requirementType = AchievementRequirement.CONSECUTIVE_DAYS,
                requirementValue = "7",
                celebrationMessage = "One week of consistent learning! Keep it up! 📅"
            ),

            // Mastery achievements
            Achievement(
                achievementId = "alphabet_master",
                title = "Alphabet Expert!",
                description = "Master 10 letters",
                category = AchievementCategory.ALPHABET_MASTERY,
                iconResource = "ic_alphabet_crown",
                pointsAwarded = 100,
                progressTarget = 10,
                requirementType = AchievementRequirement.MASTER_CONTENT,
                requirementValue = "10",
                celebrationMessage = "You've mastered the alphabet! You're amazing! 👑"
            ),

            // Exploration achievement
            Achievement(
                achievementId = "explorer",
                title = "Learning Explorer!",
                description = "Try 5 different learning activities",
                category = AchievementCategory.EXPLORATION,
                iconResource = "ic_explorer",
                pointsAwarded = 25,
                progressTarget = 5,
                requirementType = AchievementRequirement.EXPLORATION,
                requirementValue = "5",
                celebrationMessage = "You love exploring new ways to learn! 🗺️"
            )
        )
    }
}
