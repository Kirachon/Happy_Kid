package com.happykid.toddlerabc.analytics

import com.happykid.toddlerabc.model.*
import com.happykid.toddlerabc.data.LearningAnalyticsDao
import com.happykid.toddlerabc.data.DifficultyRecommendation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Adaptive Learning Engine for Happy Kid app
 * Phase 10: Intelligent difficulty adjustment and personalized learning paths
 *
 * Uses machine learning algorithms and statistical analysis to:
 * - Adjust difficulty levels based on performance
 * - Recommend optimal learning content
 * - Personalize learning experiences
 * - Predict learning outcomes
 */
@Singleton
class AdaptiveLearningEngine @Inject constructor(
    private val analyticsDao: LearningAnalyticsDao
) {

    companion object {
        // Algorithm parameters
        private const val ACCURACY_TARGET = 75f // Target accuracy percentage
        private const val ENGAGEMENT_THRESHOLD = 60f // Minimum engagement score
        private const val MASTERY_THRESHOLD = 80f // Accuracy needed for mastery
        private const val DIFFICULTY_ADJUSTMENT_SENSITIVITY = 0.3f
        private const val LEARNING_RATE_WEIGHT = 0.4f
        private const val RETENTION_WEIGHT = 0.3f
        private const val ENGAGEMENT_WEIGHT = 0.3f

        // Time windows for analysis
        private const val RECENT_SESSIONS_WINDOW = 7 * 24 * 60 * 60 * 1000L // 7 days
        private const val TREND_ANALYSIS_WINDOW = 14 * 24 * 60 * 60 * 1000L // 14 days
    }

    /**
     * Analyze performance and recommend difficulty adjustment
     */
    suspend fun recommendDifficultyAdjustment(contentId: String): DifficultyRecommendation {
        val recentSessions = analyticsDao.getSessionsForContent(contentId)
            .filter { it.sessionTimestamp > System.currentTimeMillis() - RECENT_SESSIONS_WINDOW }

        if (recentSessions.isEmpty()) {
            return DifficultyRecommendation(
                currentLevel = 1,
                recommendedLevel = 1,
                reason = "No recent data available",
                confidence = 0.5f
            )
        }

        val currentDifficulty = recentSessions.first().difficultyLevel
        val avgAccuracy = recentSessions.map { it.accuracyPercentage }.average().toFloat()
        val avgEngagement = recentSessions.map { it.engagementScore }.average().toFloat()
        val avgCompletionTime = recentSessions.map { it.sessionDurationMs }.average()
        val improvementRate = calculateImprovementRate(recentSessions)

        // Calculate recommended difficulty using adaptive algorithm
        val difficultyScore = calculateDifficultyScore(avgAccuracy, avgEngagement, improvementRate)
        val recommendedDifficulty = adjustDifficulty(currentDifficulty, difficultyScore)

        val adjustmentReason = determineAdjustmentReason(
            avgAccuracy, avgEngagement, improvementRate, avgCompletionTime
        )

        val confidence = calculateConfidence(recentSessions.size, avgAccuracy, avgEngagement)

        return DifficultyRecommendation(
            currentLevel = currentDifficulty,
            recommendedLevel = recommendedDifficulty,
            reason = adjustmentReason.name,
            confidence = confidence
        )
    }

    /**
     * Calculate optimal learning path for user
     */
    suspend fun generatePersonalizedLearningPath(
        pathType: LearningPathType,
        currentMasteryLevels: Map<String, MasteryLevel>
    ): LearningPath {
        val pathId = "adaptive_${pathType.name.lowercase()}_${System.currentTimeMillis()}"

        // Analyze user's strengths and weaknesses
        val strengthAreas = identifyStrengthAreas(currentMasteryLevels)
        val improvementAreas = identifyImprovementAreas(currentMasteryLevels)

        // Generate adaptive content sequence
        val personalizedSteps = generateAdaptiveSteps(pathType, strengthAreas, improvementAreas)

        return LearningPath(
            pathId = pathId,
            pathName = generatePathName(pathType, currentMasteryLevels),
            pathDescription = generatePathDescription(pathType, improvementAreas),
            pathType = pathType,
            totalSteps = personalizedSteps.size,
            isAdaptive = true,
            difficultyLevel = calculateOptimalStartingDifficulty(currentMasteryLevels),
            personalizedContent = personalizedSteps.joinToString(","),
            estimatedCompletionDays = estimateCompletionTime(personalizedSteps, currentMasteryLevels)
        )
    }

    /**
     * Predict learning outcomes based on current performance
     */
    suspend fun predictLearningOutcomes(contentId: String): LearningPrediction {
        val sessions = analyticsDao.getSessionsForContent(contentId)
        val recentSessions = sessions.filter {
            it.sessionTimestamp > System.currentTimeMillis() - TREND_ANALYSIS_WINDOW
        }

        if (recentSessions.size < 3) {
            return LearningPrediction(
                contentId = contentId,
                predictedMasteryLevel = MasteryLevel.BEGINNER,
                estimatedDaysToMastery = 30,
                confidence = 0.3f,
                recommendedPracticeFrequency = 3
            )
        }

        val learningVelocity = calculateLearningVelocity(recentSessions)
        val currentMastery = estimateCurrentMastery(recentSessions)
        val predictedMastery = predictFutureMastery(learningVelocity, currentMastery)
        val daysToMastery = estimateDaysToMastery(learningVelocity, currentMastery)

        return LearningPrediction(
            contentId = contentId,
            predictedMasteryLevel = predictedMastery,
            estimatedDaysToMastery = daysToMastery,
            confidence = calculatePredictionConfidence(recentSessions),
            recommendedPracticeFrequency = calculateOptimalPracticeFrequency(learningVelocity)
        )
    }

    /**
     * Generate engagement optimization recommendations
     */
    suspend fun optimizeEngagement(userId: String): List<EngagementRecommendation> {
        val recentSessions = analyticsDao.getRecentSessions(
            System.currentTimeMillis() - RECENT_SESSIONS_WINDOW
        )

        val recommendations = mutableListOf<EngagementRecommendation>()

        // Analyze engagement patterns
        val avgEngagement = recentSessions.map { it.engagementScore }.average().toFloat()
        val engagementTrend = calculateEngagementTrend(recentSessions)
        val optimalTimeOfDay = findOptimalLearningTime(recentSessions)
        val preferredActivityTypes = identifyPreferredActivities(recentSessions)

        // Generate recommendations based on analysis
        if (avgEngagement < ENGAGEMENT_THRESHOLD) {
            recommendations.add(
                EngagementRecommendation(
                    type = EngagementRecommendationType.ACTIVITY_VARIETY,
                    title = "Try Different Activities",
                    description = "Mix up learning with different activity types to maintain interest",
                    priority = EngagementPriority.HIGH,
                    estimatedImpact = 0.7f
                )
            )
        }

        if (optimalTimeOfDay != null) {
            recommendations.add(
                EngagementRecommendation(
                    type = EngagementRecommendationType.OPTIMAL_TIMING,
                    title = "Best Learning Time",
                    description = "Your child learns best around ${formatTimeOfDay(optimalTimeOfDay)}",
                    priority = EngagementPriority.MEDIUM,
                    estimatedImpact = 0.5f
                )
            )
        }

        return recommendations
    }

    // ==================== Private Helper Methods ====================

    private fun calculateDifficultyScore(
        accuracy: Float,
        engagement: Float,
        improvementRate: Float
    ): Float {
        val accuracyScore = (accuracy - ACCURACY_TARGET) / 100f
        val engagementScore = (engagement - ENGAGEMENT_THRESHOLD) / 100f
        val improvementScore = improvementRate / 100f

        return (accuracyScore * 0.5f + engagementScore * 0.3f + improvementScore * 0.2f)
    }

    private fun adjustDifficulty(currentDifficulty: Int, difficultyScore: Float): Int {
        val adjustment = (difficultyScore * DIFFICULTY_ADJUSTMENT_SENSITIVITY).roundToInt()
        return (currentDifficulty + adjustment).coerceIn(1, 5)
    }

    private fun calculateImprovementRate(sessions: List<LearningAnalytics>): Float {
        if (sessions.size < 2) return 0f

        val sortedSessions = sessions.sortedBy { it.sessionTimestamp }
        val firstHalf = sortedSessions.take(sessions.size / 2)
        val secondHalf = sortedSessions.drop(sessions.size / 2)

        val firstAvg = firstHalf.map { it.accuracyPercentage }.average().toFloat()
        val secondAvg = secondHalf.map { it.accuracyPercentage }.average().toFloat()

        return secondAvg - firstAvg
    }

    private fun determineAdjustmentReason(
        accuracy: Float,
        engagement: Float,
        improvementRate: Float,
        avgCompletionTime: Double
    ): DifficultyAdjustmentReason {
        return when {
            accuracy > 90f && improvementRate > 10f -> DifficultyAdjustmentReason.HIGH_ACCURACY
            accuracy < 50f -> DifficultyAdjustmentReason.LOW_ACCURACY
            avgCompletionTime < 30000 && accuracy > 80f -> DifficultyAdjustmentReason.FAST_COMPLETION
            avgCompletionTime > 120000 -> DifficultyAdjustmentReason.SLOW_PROGRESS
            engagement < 40f -> DifficultyAdjustmentReason.ENGAGEMENT_DROP
            accuracy > 85f && improvementRate < 5f -> DifficultyAdjustmentReason.MASTERY_ACHIEVED
            improvementRate < -10f -> DifficultyAdjustmentReason.REGRESSION_DETECTED
            else -> DifficultyAdjustmentReason.OPTIMAL_CHALLENGE
        }
    }

    private fun calculateConfidence(sessionCount: Int, accuracy: Float, engagement: Float): Float {
        val sampleSizeConfidence = min(sessionCount / 10f, 1f)
        val dataQualityConfidence = (accuracy + engagement) / 200f
        return (sampleSizeConfidence * 0.6f + dataQualityConfidence * 0.4f).coerceIn(0f, 1f)
    }

    private fun createSupportingData(
        accuracy: Float,
        engagement: Float,
        improvementRate: Float,
        sessionCount: Int
    ): String {
        return """
            {
                "avgAccuracy": $accuracy,
                "avgEngagement": $engagement,
                "improvementRate": $improvementRate,
                "sessionCount": $sessionCount,
                "analysisTimestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()
    }

    private fun identifyStrengthAreas(masteryLevels: Map<String, MasteryLevel>): List<String> {
        return masteryLevels.filter { it.value >= MasteryLevel.PROFICIENT }.keys.toList()
    }

    private fun identifyImprovementAreas(masteryLevels: Map<String, MasteryLevel>): List<String> {
        return masteryLevels.filter { it.value < MasteryLevel.DEVELOPING }.keys.toList()
    }

    private fun generateAdaptiveSteps(
        pathType: LearningPathType,
        strengths: List<String>,
        improvements: List<String>
    ): List<String> {
        // Generate personalized learning steps based on analysis
        val steps = mutableListOf<String>()

        // Start with improvement areas (lower difficulty)
        improvements.forEach { area ->
            steps.add("${area}_basic")
            steps.add("${area}_practice")
        }

        // Add strength reinforcement
        strengths.forEach { area ->
            steps.add("${area}_advanced")
        }

        // Add challenge activities
        steps.add("mixed_review")
        steps.add("assessment")

        return steps
    }

    private fun formatTimeOfDay(hour: Int): String {
        return when {
            hour < 12 -> "${hour}:00 AM"
            hour == 12 -> "12:00 PM"
            else -> "${hour - 12}:00 PM"
        }
    }

    private fun generatePathName(pathType: LearningPathType, masteryLevels: Map<String, MasteryLevel>): String {
        val avgMastery = masteryLevels.values.map { it.ordinal }.average()
        val level = when {
            avgMastery < 1.5 -> "Beginner"
            avgMastery < 2.5 -> "Developing"
            avgMastery < 3.5 -> "Intermediate"
            else -> "Advanced"
        }
        return "$level ${pathType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }}"
    }

    private fun generatePathDescription(pathType: LearningPathType, improvementAreas: List<String>): String {
        val focus = if (improvementAreas.isNotEmpty()) {
            "focusing on ${improvementAreas.take(3).joinToString(", ")}"
        } else {
            "building on current strengths"
        }
        return "Personalized learning path $focus with adaptive difficulty adjustment."
    }

    private fun calculateOptimalStartingDifficulty(masteryLevels: Map<String, MasteryLevel>): Int {
        val avgMastery = masteryLevels.values.map { it.ordinal }.average()
        return (avgMastery + 1).toInt().coerceIn(1, 5)
    }

    private fun estimateCompletionTime(steps: List<String>, masteryLevels: Map<String, MasteryLevel>): Int {
        val baseTime = steps.size * 2 // 2 days per step base
        val masteryFactor = masteryLevels.values.map { it.ordinal }.average() / 4.0
        return (baseTime * (1.5 - masteryFactor)).toInt().coerceAtLeast(7)
    }

    private fun calculateLearningVelocity(sessions: List<LearningAnalytics>): Float {
        if (sessions.size < 2) return 0f
        val sortedSessions = sessions.sortedBy { it.sessionTimestamp }
        val timeSpan = sortedSessions.last().sessionTimestamp - sortedSessions.first().sessionTimestamp
        val accuracyImprovement = sortedSessions.last().accuracyPercentage - sortedSessions.first().accuracyPercentage
        return if (timeSpan > 0) accuracyImprovement / (timeSpan / (24 * 60 * 60 * 1000f)) else 0f
    }

    private fun estimateCurrentMastery(sessions: List<LearningAnalytics>): MasteryLevel {
        val recentAccuracy = sessions.takeLast(5).map { it.accuracyPercentage }.average()
        return when {
            recentAccuracy >= 90 -> MasteryLevel.EXPERT
            recentAccuracy >= 75 -> MasteryLevel.ADVANCED
            recentAccuracy >= 60 -> MasteryLevel.PROFICIENT
            recentAccuracy >= 40 -> MasteryLevel.DEVELOPING
            else -> MasteryLevel.BEGINNER
        }
    }

    private fun predictFutureMastery(velocity: Float, currentMastery: MasteryLevel): MasteryLevel {
        val projectedImprovement = velocity * 7 // 7 days projection
        val currentScore = currentMastery.ordinal * 25f
        val futureScore = (currentScore + projectedImprovement).coerceIn(0f, 100f)
        return when {
            futureScore >= 90 -> MasteryLevel.EXPERT
            futureScore >= 75 -> MasteryLevel.ADVANCED
            futureScore >= 60 -> MasteryLevel.PROFICIENT
            futureScore >= 40 -> MasteryLevel.DEVELOPING
            else -> MasteryLevel.BEGINNER
        }
    }

    private fun estimateDaysToMastery(velocity: Float, currentMastery: MasteryLevel): Int {
        val currentScore = currentMastery.ordinal * 25f
        val targetScore = 80f // Mastery threshold
        return if (velocity > 0) {
            ((targetScore - currentScore) / velocity).toInt().coerceIn(1, 365)
        } else {
            90 // Default estimate
        }
    }

    private fun calculatePredictionConfidence(sessions: List<LearningAnalytics>): Float {
        val consistency = calculateConsistency(sessions)
        val sampleSize = min(sessions.size / 10f, 1f)
        return (consistency * 0.7f + sampleSize * 0.3f).coerceIn(0f, 1f)
    }

    private fun calculateConsistency(sessions: List<LearningAnalytics>): Float {
        if (sessions.size < 2) return 0f
        val accuracies = sessions.map { it.accuracyPercentage }
        val mean = accuracies.average()
        val variance = accuracies.map { (it - mean).pow(2.0) }.average()
        val stdDev = sqrt(variance).toFloat()
        return (1f - (stdDev / 100f)).coerceIn(0f, 1f)
    }

    private fun calculateOptimalPracticeFrequency(velocity: Float): Int {
        return when {
            velocity > 5f -> 5 // High velocity - maintain momentum
            velocity > 2f -> 4 // Good velocity - regular practice
            velocity > 0f -> 3 // Slow velocity - consistent practice
            else -> 2 // No improvement - gentle practice
        }
    }

    private fun calculateEngagementTrend(sessions: List<LearningAnalytics>): Float {
        if (sessions.size < 2) return 0f
        val sortedSessions = sessions.sortedBy { it.sessionTimestamp }
        val firstHalf = sortedSessions.take(sessions.size / 2)
        val secondHalf = sortedSessions.drop(sessions.size / 2)
        val firstAvg = firstHalf.map { it.engagementScore }.average().toFloat()
        val secondAvg = secondHalf.map { it.engagementScore }.average().toFloat()
        return secondAvg - firstAvg
    }

    private fun findOptimalLearningTime(sessions: List<LearningAnalytics>): Int? {
        val timePerformance = sessions.groupBy { it.timeOfDay }
            .mapValues { (_, sessions) -> sessions.map { it.accuracyPercentage }.average() }
        return timePerformance.maxByOrNull { it.value }?.key
    }

    private fun identifyPreferredActivities(sessions: List<LearningAnalytics>): List<LearningActivityType> {
        return sessions.groupBy { it.activityType }
            .mapValues { (_, sessions) -> sessions.map { it.engagementScore }.average() }
            .toList()
            .sortedByDescending { it.second }
            .take(3)
            .map { it.first }
    }
}

// ==================== Supporting Data Classes ====================

data class LearningPrediction(
    val contentId: String,
    val predictedMasteryLevel: MasteryLevel,
    val estimatedDaysToMastery: Int,
    val confidence: Float,
    val recommendedPracticeFrequency: Int
)

data class EngagementRecommendation(
    val type: EngagementRecommendationType,
    val title: String,
    val description: String,
    val priority: EngagementPriority,
    val estimatedImpact: Float
)

enum class EngagementRecommendationType {
    ACTIVITY_VARIETY,
    OPTIMAL_TIMING,
    DIFFICULTY_ADJUSTMENT,
    BREAK_FREQUENCY,
    REWARD_SYSTEM,
    SOCIAL_FEATURES
}

enum class EngagementPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
