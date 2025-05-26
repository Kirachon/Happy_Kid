package com.happykid.toddlerabc.di

import android.content.Context
import androidx.room.Room
import com.happykid.toddlerabc.data.HappyKidDatabase
import com.happykid.toddlerabc.data.LetterDao
import com.happykid.toddlerabc.data.FontPreferenceDao
import com.happykid.toddlerabc.data.TracingProgressDao
import com.happykid.toddlerabc.data.LearningAnalyticsDao
import com.happykid.toddlerabc.data.StoryDao
import com.happykid.toddlerabc.data.VocabularyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module for database dependencies
 * Phase 5: Enhanced with font preferences DAO
 *
 * This module maintains Windows-specific optimizations from Phase 1
 * while enabling proper dependency injection for Phase 2-5.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides application-scoped coroutine scope for database operations
     * Maintains Windows-optimized threading from Phase 1
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    /**
     * Provides singleton instance of HappyKidDatabase
     * Maintains all Windows optimizations and Room configurations from Phase 1
     */
    @Provides
    @Singleton
    fun provideHappyKidDatabase(
        @ApplicationContext context: Context,
        applicationScope: CoroutineScope
    ): HappyKidDatabase {
        return HappyKidDatabase.getDatabase(context, applicationScope)
    }

    /**
     * Provides LetterDao from the database instance
     * Enables injection of DAO into repository
     */
    @Provides
    fun provideLetterDao(database: HappyKidDatabase): LetterDao {
        return database.letterDao()
    }

    /**
     * Provides FontPreferenceDao from the database instance
     * Phase 5: Enables injection of font preference DAO
     */
    @Provides
    fun provideFontPreferenceDao(database: HappyKidDatabase): FontPreferenceDao {
        return database.fontPreferenceDao()
    }

    /**
     * Provides TracingProgressDao from the database instance
     * Phase 8: Enables injection of tracing progress DAO
     */
    @Provides
    fun provideTracingProgressDao(database: HappyKidDatabase): TracingProgressDao {
        return database.tracingProgressDao()
    }

    /**
     * Provides LearningAnalyticsDao from the database instance
     * Phase 10: Enables injection of learning analytics DAO
     */
    @Provides
    fun provideLearningAnalyticsDao(database: HappyKidDatabase): LearningAnalyticsDao {
        return database.learningAnalyticsDao()
    }

    /**
     * Provides StoryDao from the database instance
     * Phase 12: Enables injection of story DAO for content expansion
     */
    @Provides
    fun provideStoryDao(database: HappyKidDatabase): StoryDao {
        return database.storyDao()
    }

    /**
     * Provides VocabularyDao from the database instance
     * Phase 12: Enables injection of vocabulary DAO for content expansion
     */
    @Provides
    fun provideVocabularyDao(database: HappyKidDatabase): VocabularyDao {
        return database.vocabularyDao()
    }
}
