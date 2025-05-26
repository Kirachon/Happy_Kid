package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.audio.AudioManager
import com.happykid.toddlerabc.model.Letter
import com.happykid.toddlerabc.repository.LetterRepository
import com.happykid.toddlerabc.repository.LearningStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Alphabet Learning functionality
 * Phase 4: Enhanced with Media3 audio integration
 *
 * Manages UI state and coordinates with repository for data operations.
 * Maintains all existing functionality from Phase 1-3 while adding
 * audio feedback and pronunciation features.
 */
@HiltViewModel
class AlphabetViewModel @Inject constructor(
    private val repository: LetterRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    // UI State for letters
    private val _letters = MutableStateFlow<List<Letter>>(emptyList())
    val letters: StateFlow<List<Letter>> = _letters.asStateFlow()

    // UI State for learning statistics
    private val _learningStats = MutableStateFlow<LearningStats?>(null)
    val learningStats: StateFlow<LearningStats?> = _learningStats.asStateFlow()

    // UI State for loading
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // UI State for error handling
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Phase 4: Audio state management
    val isAudioEnabled: StateFlow<Boolean> = audioManager.isAudioEnabled
    val isAudioPlaying: StateFlow<Boolean> = audioManager.isPlaying
    val currentVolume: StateFlow<Float> = audioManager.currentVolume

    init {
        // Initialize data loading
        loadLetters()
        loadLearningStats()
    }

    /**
     * Load all letters from repository
     * Maintains reactive updates from Phase 1 Room implementation
     */
    private fun loadLetters() {
        viewModelScope.launch {
            try {
                repository.getAllLettersFlow().collect { letterList ->
                    _letters.value = letterList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load letters: ${e.message}"
            }
        }
    }

    /**
     * Load learning statistics
     */
    private fun loadLearningStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getLearningStats()
                _learningStats.value = stats
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load statistics: ${e.message}"
            }
        }
    }

    /**
     * Handle letter practice click
     * Phase 4: Enhanced with audio feedback and pronunciation
     */
    fun onLetterClicked(character: Char) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Phase 4: Play letter pronunciation
                audioManager.playLetterPronunciation(character)

                // Existing practice tracking logic from Phase 1
                repository.practiceLetterClicked(character)

                // Refresh statistics after practice
                loadLearningStats()

                // Play success sound for milestone achievements
                val currentStats = repository.getLearningStats()
                if (currentStats.totalPractices % 5 == 0) {
                    audioManager.playSuccessSound()
                }

            } catch (e: Exception) {
                _errorMessage.value = "Failed to record practice: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Mark letter as learned/unlearned
     */
    fun markLetterAsLearned(character: Char, isLearned: Boolean) {
        viewModelScope.launch {
            try {
                repository.markLetterAsLearned(character, isLearned)
                loadLearningStats()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update letter status: ${e.message}"
            }
        }
    }

    /**
     * Reset all learning progress
     */
    fun resetAllProgress() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.resetAllProgress()
                loadLearningStats()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reset progress: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Refresh all data
     */
    fun refresh() {
        loadLearningStats()
        // Letters are automatically refreshed via Flow
    }

    // Phase 4: Audio control methods

    /**
     * Play letter phonetic sound
     */
    fun playLetterSound(character: Char) {
        audioManager.playLetterSound(character)
    }

    /**
     * Toggle audio on/off
     */
    fun toggleAudio() {
        audioManager.toggleAudio()
    }

    /**
     * Set audio enabled state
     */
    fun setAudioEnabled(enabled: Boolean) {
        audioManager.setAudioEnabled(enabled)
    }

    /**
     * Set volume level (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        audioManager.setVolume(volume)
    }

    /**
     * Stop all audio playback
     */
    fun stopAudio() {
        audioManager.stopAudio()
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up audio resources when ViewModel is destroyed
        audioManager.release()
    }
}
