package com.happykid.toddlerabc.di

import android.content.Context
import com.happykid.toddlerabc.permission.PermissionManager
import com.happykid.toddlerabc.speech.SpeechRecognitionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for speech recognition dependencies
 * Phase 7: Provides speech recognition and permission components for dependency injection
 * 
 * This module provides speech recognition components while maintaining Windows-specific
 * optimizations and compatibility with existing audio and performance systems.
 */
@Module
@InstallIn(SingletonComponent::class)
object SpeechModule {

    /**
     * Provides singleton instance of SpeechRecognitionManager
     * Maintains Windows emulator compatibility and proper resource management
     */
    @Provides
    @Singleton
    fun provideSpeechRecognitionManager(
        @ApplicationContext context: Context
    ): SpeechRecognitionManager {
        return SpeechRecognitionManager(context)
    }

    /**
     * Provides singleton instance of PermissionManager
     * Handles microphone permissions with parental consent flow
     */
    @Provides
    @Singleton
    fun providePermissionManager(
        @ApplicationContext context: Context
    ): PermissionManager {
        return PermissionManager(context)
    }
}
