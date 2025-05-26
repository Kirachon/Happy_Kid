package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO for parental authentication and controls
 * Phase 11: Secure parental authentication system
 */
@Dao
interface ParentalAuthDao {

    // ==================== Parental Authentication ====================

    /**
     * Get parental authentication settings
     */
    @Query("SELECT * FROM parental_auth WHERE id = 1")
    suspend fun getParentalAuth(): ParentalAuth?

    /**
     * Get parental authentication settings as Flow
     */
    @Query("SELECT * FROM parental_auth WHERE id = 1")
    fun getParentalAuthFlow(): Flow<ParentalAuth?>

    /**
     * Insert or update parental authentication settings
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParentalAuth(auth: ParentalAuth)

    /**
     * Update authentication method
     */
    @Query("UPDATE parental_auth SET authMethod = :method, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateAuthMethod(method: AuthMethod, timestamp: Long = System.currentTimeMillis())

    /**
     * Update PIN hash
     */
    @Query("UPDATE parental_auth SET pinHash = :pinHash, updatedAt = :timestamp WHERE id = 1")
    suspend fun updatePinHash(pinHash: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update biometric setting
     */
    @Query("UPDATE parental_auth SET biometricEnabled = :enabled, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateBiometricEnabled(enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    /**
     * Update last authentication timestamp
     */
    @Query("UPDATE parental_auth SET lastAuthTimestamp = :timestamp WHERE id = 1")
    suspend fun updateLastAuthTimestamp(timestamp: Long)

    /**
     * Update failed attempts
     */
    @Query("UPDATE parental_auth SET failedAttempts = :attempts, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateFailedAttempts(attempts: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Set lockout until timestamp
     */
    @Query("UPDATE parental_auth SET lockoutUntil = :lockoutUntil, updatedAt = :timestamp WHERE id = 1")
    suspend fun setLockoutUntil(lockoutUntil: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * Reset failed attempts
     */
    @Query("UPDATE parental_auth SET failedAttempts = 0, lockoutUntil = 0, updatedAt = :timestamp WHERE id = 1")
    suspend fun resetFailedAttempts(timestamp: Long = System.currentTimeMillis())

    /**
     * Enable authentication
     */
    @Query("UPDATE parental_auth SET isAuthEnabled = :enabled, updatedAt = :timestamp WHERE id = 1")
    suspend fun setAuthEnabled(enabled: Boolean, timestamp: Long = System.currentTimeMillis())

    // ==================== Parental Controls ====================

    /**
     * Get parental control settings
     */
    @Query("SELECT * FROM parental_controls WHERE id = 1")
    suspend fun getParentalControls(): ParentalControls?

    /**
     * Get parental control settings as Flow
     */
    @Query("SELECT * FROM parental_controls WHERE id = 1")
    fun getParentalControlsFlow(): Flow<ParentalControls?>

    /**
     * Insert or update parental control settings
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParentalControls(controls: ParentalControls)

    /**
     * Update session time limit
     */
    @Query("UPDATE parental_controls SET sessionTimeLimitMinutes = :minutes, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateSessionTimeLimit(minutes: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Update daily time limit
     */
    @Query("UPDATE parental_controls SET dailyTimeLimitMinutes = :minutes, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateDailyTimeLimit(minutes: Int, timestamp: Long = System.currentTimeMillis())

    /**
     * Update allowed time window
     */
    @Query("UPDATE parental_controls SET allowedTimeStart = :start, allowedTimeEnd = :end, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateAllowedTimeWindow(start: String, end: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update content filter level
     */
    @Query("UPDATE parental_controls SET contentFilterLevel = :level, updatedAt = :timestamp WHERE id = 1")
    suspend fun updateContentFilterLevel(level: ContentFilterLevel, timestamp: Long = System.currentTimeMillis())

    /**
     * Update activity permissions
     */
    @Query("""
        UPDATE parental_controls SET 
        allowSpeechRecognition = :speech,
        allowTracingActivities = :tracing,
        allowPhonicsActivities = :phonics,
        allowAlphabetActivities = :alphabet,
        updatedAt = :timestamp 
        WHERE id = 1
    """)
    suspend fun updateActivityPermissions(
        speech: Boolean,
        tracing: Boolean,
        phonics: Boolean,
        alphabet: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )

    // ==================== App Sessions ====================

    /**
     * Insert new app session
     */
    @Insert
    suspend fun insertAppSession(session: AppSession): Long

    /**
     * Update session end time and duration
     */
    @Query("UPDATE app_sessions SET endTime = :endTime, durationMinutes = :duration, isCompleted = 1 WHERE id = :sessionId")
    suspend fun completeSession(sessionId: Long, endTime: Long, duration: Int)

    /**
     * Get current active session
     */
    @Query("SELECT * FROM app_sessions WHERE isCompleted = 0 ORDER BY startTime DESC LIMIT 1")
    suspend fun getCurrentSession(): AppSession?

    /**
     * Get sessions for a specific date
     */
    @Query("SELECT * FROM app_sessions WHERE date = :date ORDER BY startTime DESC")
    suspend fun getSessionsForDate(date: String): List<AppSession>

    /**
     * Get total minutes used today
     */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM app_sessions WHERE date = :date AND isCompleted = 1")
    suspend fun getTotalMinutesForDate(date: String): Int

    /**
     * Get sessions in date range
     */
    @Query("SELECT * FROM app_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY startTime DESC")
    suspend fun getSessionsInRange(startDate: String, endDate: String): List<AppSession>

    /**
     * Delete old sessions (cleanup)
     */
    @Query("DELETE FROM app_sessions WHERE date < :cutoffDate")
    suspend fun deleteOldSessions(cutoffDate: String)

    // ==================== Initialization ====================

    /**
     * Initialize default parental auth if not exists
     */
    @Query("INSERT OR IGNORE INTO parental_auth (id, isAuthEnabled, authMethod, sessionTimeoutMinutes) VALUES (1, 0, 'PIN', 30)")
    suspend fun initializeDefaultAuth()

    /**
     * Initialize default parental controls if not exists
     */
    @Query("""
        INSERT OR IGNORE INTO parental_controls 
        (id, sessionTimeLimitMinutes, dailyTimeLimitMinutes, allowedTimeStart, allowedTimeEnd, 
         weekendTimeLimitMinutes, contentFilterLevel, allowSpeechRecognition, allowTracingActivities, 
         allowPhonicsActivities, allowAlphabetActivities, requireParentalApprovalForNewFeatures, 
         enableUsageReports, enableAchievementNotifications) 
        VALUES (1, 30, 60, '08:00', '20:00', 90, 'AGE_APPROPRIATE', 1, 1, 1, 1, 1, 1, 1)
    """)
    suspend fun initializeDefaultControls()
}
