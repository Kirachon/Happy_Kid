package com.happykid.toddlerabc.di

import com.happykid.toddlerabc.data.LearningAnalyticsDao
import com.happykid.toddlerabc.analytics.AdaptiveLearningEngine
import com.happykid.toddlerabc.analytics.AchievementManager
import com.happykid.toddlerabc.analytics.ParentDashboardAnalytics
import com.happykid.toddlerabc.repository.AnalyticsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for analytics and adaptive learning components
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Provides dependency injection for analytics engines, achievement management,
 * parent dashboard analytics, and the central analytics repository.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    /**
     * Provides singleton instance of AdaptiveLearningEngine
     * Handles intelligent difficulty adjustment and personalized learning paths
     */
    @Provides
    @Singleton
    fun provideAdaptiveLearningEngine(
        analyticsDao: LearningAnalyticsDao
    ): AdaptiveLearningEngine {
        return AdaptiveLearningEngine(analyticsDao)
    }

    /**
     * Provides singleton instance of AchievementManager
     * Handles achievement unlocking, progress tracking, and celebration events
     */
    @Provides
    @Singleton
    fun provideAchievementManager(
        analyticsDao: LearningAnalyticsDao
    ): AchievementManager {
        return AchievementManager(analyticsDao)
    }

    /**
     * Provides singleton instance of ParentDashboardAnalytics
     * Handles comprehensive insights and recommendations for parents
     */
    @Provides
    @Singleton
    fun provideParentDashboardAnalytics(
        analyticsDao: LearningAnalyticsDao,
        adaptiveLearningEngine: AdaptiveLearningEngine
    ): ParentDashboardAnalytics {
        return ParentDashboardAnalytics(analyticsDao, adaptiveLearningEngine)
    }

    /**
     * Provides singleton instance of AnalyticsRepository
     * Central repository coordinating all analytics and adaptive learning operations
     */
    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        analyticsDao: LearningAnalyticsDao,
        adaptiveLearningEngine: AdaptiveLearningEngine,
        achievementManager: AchievementManager,
        parentDashboardAnalytics: ParentDashboardAnalytics
    ): AnalyticsRepository {
        return AnalyticsRepository(
            analyticsDao,
            adaptiveLearningEngine,
            achievementManager,
            parentDashboardAnalytics
        )
    }
}
