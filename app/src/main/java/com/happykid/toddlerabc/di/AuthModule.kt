package com.happykid.toddlerabc.di

import android.content.Context
import com.happykid.toddlerabc.auth.ParentalAuthManager
import com.happykid.toddlerabc.auth.SessionManager
import com.happykid.toddlerabc.data.HappyKidDatabase
import com.happykid.toddlerabc.data.ParentalAuthDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for authentication components
 * Phase 11: Parental Controls and Dashboard Enhancement
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    /**
     * Provides ParentalAuthDao from the database
     */
    @Provides
    @Singleton
    fun provideParentalAuthDao(database: HappyKidDatabase): ParentalAuthDao {
        return database.parentalAuthDao()
    }

    /**
     * Provides ParentalAuthManager singleton
     */
    @Provides
    @Singleton
    fun provideParentalAuthManager(
        @ApplicationContext context: Context,
        parentalAuthDao: ParentalAuthDao
    ): ParentalAuthManager {
        return ParentalAuthManager(context, parentalAuthDao)
    }

    /**
     * Provides SessionManager singleton
     */
    @Provides
    @Singleton
    fun provideSessionManager(
        parentalAuthDao: ParentalAuthDao
    ): SessionManager {
        return SessionManager(parentalAuthDao)
    }
}
