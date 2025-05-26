package com.happykid.toddlerabc.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.happykid.toddlerabc.util.WindowsDeviceUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Speech Recognition Manager for Happy_Kid App
 * Phase 7: Offline-capable speech recognition with toddler-friendly vocal effort detection
 *
 * This manager provides Android SpeechRecognizer API integration specifically optimized
 * for toddler speech patterns with loose thresholds and immediate positive feedback.
 * Designed for Windows emulator compatibility and offline operation.
 */
@Singleton
class SpeechRecognitionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "SpeechRecognitionManager"
        private const val SPEECH_TIMEOUT_MS = 5000L
        private const val TODDLER_CONFIDENCE_THRESHOLD = 0.3f
        private const val MAX_RECOGNITION_ATTEMPTS = 3
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionAttempts = 0
    private val isWindowsEmulator = WindowsDeviceUtils.isWindowsEmulator()

    // Speech recognition state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechResult = MutableStateFlow<SpeechResult?>(null)
    val speechResult: StateFlow<SpeechResult?> = _speechResult.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        initializeSpeechRecognizer()
    }

    /**
     * Initialize speech recognizer with toddler-friendly settings
     * Phase 7: Windows emulator optimized speech recognition
     */
    private fun initializeSpeechRecognizer() {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
                speechRecognizer?.setRecognitionListener(createRecognitionListener())
                _isAvailable.value = true
                
                if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                    Log.d(TAG, "Speech recognizer initialized for Windows emulator")
                }
            } else {
                Log.w(TAG, "Speech recognition not available on this device")
                _isAvailable.value = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize speech recognizer", e)
            _isAvailable.value = false
        }
    }

    /**
     * Start listening for speech with toddler-optimized settings
     * Phase 7: Vocal effort detection with loose thresholds
     */
    fun startListening(targetLetter: Char? = null) {
        if (!_isAvailable.value) {
            _errorMessage.value = "Speech recognition not available"
            return
        }

        if (_isListening.value) {
            Log.w(TAG, "Already listening for speech")
            return
        }

        try {
            val intent = createSpeechRecognitionIntent(targetLetter)
            speechRecognizer?.startListening(intent)
            _isListening.value = true
            _errorMessage.value = null
            recognitionAttempts++

            if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                Log.d(TAG, "Started listening for speech (attempt $recognitionAttempts)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognition", e)
            _errorMessage.value = "Failed to start listening"
            _isListening.value = false
        }
    }

    /**
     * Stop listening for speech
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            
            if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                Log.d(TAG, "Stopped listening for speech")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop speech recognition", e)
        }
    }

    /**
     * Create speech recognition intent with toddler-friendly settings
     * Phase 7: Optimized for vocal effort detection
     */
    private fun createSpeechRecognitionIntent(targetLetter: Char?): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            // Use offline recognition for privacy and reliability
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            
            // Set language model for better toddler speech recognition
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            
            // Set language to US English
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            
            // Enable partial results for immediate feedback
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            
            // Set maximum results for better accuracy
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            
            // Set speech timeout for toddler attention span
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SPEECH_TIMEOUT_MS)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, SPEECH_TIMEOUT_MS / 2)
            
            // Add prompt for target letter if specified
            targetLetter?.let { letter ->
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say the letter ${letter.uppercaseChar()}")
            } ?: run {
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")
            }
            
            // Windows emulator specific optimizations
            if (isWindowsEmulator) {
                putExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES, true)
                putExtra("android.speech.extra.DICTATION_MODE", true)
            }
        }
    }

    /**
     * Create recognition listener with toddler-friendly feedback
     * Phase 7: Immediate positive feedback for any vocal effort
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech input")
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech input detected - providing immediate positive feedback")
                // Immediate positive feedback for any vocal effort
                _speechResult.value = SpeechResult.VocalEffortDetected
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Monitor audio levels for visual feedback
                if (rmsdB > -30f) { // Any reasonable audio level
                    _speechResult.value = SpeechResult.VocalEffortDetected
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received - vocal effort confirmed
                _speechResult.value = SpeechResult.VocalEffortDetected
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "End of speech detected")
                _isListening.value = false
            }

            override fun onError(error: Int) {
                handleSpeechError(error)
            }

            override fun onResults(results: Bundle?) {
                handleSpeechResults(results)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                // Provide immediate feedback for partial results
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { results ->
                    if (results.isNotEmpty()) {
                        _speechResult.value = SpeechResult.VocalEffortDetected
                        Log.d(TAG, "Partial speech result: ${results.first()}")
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d(TAG, "Speech recognition event: $eventType")
            }
        }
    }

    /**
     * Handle speech recognition results with toddler-friendly interpretation
     * Phase 7: Loose thresholds for toddler speech variability
     */
    private fun handleSpeechResults(results: Bundle?) {
        _isListening.value = false
        recognitionAttempts = 0

        results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { recognizedText ->
            val confidenceScores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
            
            if (recognizedText.isNotEmpty()) {
                val bestResult = recognizedText.first()
                val confidence = confidenceScores?.firstOrNull() ?: 1.0f
                
                // Very loose threshold for toddler speech - any recognition is success
                if (confidence >= TODDLER_CONFIDENCE_THRESHOLD || bestResult.isNotBlank()) {
                    _speechResult.value = SpeechResult.Success(bestResult, confidence)
                    Log.d(TAG, "Speech recognition success: '$bestResult' (confidence: $confidence)")
                } else {
                    // Still provide positive feedback for vocal effort
                    _speechResult.value = SpeechResult.VocalEffortDetected
                    Log.d(TAG, "Low confidence speech, but vocal effort detected")
                }
            } else {
                // No text but vocal effort was detected earlier
                _speechResult.value = SpeechResult.VocalEffortDetected
                Log.d(TAG, "No text recognized, but vocal effort was detected")
            }
        } ?: run {
            // Fallback to vocal effort detection
            _speechResult.value = SpeechResult.VocalEffortDetected
            Log.d(TAG, "No results bundle, but vocal effort was detected")
        }
    }

    /**
     * Handle speech recognition errors with graceful degradation
     */
    private fun handleSpeechError(error: Int) {
        _isListening.value = false
        
        val errorMessage = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
            SpeechRecognizer.ERROR_NETWORK -> "Network error (using offline mode)"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout (using offline mode)"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected - try again!"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected - try again!"
            else -> "Speech recognition error"
        }

        // For toddler-friendly experience, treat most errors as "try again" opportunities
        when (error) {
            SpeechRecognizer.ERROR_NO_MATCH,
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                if (recognitionAttempts < MAX_RECOGNITION_ATTEMPTS) {
                    _speechResult.value = SpeechResult.TryAgain
                } else {
                    _speechResult.value = SpeechResult.VocalEffortDetected // Still positive!
                }
            }
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                _errorMessage.value = "Microphone permission needed for speaking activities"
            }
            else -> {
                _speechResult.value = SpeechResult.VocalEffortDetected // Always positive for toddlers
            }
        }

        Log.w(TAG, "Speech recognition error: $errorMessage (code: $error)")
    }

    /**
     * Clear current speech result
     */
    fun clearResult() {
        _speechResult.value = null
        _errorMessage.value = null
        recognitionAttempts = 0
    }

    /**
     * Release speech recognition resources
     */
    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
            _isAvailable.value = false
            Log.d(TAG, "Speech recognition resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing speech recognition resources", e)
        }
    }
}

/**
 * Speech recognition result types for toddler-friendly feedback
 * Phase 7: Positive reinforcement for all vocal efforts
 */
sealed class SpeechResult {
    object VocalEffortDetected : SpeechResult()
    object TryAgain : SpeechResult()
    data class Success(val text: String, val confidence: Float) : SpeechResult()
}
