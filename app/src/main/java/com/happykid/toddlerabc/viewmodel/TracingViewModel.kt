package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.audio.AudioManager
import com.happykid.toddlerabc.haptic.HapticFeedbackManager
import com.happykid.toddlerabc.model.Letter
import com.happykid.toddlerabc.model.TracingProgress
import com.happykid.toddlerabc.repository.LetterRepository
import com.happykid.toddlerabc.repository.TracingProgressRepository
import com.happykid.toddlerabc.tracing.LetterFormationGuide
import com.happykid.toddlerabc.tracing.LetterFormationGuideFactory
import com.happykid.toddlerabc.tracing.TracingTouchDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Tracing ViewModel for Happy_Kid App
 * Phase 8: Manages tracing state and coordinates backend components
 *
 * This ViewModel integrates TracingCanvas, TouchDetector, HapticFeedback,
 * and database tracking to provide a complete tracing experience.
 */
@HiltViewModel
class TracingViewModel @Inject constructor(
    private val letterRepository: LetterRepository,
    private val tracingProgressRepository: TracingProgressRepository,
    private val touchDetector: TracingTouchDetector,
    private val hapticManager: HapticFeedbackManager,
    private val audioManager: AudioManager
) : ViewModel() {

    companion object {
        private const val TAG = "TracingViewModel"
        private const val MIN_ACCURACY_FOR_SUCCESS = 75f
        private const val MIN_ACCURACY_FOR_PROGRESS = 60f
    }

    // UI State
    private val _letters = MutableStateFlow<List<Letter>>(emptyList())
    val letters: StateFlow<List<Letter>> = _letters.asStateFlow()

    private val _currentLetter = MutableStateFlow<Letter?>(null)
    val currentLetter: StateFlow<Letter?> = _currentLetter.asStateFlow()

    private val _currentLetterGuide = MutableStateFlow<LetterFormationGuide?>(null)
    val currentLetterGuide: StateFlow<LetterFormationGuide?> = _currentLetterGuide.asStateFlow()

    private val _tracingProgress = MutableStateFlow<List<TracingProgress>>(emptyList())
    val tracingProgress: StateFlow<List<TracingProgress>> = _tracingProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showGuides = MutableStateFlow(true)
    val showGuides: StateFlow<Boolean> = _showGuides.asStateFlow()

    private val _difficultyLevel = MutableStateFlow(1)
    val difficultyLevel: StateFlow<Int> = _difficultyLevel.asStateFlow()

    private val _sessionStartTime = MutableStateFlow(0L)
    val sessionStartTime: StateFlow<Long> = _sessionStartTime.asStateFlow()

    private val _lastTracingAccuracy = MutableStateFlow(0f)
    val lastTracingAccuracy: StateFlow<Float> = _lastTracingAccuracy.asStateFlow()

    // Audio state from AudioManager
    val isAudioEnabled: StateFlow<Boolean> = audioManager.isAudioEnabled
    val isAudioPlaying: StateFlow<Boolean> = audioManager.isPlaying

    // Touch detector state
    val isTracking = touchDetector.isTracking
    val touchPath = touchDetector.touchPath
    val tracingMetrics = touchDetector.tracingMetrics

    // Expose backend components for TracingScreen
    fun getTouchDetector(): TracingTouchDetector = touchDetector
    fun getHapticManager(): HapticFeedbackManager = hapticManager

    init {
        loadLetters()
        loadTracingProgress()
    }

    /**
     * Load all letters from repository
     */
    private fun loadLetters() {
        viewModelScope.launch {
            try {
                letterRepository.getAllLettersFlow().collect { letterList ->
                    _letters.value = letterList

                    // Set first letter as current if none selected
                    if (_currentLetter.value == null && letterList.isNotEmpty()) {
                        selectLetter(letterList.first())
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load letters: ${e.message}"
            }
        }
    }

    /**
     * Load tracing progress from repository
     */
    private fun loadTracingProgress() {
        viewModelScope.launch {
            try {
                tracingProgressRepository.getAllTracingProgressFlow().collect { progressList ->
                    _tracingProgress.value = progressList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load tracing progress: ${e.message}"
            }
        }
    }

    /**
     * Select a letter for tracing practice
     */
    fun selectLetter(letter: Letter) {
        _currentLetter.value = letter
        _currentLetterGuide.value = LetterFormationGuideFactory.getGuideForLetter(letter.character)
        _sessionStartTime.value = System.currentTimeMillis()

        // Clear previous tracing data
        touchDetector.clearTracing()

        // Play letter pronunciation
        audioManager.playLetterPronunciation(letter.character)

        // Provide haptic feedback for letter selection
        hapticManager.provideGuidanceFeedback()
    }

    /**
     * Handle tracing completion with accuracy feedback
     */
    fun onTracingComplete(accuracy: Float) {
        viewModelScope.launch {
            try {
                _lastTracingAccuracy.value = accuracy
                val currentLetter = _currentLetter.value ?: return@launch
                val sessionDuration = System.currentTimeMillis() - _sessionStartTime.value

                // Record tracing session
                val tracingSession = TracingProgress(
                    character = currentLetter.character,
                    sessionTimestamp = System.currentTimeMillis(),
                    sessionDurationMs = sessionDuration,
                    accuracyPercentage = accuracy,
                    completionPercentage = if (accuracy >= MIN_ACCURACY_FOR_PROGRESS) 100f else accuracy,
                    strokeAccuracy = accuracy,
                    totalStrokes = 1, // Simplified for now
                    completedStrokes = if (accuracy >= MIN_ACCURACY_FOR_SUCCESS) 1 else 0,
                    correctStrokeOrder = accuracy >= MIN_ACCURACY_FOR_SUCCESS,
                    difficultyLevel = _difficultyLevel.value,
                    guidanceUsed = _showGuides.value,
                    isCompleted = accuracy >= MIN_ACCURACY_FOR_PROGRESS,
                    isSuccessful = accuracy >= MIN_ACCURACY_FOR_SUCCESS,
                    milestoneAchieved = TracingProgress.generateMilestone(
                        currentLetter.character,
                        accuracy,
                        false, // We'll check this later
                        _difficultyLevel.value
                    )
                )

                tracingProgressRepository.recordTracingSession(tracingSession)

                // Provide audio and haptic feedback based on accuracy
                when {
                    accuracy >= MIN_ACCURACY_FOR_SUCCESS -> {
                        audioManager.playSuccessSound()
                        hapticManager.provideSuccessFeedback()

                        // Update letter practice count
                        letterRepository.practiceLetterClicked(currentLetter.character)
                    }
                    accuracy >= MIN_ACCURACY_FOR_PROGRESS -> {
                        audioManager.playEncouragement()
                        hapticManager.provideEncouragementFeedback()
                    }
                    else -> {
                        audioManager.providePronunciationFeedback(false, currentLetter.character.toString())
                        hapticManager.provideErrorFeedback()
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to record tracing session: ${e.message}"
            }
        }
    }

    /**
     * Clear current tracing
     */
    fun clearTracing() {
        touchDetector.clearTracing()
        _lastTracingAccuracy.value = 0f
        hapticManager.provideGuidanceFeedback()
    }

    /**
     * Toggle guide visibility
     */
    fun toggleGuides() {
        _showGuides.value = !_showGuides.value
        hapticManager.provideGuidanceFeedback()
    }

    /**
     * Set difficulty level
     */
    fun setDifficultyLevel(level: Int) {
        _difficultyLevel.value = level.coerceIn(1, 3)
        hapticManager.provideGuidanceFeedback()
    }

    /**
     * Get next letter for practice
     */
    fun getNextLetter() {
        val currentLetters = _letters.value
        val currentLetter = _currentLetter.value

        if (currentLetters.isNotEmpty()) {
            val currentIndex = currentLetters.indexOf(currentLetter)
            val nextIndex = (currentIndex + 1) % currentLetters.size
            selectLetter(currentLetters[nextIndex])
        }
    }

    /**
     * Get previous letter for practice
     */
    fun getPreviousLetter() {
        val currentLetters = _letters.value
        val currentLetter = _currentLetter.value

        if (currentLetters.isNotEmpty()) {
            val currentIndex = currentLetters.indexOf(currentLetter)
            val previousIndex = if (currentIndex > 0) currentIndex - 1 else currentLetters.size - 1
            selectLetter(currentLetters[previousIndex])
        }
    }

    /**
     * Get tracing statistics for current letter
     */
    fun getLetterTracingStats(letter: Char): TracingProgress? {
        return _tracingProgress.value
            .filter { it.character == letter }
            .maxByOrNull { it.sessionTimestamp }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Toggle audio
     */
    fun toggleAudio() {
        audioManager.toggleAudio()
    }
}
