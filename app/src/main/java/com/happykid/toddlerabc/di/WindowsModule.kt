package com.happykid.toddlerabc.di

import android.content.Context
import com.happykid.toddlerabc.util.WindowsPerformanceProfiler
import com.happykid.toddlerabc.util.WindowsTouchOptimizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Windows-specific utilities
 * Phase 6: Provides Windows emulator optimization components for dependency injection
 *
 * Manages Windows performance profiler, touch optimizer, and other Windows-specific
 * utilities with proper singleton scoping for optimal performance.
 */
@Module
@InstallIn(SingletonComponent::class)
object WindowsModule {

    /**
     * Provides singleton instance of WindowsPerformanceProfiler
     * Handles performance monitoring for Windows emulator environment
     */
    @Provides
    @Singleton
    fun provideWindowsPerformanceProfiler(
        @ApplicationContext context: Context
    ): WindowsPerformanceProfiler {
        return WindowsPerformanceProfiler(context)
    }

    /**
     * Provides singleton instance of WindowsTouchOptimizer
     * Handles touch input optimization for Windows emulator
     */
    @Provides
    @Singleton
    fun provideWindowsTouchOptimizer(
        @ApplicationContext context: Context
    ): WindowsTouchOptimizer {
        return WindowsTouchOptimizer(context)
    }
}
