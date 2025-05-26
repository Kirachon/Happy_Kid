package com.happykid.toddlerabc.auth

import com.happykid.toddlerabc.data.ParentalAuthDao
import com.happykid.toddlerabc.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Session Manager for tracking app usage and enforcing time limits
 * Phase 11: Parental Controls and Dashboard Enhancement
 */
@Singleton
class SessionManager @Inject constructor(
    private val parentalAuthDao: ParentalAuthDao
) {
    companion object {
        private const val TAG = "SessionManager"
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    }

    // Current session state
    private val _currentSession = MutableStateFlow<AppSession?>(null)
    val currentSession: StateFlow<AppSession?> = _currentSession.asStateFlow()

    private val _timeLimitStatus = MutableStateFlow<TimeLimitStatus?>(null)
    val timeLimitStatus: StateFlow<TimeLimitStatus?> = _timeLimitStatus.asStateFlow()

    private val _isSessionActive = MutableStateFlow(false)
    val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

    /**
     * Start a new app session
     */
    suspend fun startSession(): Boolean {
        // Check if session can be started based on time limits
        val canStart = canStartNewSession()
        if (!canStart) {
            return false
        }

        // End any existing session first
        endCurrentSession()

        // Create new session
        val currentTime = System.currentTimeMillis()
        val today = dateFormat.format(Date(currentTime))
        
        val newSession = AppSession(
            startTime = currentTime,
            endTime = null,
            durationMinutes = 0,
            activitiesAccessed = "",
            date = today,
            isCompleted = false
        )

        val sessionId = parentalAuthDao.insertAppSession(newSession)
        val sessionWithId = newSession.copy(id = sessionId)
        
        _currentSession.value = sessionWithId
        _isSessionActive.value = true
        
        // Update time limit status
        updateTimeLimitStatus()
        
        return true
    }

    /**
     * End the current session
     */
    suspend fun endCurrentSession() {
        val session = _currentSession.value
        if (session != null && !session.isCompleted) {
            val endTime = System.currentTimeMillis()
            val durationMinutes = ((endTime - session.startTime) / 60000).toInt()
            
            parentalAuthDao.completeSession(session.id, endTime, durationMinutes)
            
            _currentSession.value = null
            _isSessionActive.value = false
            
            // Update time limit status
            updateTimeLimitStatus()
        }
    }

    /**
     * Add activity to current session
     */
    suspend fun addActivityToSession(activityName: String) {
        val session = _currentSession.value
        if (session != null && _isSessionActive.value) {
            // Update activities accessed (simple comma-separated list for now)
            val currentActivities = session.activitiesAccessed
            val updatedActivities = if (currentActivities.isEmpty()) {
                activityName
            } else if (!currentActivities.contains(activityName)) {
                "$currentActivities,$activityName"
            } else {
                currentActivities // Activity already recorded
            }
            
            _currentSession.value = session.copy(activitiesAccessed = updatedActivities)
        }
    }

    /**
     * Check if a new session can be started based on time limits
     */
    suspend fun canStartNewSession(): Boolean {
        val controls = parentalAuthDao.getParentalControls() ?: return true
        val today = dateFormat.format(Date())
        
        // Check daily time limit
        val dailyMinutesUsed = parentalAuthDao.getTotalMinutesForDate(today)
        val dailyLimit = if (isWeekend()) controls.weekendTimeLimitMinutes else controls.dailyTimeLimitMinutes
        
        if (dailyMinutesUsed >= dailyLimit) {
            return false
        }
        
        // Check allowed time window
        val currentTime = timeFormat.format(Date())
        if (!isWithinAllowedHours(currentTime, controls.allowedTimeStart, controls.allowedTimeEnd)) {
            return false
        }
        
        return true
    }

    /**
     * Get current time limit status
     */
    suspend fun getTimeLimitStatus(): TimeLimitStatus {
        val controls = parentalAuthDao.getParentalControls() ?: return getDefaultTimeLimitStatus()
        val today = dateFormat.format(Date())
        
        val dailyMinutesUsed = parentalAuthDao.getTotalMinutesForDate(today)
        val dailyLimit = if (isWeekend()) controls.weekendTimeLimitMinutes else controls.dailyTimeLimitMinutes
        
        val sessionMinutesUsed = getCurrentSessionMinutes()
        val sessionLimit = controls.sessionTimeLimitMinutes
        
        val currentTime = timeFormat.format(Date())
        val isWithinAllowedHours = isWithinAllowedHours(currentTime, controls.allowedTimeStart, controls.allowedTimeEnd)
        
        val timeUntilNextAllowedSession = if (!isWithinAllowedHours) {
            calculateTimeUntilNextAllowedSession(controls.allowedTimeStart)
        } else {
            0L
        }
        
        val canStartNewSession = dailyMinutesUsed < dailyLimit && 
                                isWithinAllowedHours && 
                                sessionMinutesUsed < sessionLimit
        
        return TimeLimitStatus(
            dailyMinutesUsed = dailyMinutesUsed,
            dailyMinutesAllowed = dailyLimit,
            sessionMinutesUsed = sessionMinutesUsed,
            sessionMinutesAllowed = sessionLimit,
            isWithinAllowedHours = isWithinAllowedHours,
            timeUntilNextAllowedSession = timeUntilNextAllowedSession,
            canStartNewSession = canStartNewSession
        )
    }

    /**
     * Get parental controls settings as Flow
     */
    fun getParentalControlsFlow(): Flow<ParentalControls?> {
        return parentalAuthDao.getParentalControlsFlow()
    }

    /**
     * Initialize session manager
     */
    suspend fun initialize() {
        // Check for any incomplete sessions and complete them
        val currentSession = parentalAuthDao.getCurrentSession()
        if (currentSession != null) {
            // Complete the previous session
            val endTime = System.currentTimeMillis()
            val durationMinutes = ((endTime - currentSession.startTime) / 60000).toInt()
            parentalAuthDao.completeSession(currentSession.id, endTime, durationMinutes)
        }
        
        // Initialize default parental controls if not exists
        parentalAuthDao.initializeDefaultControls()
        
        // Update time limit status
        updateTimeLimitStatus()
    }

    /**
     * Update time limit status
     */
    private suspend fun updateTimeLimitStatus() {
        _timeLimitStatus.value = getTimeLimitStatus()
    }

    /**
     * Get current session duration in minutes
     */
    private fun getCurrentSessionMinutes(): Int {
        val session = _currentSession.value
        return if (session != null && _isSessionActive.value) {
            ((System.currentTimeMillis() - session.startTime) / 60000).toInt()
        } else {
            0
        }
    }

    /**
     * Check if current time is within allowed hours
     */
    private fun isWithinAllowedHours(currentTime: String, startTime: String, endTime: String): Boolean {
        return try {
            val current = timeFormat.parse(currentTime)?.time ?: return false
            val start = timeFormat.parse(startTime)?.time ?: return false
            val end = timeFormat.parse(endTime)?.time ?: return false
            
            current in start..end
        } catch (e: Exception) {
            true // Default to allowing if parsing fails
        }
    }

    /**
     * Check if today is weekend
     */
    private fun isWeekend(): Boolean {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }

    /**
     * Calculate time until next allowed session
     */
    private fun calculateTimeUntilNextAllowedSession(allowedStartTime: String): Long {
        return try {
            val calendar = Calendar.getInstance()
            val startTime = timeFormat.parse(allowedStartTime) ?: return 0L
            
            val startCalendar = Calendar.getInstance().apply {
                time = startTime
                set(Calendar.YEAR, calendar.get(Calendar.YEAR))
                set(Calendar.MONTH, calendar.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH))
            }
            
            // If start time is tomorrow
            if (startCalendar.timeInMillis <= calendar.timeInMillis) {
                startCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            startCalendar.timeInMillis - calendar.timeInMillis
        } catch (e: Exception) {
            0L
        }
    }

    /**
     * Get default time limit status
     */
    private fun getDefaultTimeLimitStatus(): TimeLimitStatus {
        return TimeLimitStatus(
            dailyMinutesUsed = 0,
            dailyMinutesAllowed = 60,
            sessionMinutesUsed = 0,
            sessionMinutesAllowed = 30,
            isWithinAllowedHours = true,
            timeUntilNextAllowedSession = 0L,
            canStartNewSession = true
        )
    }

    /**
     * Clean up old sessions (call periodically)
     */
    suspend fun cleanupOldSessions() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -30) // Keep 30 days of history
        val cutoffDate = dateFormat.format(calendar.time)
        parentalAuthDao.deleteOldSessions(cutoffDate)
    }
}
