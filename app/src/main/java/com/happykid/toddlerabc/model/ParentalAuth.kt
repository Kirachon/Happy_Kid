package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Parental authentication models for Phase 11
 * Secure authentication system for parental controls and dashboard access
 */

/**
 * Parental authentication settings stored in Room database
 */
@Entity(tableName = "parental_auth")
data class ParentalAuth(
    @PrimaryKey
    val id: Int = 1, // Single row for app-wide auth settings
    val isAuthEnabled: Boolean = false,
    val authMethod: AuthMethod = AuthMethod.PIN,
    val pinHash: String? = null, // Hashed PIN for security
    val biometricEnabled: Boolean = false,
    val sessionTimeoutMinutes: Int = 30,
    val lastAuthTimestamp: Long = 0L,
    val failedAttempts: Int = 0,
    val lockoutUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Authentication methods supported
 */
enum class AuthMethod {
    PIN,
    BIOMETRIC,
    PIN_AND_BIOMETRIC
}

/**
 * Authentication state for UI
 */
sealed class AuthState {
    object NotConfigured : AuthState()
    object Authenticated : AuthState()
    object NotAuthenticated : AuthState()
    object LockedOut : AuthState()
    data class Failed(val attemptsRemaining: Int) : AuthState()
    object BiometricNotAvailable : AuthState()
    object BiometricNotEnrolled : AuthState()
}

/**
 * Authentication result
 */
sealed class AuthResult {
    object Success : AuthResult()
    object InvalidCredentials : AuthResult()
    object TooManyAttempts : AuthResult()
    object BiometricError : AuthResult()
    object BiometricNotAvailable : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Parental control settings
 */
@Entity(tableName = "parental_controls")
data class ParentalControls(
    @PrimaryKey
    val id: Int = 1, // Single row for app-wide controls
    val sessionTimeLimitMinutes: Int = 30,
    val dailyTimeLimitMinutes: Int = 60,
    val allowedTimeStart: String = "08:00", // HH:mm format
    val allowedTimeEnd: String = "20:00", // HH:mm format
    val weekendTimeLimitMinutes: Int = 90,
    val contentFilterLevel: ContentFilterLevel = ContentFilterLevel.AGE_APPROPRIATE,
    val allowSpeechRecognition: Boolean = true,
    val allowTracingActivities: Boolean = true,
    val allowPhonicsActivities: Boolean = true,
    val allowAlphabetActivities: Boolean = true,
    val requireParentalApprovalForNewFeatures: Boolean = true,
    val enableUsageReports: Boolean = true,
    val enableAchievementNotifications: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Content filter levels
 */
enum class ContentFilterLevel {
    UNRESTRICTED,
    AGE_APPROPRIATE,
    STRICT
}

/**
 * Session tracking for time limits
 */
@Entity(tableName = "app_sessions")
data class AppSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val durationMinutes: Int = 0,
    val activitiesAccessed: String = "", // JSON string of activities
    val date: String = "", // YYYY-MM-DD format for daily tracking
    val isCompleted: Boolean = false
)

/**
 * Time limit status
 */
data class TimeLimitStatus(
    val dailyMinutesUsed: Int,
    val dailyMinutesAllowed: Int,
    val sessionMinutesUsed: Int,
    val sessionMinutesAllowed: Int,
    val isWithinAllowedHours: Boolean,
    val timeUntilNextAllowedSession: Long, // milliseconds
    val canStartNewSession: Boolean
)

/**
 * Authentication configuration for setup
 */
data class AuthConfig(
    val method: AuthMethod,
    val pin: String? = null,
    val enableBiometric: Boolean = false,
    val sessionTimeoutMinutes: Int = 30
)
