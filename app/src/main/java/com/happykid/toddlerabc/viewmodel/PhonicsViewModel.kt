package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.audio.AudioManager
import com.happykid.toddlerabc.model.PhonicsWord
import com.happykid.toddlerabc.model.PhonicsProgress
import com.happykid.toddlerabc.model.PhonicsActivityType
import com.happykid.toddlerabc.model.WordCategory
import com.happykid.toddlerabc.repository.PhonicsRepository
import com.happykid.toddlerabc.repository.PhonicsProgressRepository
import com.happykid.toddlerabc.speech.SpeechRecognitionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Phonics Learning functionality
 * Phase 9: Manages phonics activities, word recognition, and reading progress
 *
 * Coordinates phonics word management, progress tracking, audio feedback,
 * and speech recognition integration while maintaining all existing functionality.
 */
@HiltViewModel
class PhonicsViewModel @Inject constructor(
    private val phonicsRepository: PhonicsRepository,
    private val progressRepository: PhonicsProgressRepository,
    private val audioManager: AudioManager,
    private val speechRecognitionManager: SpeechRecognitionManager
) : ViewModel() {

    // UI State
    private val _phonicsWords = MutableStateFlow<List<PhonicsWord>>(emptyList())
    val phonicsWords: StateFlow<List<PhonicsWord>> = _phonicsWords.asStateFlow()

    private val _currentWord = MutableStateFlow<PhonicsWord?>(null)
    val currentWord: StateFlow<PhonicsWord?> = _currentWord.asStateFlow()

    private val _currentActivity = MutableStateFlow(PhonicsActivityType.WORD_RECOGNITION)
    val currentActivity: StateFlow<PhonicsActivityType> = _currentActivity.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _phonicsStats = MutableStateFlow<com.happykid.toddlerabc.model.PhonicsStats?>(null)
    val phonicsStats: StateFlow<com.happykid.toddlerabc.model.PhonicsStats?> = _phonicsStats.asStateFlow()

    // Activity state
    private val _sessionStartTime = MutableStateFlow(0L)
    private val _currentSessionProgress = MutableStateFlow<PhonicsProgress?>(null)
    val currentSessionProgress: StateFlow<PhonicsProgress?> = _currentSessionProgress.asStateFlow()

    private val _showHints = MutableStateFlow(true)
    val showHints: StateFlow<Boolean> = _showHints.asStateFlow()

    private val _difficultyLevel = MutableStateFlow(1)
    val difficultyLevel: StateFlow<Int> = _difficultyLevel.asStateFlow()

    // Audio state from AudioManager
    val isAudioEnabled: StateFlow<Boolean> = audioManager.isAudioEnabled
    val isAudioPlaying: StateFlow<Boolean> = audioManager.isPlaying

    // Speech recognition state
    val speechRecognitionResult = speechRecognitionManager.speechResult
    val isSpeechRecognitionActive = speechRecognitionManager.isListening

    init {
        loadPhonicsWords()
        loadPhonicsStats()
        initializePhonicsData()
    }

    /**
     * Load all phonics words from repository
     */
    private fun loadPhonicsWords() {
        viewModelScope.launch {
            try {
                phonicsRepository.getAllPhonicsWordsFlow().collect { wordList ->
                    _phonicsWords.value = wordList

                    // Set first word as current if none selected
                    if (_currentWord.value == null && wordList.isNotEmpty()) {
                        _currentWord.value = wordList.first()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load phonics words: ${e.message}"
            }
        }
    }

    /**
     * Load phonics statistics
     */
    private fun loadPhonicsStats() {
        viewModelScope.launch {
            try {
                val stats = progressRepository.getPhonicsStats()
                _phonicsStats.value = stats
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load phonics stats: ${e.message}"
            }
        }
    }

    /**
     * Initialize phonics data if empty
     */
    private fun initializePhonicsData() {
        viewModelScope.launch {
            try {
                phonicsRepository.initializeIfEmpty()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize phonics data: ${e.message}"
            }
        }
    }

    /**
     * Select a specific word for practice
     */
    fun selectWord(word: PhonicsWord) {
        _currentWord.value = word
        startNewSession()

        // Play word pronunciation
        audioManager.pronounceWord(word.word)
    }

    /**
     * Set current activity type
     */
    fun setActivity(activityType: PhonicsActivityType) {
        _currentActivity.value = activityType
        startNewSession()

        // Play activity instructions
        audioManager.playPhonicsInstructions(activityType.name)
    }

    /**
     * Start a new learning session
     */
    private fun startNewSession() {
        _sessionStartTime.value = System.currentTimeMillis()
        _currentSessionProgress.value = null
    }

    /**
     * Handle letter selection in word building activity
     */
    fun onLetterSelected(letter: Char) {
        viewModelScope.launch {
            try {
                // Play letter sound
                audioManager.playLetterPronunciation(letter)

                // Record interaction if we have a current word
                _currentWord.value?.let { word ->
                    recordPhonicsInteraction(
                        word = word,
                        activityType = _currentActivity.value,
                        isSuccessful = word.word.contains(letter, ignoreCase = true)
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process letter selection: ${e.message}"
            }
        }
    }

    /**
     * Handle word completion
     */
    fun onWordCompleted(builtWord: String) {
        viewModelScope.launch {
            try {
                _currentWord.value?.let { targetWord ->
                    val isCorrect = builtWord.equals(targetWord.word, ignoreCase = true)
                    val sessionDuration = System.currentTimeMillis() - _sessionStartTime.value

                    // Record successful completion
                    recordPhonicsSession(
                        word = targetWord,
                        activityType = _currentActivity.value,
                        recognitionAccuracy = if (isCorrect) 100f else 0f,
                        sessionDurationMs = sessionDuration,
                        isSuccessful = isCorrect
                    )

                    // Provide audio feedback
                    if (isCorrect) {
                        audioManager.playSuccessSound()
                        audioManager.pronounceWord(targetWord.word)
                    } else {
                        audioManager.providePronunciationFeedback(false, targetWord.word)
                    }

                    // Update word practice statistics
                    phonicsRepository.practiceWord(targetWord.id, isCorrect)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process word completion: ${e.message}"
            }
        }
    }

    /**
     * Handle blending step completion
     */
    fun onBlendingStepCompleted(step: Int) {
        viewModelScope.launch {
            try {
                _currentWord.value?.let { word ->
                    if (step < word.blendingSteps.size) {
                        // Play the blending step
                        audioManager.demonstrateBlending(word.word, word.blendingSteps.take(step + 1))

                        // Record progress
                        recordPhonicsInteraction(
                            word = word,
                            activityType = PhonicsActivityType.SOUND_BLENDING,
                            isSuccessful = true
                        )
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to process blending step: ${e.message}"
            }
        }
    }

    /**
     * Handle pronunciation attempt
     */
    fun onPronunciationAttempt() {
        viewModelScope.launch {
            try {
                _currentWord.value?.let { word ->
                    // Start speech recognition for the target word
                    speechRecognitionManager.startListening(word.word.first())

                    // Record pronunciation attempt
                    recordPhonicsInteraction(
                        word = word,
                        activityType = PhonicsActivityType.PRONUNCIATION,
                        isSuccessful = true // Will be updated based on speech recognition result
                    )
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start pronunciation practice: ${e.message}"
            }
        }
    }

    /**
     * Get words by category
     */
    fun getWordsByCategory(category: WordCategory) {
        viewModelScope.launch {
            try {
                phonicsRepository.getWordsByCategory(category).collect { words ->
                    _phonicsWords.value = words
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load words by category: ${e.message}"
            }
        }
    }

    /**
     * Get words by difficulty level
     */
    fun getWordsByDifficulty(level: Int) {
        _difficultyLevel.value = level
        viewModelScope.launch {
            try {
                phonicsRepository.getWordsByDifficulty(level).collect { words ->
                    _phonicsWords.value = words
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load words by difficulty: ${e.message}"
            }
        }
    }

    /**
     * Toggle hints visibility
     */
    fun toggleHints() {
        _showHints.value = !_showHints.value
    }

    /**
     * Get next word for practice
     */
    fun getNextWord() {
        val currentWords = _phonicsWords.value
        val currentWord = _currentWord.value

        if (currentWords.isNotEmpty()) {
            val currentIndex = currentWords.indexOf(currentWord)
            val nextIndex = (currentIndex + 1) % currentWords.size
            selectWord(currentWords[nextIndex])
        }
    }

    /**
     * Get random word for practice
     */
    fun getRandomWord() {
        viewModelScope.launch {
            try {
                val randomWords = phonicsRepository.getRandomWordsForPractice(
                    ageInYears = 3, // Default age
                    count = 1
                )
                if (randomWords.isNotEmpty()) {
                    selectWord(randomWords.first())
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get random word: ${e.message}"
            }
        }
    }

    /**
     * Record phonics interaction
     */
    private suspend fun recordPhonicsInteraction(
        word: PhonicsWord,
        activityType: PhonicsActivityType,
        isSuccessful: Boolean
    ) {
        try {
            val sessionDuration = System.currentTimeMillis() - _sessionStartTime.value

            if (isSuccessful) {
                progressRepository.recordSuccessfulSession(
                    wordId = word.id,
                    word = word.word,
                    category = word.category,
                    activityType = activityType,
                    sessionDurationMs = sessionDuration
                )
            } else {
                progressRepository.recordAssistedSession(
                    wordId = word.id,
                    word = word.word,
                    category = word.category,
                    activityType = activityType
                )
            }
        } catch (e: Exception) {
            _errorMessage.value = "Failed to record interaction: ${e.message}"
        }
    }

    /**
     * Record complete phonics session
     */
    private suspend fun recordPhonicsSession(
        word: PhonicsWord,
        activityType: PhonicsActivityType,
        recognitionAccuracy: Float,
        sessionDurationMs: Long,
        isSuccessful: Boolean
    ) {
        try {
            val session = PhonicsProgress(
                wordId = word.id,
                word = word.word,
                category = word.category,
                activityType = activityType,
                recognitionAccuracy = recognitionAccuracy,
                sessionDurationMs = sessionDurationMs,
                isSuccessful = isSuccessful,
                isFirstAttempt = progressRepository.isFirstSessionForWord(word.id),
                engagementScore = 85f, // Default engagement score
                confidenceLevel = if (isSuccessful) 80f else 60f
            )

            progressRepository.recordPhonicsSession(session)
            _currentSessionProgress.value = session

            // Refresh stats
            loadPhonicsStats()
        } catch (e: Exception) {
            _errorMessage.value = "Failed to record session: ${e.message}"
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
