package com.happykid.toddlerabc.di

import android.content.Context
import com.happykid.toddlerabc.data.TracingProgressDao
import com.happykid.toddlerabc.haptic.HapticFeedbackManager
import com.happykid.toddlerabc.repository.TracingProgressRepository
import com.happykid.toddlerabc.tracing.TracingTouchDetector

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for tracing system dependencies
 * Phase 8: Provides tracing components for dependency injection
 *
 * This module provides tracing system components while maintaining Windows-specific
 * optimizations and compatibility with existing performance and audio systems.
 */
@Module
@InstallIn(SingletonComponent::class)
object TracingModule {

    /**
     * Provides singleton instance of TracingTouchDetector
     * Maintains Windows emulator compatibility and performance optimization
     */
    @Provides
    @Singleton
    fun provideTracingTouchDetector(): TracingTouchDetector {
        return TracingTouchDetector()
    }

    /**
     * Provides singleton instance of HapticFeedbackManager
     * Handles tactile feedback with Windows emulator simulation
     */
    @Provides
    @Singleton
    fun provideHapticFeedbackManager(
        @ApplicationContext context: Context
    ): HapticFeedbackManager {
        return HapticFeedbackManager(context)
    }

    /**
     * Provides singleton instance of TracingProgressRepository
     * Handles tracing progress data operations with TracingProgressDao dependency
     */
    @Provides
    @Singleton
    fun provideTracingProgressRepository(
        tracingProgressDao: TracingProgressDao
    ): TracingProgressRepository {
        return TracingProgressRepository(tracingProgressDao)
    }
}
