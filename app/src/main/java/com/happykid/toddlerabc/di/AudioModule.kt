package com.happykid.toddlerabc.di

import android.content.Context
import com.happykid.toddlerabc.audio.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for audio dependencies
 * Phase 4: Provides audio components for dependency injection
 * 
 * This module provides AudioManager and related audio components
 * while maintaining Windows-specific optimizations and compatibility.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    /**
     * Provides singleton instance of AudioManager
     * Maintains Windows emulator compatibility and proper resource management
     */
    @Provides
    @Singleton
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager {
        return AudioManager(context)
    }
}
