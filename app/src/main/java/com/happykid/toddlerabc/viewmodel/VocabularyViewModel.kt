package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.model.VocabularyWord
import com.happykid.toddlerabc.repository.VocabularyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Vocabulary learning activities
 * Phase 12: Content Expansion - Vocabulary learning with spaced repetition
 *
 * Manages vocabulary word presentation, spaced repetition algorithms,
 * progress tracking, and adaptive difficulty adjustment.
 */
@HiltViewModel
class VocabularyViewModel @Inject constructor(
    private val vocabularyRepository: VocabularyRepository
) : ViewModel() {

    // ==================== UI State ====================

    private val _uiState = MutableStateFlow(VocabularyUiState())
    val uiState: StateFlow<VocabularyUiState> = _uiState.asStateFlow()

    private val _currentWord = MutableStateFlow<VocabularyWord?>(null)
    val currentWord: StateFlow<VocabularyWord?> = _currentWord.asStateFlow()

    private val _practiceWords = MutableStateFlow<List<VocabularyWord>>(emptyList())
    val practiceWords: StateFlow<List<VocabularyWord>> = _practiceWords.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    // ==================== Initialization ====================

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Load categories
                val categories = vocabularyRepository.getAllCategories()
                _categories.value = categories

                // Load daily practice set
                val practiceSet = vocabularyRepository.getDailyPracticeSet(
                    userAgeRange = "2-4", // Default age range for toddlers
                    targetCount = 5
                )
                _practiceWords.value = practiceSet

                // Set first word if available
                if (practiceSet.isNotEmpty()) {
                    _currentWord.value = practiceSet.first()
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWordIndex = 0,
                    totalWords = practiceSet.size,
                    sessionStarted = false
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load vocabulary data: ${e.message}"
                )
            }
        }
    }

    // ==================== Session Management ====================

    /**
     * Start a new vocabulary practice session
     */
    fun startPracticeSession() {
        viewModelScope.launch {
            try {
                val practiceSet = vocabularyRepository.getDailyPracticeSet(
                    userAgeRange = "2-4",
                    targetCount = 5
                )
                
                _practiceWords.value = practiceSet
                _currentWord.value = practiceSet.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    sessionStarted = true,
                    currentWordIndex = 0,
                    totalWords = practiceSet.size,
                    sessionScore = 0,
                    correctAnswers = 0,
                    sessionStartTime = System.currentTimeMillis(),
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to start practice session: ${e.message}"
                )
            }
        }
    }

    /**
     * Process user's answer for current word
     */
    fun submitAnswer(isCorrect: Boolean, responseTimeMs: Long = 0) {
        val currentState = _uiState.value
        val word = _currentWord.value ?: return

        viewModelScope.launch {
            try {
                // Update word statistics
                vocabularyRepository.updateWordStatistics(
                    wordId = word.id,
                    wasCorrect = isCorrect,
                    responseTimeMs = responseTimeMs
                )

                // Update session state
                val newScore = if (isCorrect) currentState.sessionScore + 10 else currentState.sessionScore
                val newCorrectAnswers = if (isCorrect) currentState.correctAnswers + 1 else currentState.correctAnswers

                _uiState.value = currentState.copy(
                    sessionScore = newScore,
                    correctAnswers = newCorrectAnswers,
                    lastAnswerCorrect = isCorrect,
                    showFeedback = true
                )

                // Auto-advance after feedback delay
                kotlinx.coroutines.delay(2000) // Show feedback for 2 seconds
                nextWord()

            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    error = "Failed to process answer: ${e.message}"
                )
            }
        }
    }

    /**
     * Move to next word in practice session
     */
    fun nextWord() {
        val currentState = _uiState.value
        val currentIndex = currentState.currentWordIndex
        val practiceList = _practiceWords.value

        if (currentIndex + 1 < practiceList.size) {
            // Move to next word
            val nextIndex = currentIndex + 1
            _currentWord.value = practiceList[nextIndex]
            _uiState.value = currentState.copy(
                currentWordIndex = nextIndex,
                showFeedback = false,
                lastAnswerCorrect = null
            )
        } else {
            // Session complete
            completeSession()
        }
    }

    /**
     * Complete the current practice session
     */
    private fun completeSession() {
        val currentState = _uiState.value
        val sessionDuration = System.currentTimeMillis() - currentState.sessionStartTime
        val accuracy = if (currentState.totalWords > 0) {
            (currentState.correctAnswers.toFloat() / currentState.totalWords.toFloat()) * 100
        } else 0f

        _uiState.value = currentState.copy(
            sessionCompleted = true,
            sessionDurationMs = sessionDuration,
            sessionAccuracy = accuracy,
            showFeedback = false
        )
    }

    /**
     * Reset session to start over
     */
    fun resetSession() {
        _uiState.value = VocabularyUiState()
        _currentWord.value = null
        loadInitialData()
    }

    // ==================== Word Management ====================

    /**
     * Load words by category
     */
    fun loadWordsByCategory(category: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val words = vocabularyRepository.getVocabularyWordsByCategory(category)
                _practiceWords.value = words
                _currentWord.value = words.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWordIndex = 0,
                    totalWords = words.size,
                    selectedCategory = category
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load words for category: ${e.message}"
                )
            }
        }
    }

    /**
     * Load words for specific letter practice
     */
    fun loadWordsForLetter(letter: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val words = vocabularyRepository.getVocabularyWordsForLetterPractice(listOf(letter))
                _practiceWords.value = words
                _currentWord.value = words.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWordIndex = 0,
                    totalWords = words.size,
                    targetLetter = letter
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load words for letter: ${e.message}"
                )
            }
        }
    }

    /**
     * Get words due for review (spaced repetition)
     */
    fun loadReviewWords() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val reviewWords = vocabularyRepository.getWordsForReview()
                _practiceWords.value = reviewWords
                _currentWord.value = reviewWords.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    currentWordIndex = 0,
                    totalWords = reviewWords.size,
                    isReviewMode = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load review words: ${e.message}"
                )
            }
        }
    }

    // ==================== Error Handling ====================

    /**
     * Clear current error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    /**
     * Hide feedback message
     */
    fun hideFeedback() {
        _uiState.value = _uiState.value.copy(showFeedback = false, lastAnswerCorrect = null)
    }
}

/**
 * UI State for Vocabulary screen
 */
data class VocabularyUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val sessionStarted: Boolean = false,
    val sessionCompleted: Boolean = false,
    val currentWordIndex: Int = 0,
    val totalWords: Int = 0,
    val sessionScore: Int = 0,
    val correctAnswers: Int = 0,
    val sessionStartTime: Long = 0L,
    val sessionDurationMs: Long = 0L,
    val sessionAccuracy: Float = 0f,
    val showFeedback: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val selectedCategory: String? = null,
    val targetLetter: String? = null,
    val isReviewMode: Boolean = false
)
