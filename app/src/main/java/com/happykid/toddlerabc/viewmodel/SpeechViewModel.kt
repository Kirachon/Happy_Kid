package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.audio.AudioManager
import com.happykid.toddlerabc.permission.PermissionManager
import com.happykid.toddlerabc.permission.PermissionState
import com.happykid.toddlerabc.speech.SpeechRecognitionManager
import com.happykid.toddlerabc.speech.SpeechResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Speech Recognition ViewModel for Happy_Kid App
 * Phase 7: Manages speech recognition state and integration with audio system
 *
 * This ViewModel coordinates speech recognition, permissions, and audio feedback
 * to provide a seamless toddler-friendly speech learning experience.
 */
@HiltViewModel
class SpeechViewModel @Inject constructor(
    private val speechRecognitionManager: SpeechRecognitionManager,
    private val permissionManager: PermissionManager,
    private val audioManager: AudioManager
) : ViewModel() {

    companion object {
        private const val TAG = "SpeechViewModel"
        private const val FEEDBACK_DISPLAY_DURATION = 3000L
        private const val SUCCESS_CELEBRATION_DURATION = 2000L
    }

    // Speech recognition state
    val isListening = speechRecognitionManager.isListening
    val speechResult = speechRecognitionManager.speechResult
    val isSpeechAvailable = speechRecognitionManager.isAvailable
    val speechError = speechRecognitionManager.errorMessage

    // Permission state
    val microphonePermissionState = permissionManager.microphonePermissionState
    val parentalConsentGiven = permissionManager.parentalConsentGiven
    val shouldShowPermissionRationale = permissionManager.shouldShowPermissionRationale

    // Audio state
    val isAudioEnabled = audioManager.isAudioEnabled
    val isAudioPlaying = audioManager.isPlaying

    // UI state
    private val _currentTargetLetter = MutableStateFlow<Char?>(null)
    val currentTargetLetter: StateFlow<Char?> = _currentTargetLetter.asStateFlow()

    private val _showPermissionDialog = MutableStateFlow(false)
    val showPermissionDialog: StateFlow<Boolean> = _showPermissionDialog.asStateFlow()

    private val _showParentalConsentDialog = MutableStateFlow(false)
    val showParentalConsentDialog: StateFlow<Boolean> = _showParentalConsentDialog.asStateFlow()

    private val _speechFeedbackMessage = MutableStateFlow<String?>(null)
    val speechFeedbackMessage: StateFlow<String?> = _speechFeedbackMessage.asStateFlow()

    // Computed state for speech recognition availability
    val canUseSpeechRecognition: StateFlow<Boolean> = combine(
        isSpeechAvailable,
        microphonePermissionState,
        parentalConsentGiven
    ) { available, permissionState, consent ->
        available && permissionState == PermissionState.Granted && consent
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        observeSpeechResults()
    }

    /**
     * Start speech recognition for a specific letter
     * Phase 7: Letter-specific speech practice
     */
    fun startSpeechRecognition(targetLetter: Char? = null) {
        if (!canUseSpeechRecognition.value) {
            handlePermissionRequired()
            return
        }

        viewModelScope.launch {
            try {
                _currentTargetLetter.value = targetLetter

                // Provide audio prompt if audio is enabled
                if (isAudioEnabled.value) {
                    targetLetter?.let { letter ->
                        audioManager.playSpeechPrompt(letter)
                        delay(1000) // Wait for prompt to finish
                    } ?: run {
                        audioManager.speak("Say something!")
                        delay(800)
                    }
                }

                // Clear previous results
                speechRecognitionManager.clearResult()

                // Start listening
                speechRecognitionManager.startListening(targetLetter)

            } catch (e: Exception) {
                _speechFeedbackMessage.value = "Failed to start speech recognition"
            }
        }
    }

    /**
     * Stop speech recognition
     */
    fun stopSpeechRecognition() {
        speechRecognitionManager.stopListening()
        _currentTargetLetter.value = null
    }

    /**
     * Handle permission request result
     */
    fun onPermissionResult(permission: String, isGranted: Boolean) {
        permissionManager.onPermissionResult(permission, isGranted)

        if (isGranted && permission == PermissionManager.MICROPHONE_PERMISSION) {
            if (!parentalConsentGiven.value) {
                _showParentalConsentDialog.value = true
            }
        }
    }

    /**
     * Handle parental consent response
     */
    fun onParentalConsentResponse(granted: Boolean) {
        permissionManager.setParentalConsent(granted)
        _showParentalConsentDialog.value = false

        if (granted) {
            _speechFeedbackMessage.value = "Speech features are now available! 🎤"
            clearFeedbackAfterDelay()
        }
    }

    /**
     * Show permission request dialog
     */
    fun showPermissionDialog() {
        _showPermissionDialog.value = true
    }

    /**
     * Dismiss permission dialog
     */
    fun dismissPermissionDialog() {
        _showPermissionDialog.value = false
    }

    /**
     * Dismiss parental consent dialog
     */
    fun dismissParentalConsentDialog() {
        _showParentalConsentDialog.value = false
    }

    /**
     * Handle permission required scenario
     */
    private fun handlePermissionRequired() {
        when (microphonePermissionState.value) {
            PermissionState.NotRequested, PermissionState.Denied -> {
                if (permissionManager.canRequestPermission()) {
                    _showPermissionDialog.value = true
                } else {
                    _speechFeedbackMessage.value = "Please enable microphone permission in Settings"
                    clearFeedbackAfterDelay()
                }
            }
            PermissionState.PermanentlyDenied -> {
                _speechFeedbackMessage.value = "Please enable microphone permission in Settings"
                clearFeedbackAfterDelay()
            }
            PermissionState.Granted -> {
                if (!parentalConsentGiven.value) {
                    _showParentalConsentDialog.value = true
                }
            }
        }
    }

    /**
     * Observe speech recognition results and provide feedback
     * Phase 7: Immediate positive feedback for toddler vocal efforts
     */
    private fun observeSpeechResults() {
        viewModelScope.launch {
            speechResult.collect { result ->
                when (result) {
                    is SpeechResult.Success -> {
                        handleSpeechSuccess(result)
                    }
                    is SpeechResult.VocalEffortDetected -> {
                        handleVocalEffort()
                    }
                    is SpeechResult.TryAgain -> {
                        handleTryAgain()
                    }
                    null -> {
                        // No result yet
                    }
                }
            }
        }
    }

    /**
     * Handle successful speech recognition
     */
    private fun handleSpeechSuccess(result: SpeechResult.Success) {
        viewModelScope.launch {
            _speechFeedbackMessage.value = "Great job! You said: '${result.text}'"

            // Play success audio feedback
            if (isAudioEnabled.value) {
                audioManager.playSuccessSound()
                delay(500)
                audioManager.speak("Excellent speaking!")
            }

            clearFeedbackAfterDelay(SUCCESS_CELEBRATION_DURATION)
        }
    }

    /**
     * Handle vocal effort detection (positive reinforcement)
     */
    private fun handleVocalEffort() {
        viewModelScope.launch {
            _speechFeedbackMessage.value = "Good try! I heard you speak! 👏"

            // Play encouraging audio feedback
            if (isAudioEnabled.value) {
                audioManager.playEncouragement()
            }

            clearFeedbackAfterDelay()
        }
    }

    /**
     * Handle try again scenario
     */
    private fun handleTryAgain() {
        viewModelScope.launch {
            _speechFeedbackMessage.value = "Try speaking a little louder! 😊"

            if (isAudioEnabled.value) {
                audioManager.speak("Try speaking a little louder!")
            }

            clearFeedbackAfterDelay()
        }
    }

    /**
     * Clear speech feedback message after delay
     */
    private fun clearFeedbackAfterDelay(duration: Long = FEEDBACK_DISPLAY_DURATION) {
        viewModelScope.launch {
            delay(duration)
            _speechFeedbackMessage.value = null
        }
    }

    /**
     * Clear current speech result
     */
    fun clearSpeechResult() {
        speechRecognitionManager.clearResult()
        _speechFeedbackMessage.value = null
    }

    /**
     * Get permission status message for UI
     */
    fun getPermissionStatusMessage(): String {
        return permissionManager.getPermissionStatusMessage()
    }

    /**
     * Get parental consent message for UI
     */
    fun getParentalConsentMessage(): String {
        return permissionManager.getParentalConsentMessage()
    }

    /**
     * Update should show permission rationale
     */
    fun updateShouldShowRationale(shouldShow: Boolean) {
        permissionManager.updateShouldShowRationale(shouldShow)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognitionManager.release()
    }
}


