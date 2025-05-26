package com.happykid.toddlerabc.di

import com.happykid.toddlerabc.data.HappyKidDatabase
import com.happykid.toddlerabc.data.PhonicsDao
import com.happykid.toddlerabc.data.PhonicsProgressDao
import com.happykid.toddlerabc.repository.PhonicsRepository
import com.happykid.toddlerabc.repository.PhonicsProgressRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for Phonics dependencies
 * Phase 9: Provides phonics-related dependencies for dependency injection
 *
 * Manages phonics word repository, progress tracking repository, and database DAOs
 * with proper singleton scoping for optimal performance and resource management.
 */
@Module
@InstallIn(SingletonComponent::class)
object PhonicsModule {

    /**
     * Provides PhonicsDao from the database
     */
    @Provides
    @Singleton
    fun providePhonicsDao(database: HappyKidDatabase): PhonicsDao {
        return database.phonicsDao()
    }

    /**
     * Provides PhonicsProgressDao from the database
     */
    @Provides
    @Singleton
    fun providePhonicsProgressDao(database: HappyKidDatabase): PhonicsProgressDao {
        return database.phonicsProgressDao()
    }

    /**
     * Provides PhonicsRepository with PhonicsDao dependency
     */
    @Provides
    @Singleton
    fun providePhonicsRepository(phonicsDao: PhonicsDao): PhonicsRepository {
        return PhonicsRepository(phonicsDao)
    }

    /**
     * Provides PhonicsProgressRepository with PhonicsProgressDao dependency
     */
    @Provides
    @Singleton
    fun providePhonicsProgressRepository(phonicsProgressDao: PhonicsProgressDao): PhonicsProgressRepository {
        return PhonicsProgressRepository(phonicsProgressDao)
    }
}
