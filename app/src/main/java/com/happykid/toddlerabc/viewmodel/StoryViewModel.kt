package com.happykid.toddlerabc.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happykid.toddlerabc.audio.AudioManager
import com.happykid.toddlerabc.model.Story
import com.happykid.toddlerabc.model.InteractiveElement
import com.happykid.toddlerabc.repository.StoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Story functionality
 * Phase 12: Content Expansion - Interactive story system with educational integration
 *
 * Manages story UI state, reading progress, interactive elements,
 * and audio integration while maintaining educational objectives.
 */
@HiltViewModel
class StoryViewModel @Inject constructor(
    private val storyRepository: StoryRepository,
    private val audioManager: AudioManager
) : ViewModel() {

    // ==================== UI State ====================

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> = _stories.asStateFlow()

    private val _currentStory = MutableStateFlow<Story?>(null)
    val currentStory: StateFlow<Story?> = _currentStory.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ==================== Story Reading State ====================

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _isReading = MutableStateFlow(false)
    val isReading: StateFlow<Boolean> = _isReading.asStateFlow()

    private val _readingProgress = MutableStateFlow(0f)
    val readingProgress: StateFlow<Float> = _readingProgress.asStateFlow()

    private val _highlightedWords = MutableStateFlow<Set<String>>(emptySet())
    val highlightedWords: StateFlow<Set<String>> = _highlightedWords.asStateFlow()

    // ==================== Interactive Elements State ====================

    private val _currentInteractiveElement = MutableStateFlow<InteractiveElement?>(null)
    val currentInteractiveElement: StateFlow<InteractiveElement?> = _currentInteractiveElement.asStateFlow()

    private val _completedInteractions = MutableStateFlow<Set<String>>(emptySet())
    val completedInteractions: StateFlow<Set<String>> = _completedInteractions.asStateFlow()

    private val _interactionScore = MutableStateFlow(0)
    val interactionScore: StateFlow<Int> = _interactionScore.asStateFlow()

    // ==================== Filter and Search State ====================

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow<Int?>(null)
    val selectedDifficulty: StateFlow<Int?> = _selectedDifficulty.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ==================== Audio State ====================

    val isAudioEnabled: StateFlow<Boolean> = audioManager.isAudioEnabled
    val isAudioPlaying: StateFlow<Boolean> = audioManager.isPlaying
    val currentVolume: StateFlow<Float> = audioManager.currentVolume

    init {
        loadStories()
    }

    // ==================== Story Loading Operations ====================

    /**
     * Load all stories from repository
     */
    private fun loadStories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                storyRepository.getAllStoriesFlow().collect { storyList ->
                    _stories.value = storyList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load stories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load stories by category
     */
    fun loadStoriesByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _selectedCategory.value = category
                storyRepository.getStoriesByCategoryFlow(category).collect { storyList ->
                    _stories.value = storyList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load stories for category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load stories by difficulty level
     */
    fun loadStoriesByDifficulty(level: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _selectedDifficulty.value = level
                val storyList = storyRepository.getStoriesByDifficulty(level)
                _stories.value = storyList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load stories for difficulty: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Search stories by query
     */
    fun searchStories(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _searchQuery.value = query
                val storyList = if (query.isEmpty()) {
                    storyRepository.getAllStories()
                } else {
                    storyRepository.searchStories(query)
                }
                _stories.value = storyList
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search stories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Get recommended stories for user
     */
    fun loadRecommendedStories(userAgeRange: String, completedStoryIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val recommendedStories = storyRepository.getRecommendedStories(
                    userAgeRange = userAgeRange,
                    completedStoryIds = completedStoryIds
                )
                _stories.value = recommendedStories
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recommended stories: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ==================== Story Reading Operations ====================

    /**
     * Start reading a story
     */
    fun startReading(storyId: String) {
        viewModelScope.launch {
            try {
                val story = storyRepository.getStoryById(storyId)
                if (story != null) {
                    _currentStory.value = story
                    _currentPage.value = 0
                    _isReading.value = true
                    _readingProgress.value = 0f
                    _completedInteractions.value = emptySet()
                    _interactionScore.value = 0

                    // Start audio narration if available
                    story.audioPath?.let { audioPath ->
                        playStoryAudio(audioPath)
                    }

                    // Initialize first interactive element
                    if (story.interactiveElements.isNotEmpty()) {
                        _currentInteractiveElement.value = story.interactiveElements.first()
                    }
                } else {
                    _errorMessage.value = "Story not found"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to start reading: ${e.message}"
            }
        }
    }

    /**
     * Stop reading current story
     */
    fun stopReading() {
        _currentStory.value = null
        _isReading.value = false
        _currentPage.value = 0
        _readingProgress.value = 0f
        _currentInteractiveElement.value = null
        _completedInteractions.value = emptySet()
        _interactionScore.value = 0
        stopAudio()
    }

    /**
     * Navigate to next page
     */
    fun nextPage() {
        val story = _currentStory.value ?: return
        val totalPages = calculateTotalPages(story)

        if (_currentPage.value < totalPages - 1) {
            _currentPage.value += 1
            updateReadingProgress()
            updateInteractiveElement()
        }
    }

    /**
     * Navigate to previous page
     */
    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            updateReadingProgress()
            updateInteractiveElement()
        }
    }

    /**
     * Update reading progress based on current page
     */
    private fun updateReadingProgress() {
        val story = _currentStory.value ?: return
        val totalPages = calculateTotalPages(story)
        _readingProgress.value = (_currentPage.value + 1).toFloat() / totalPages.toFloat()
    }

    /**
     * Calculate total pages for a story
     */
    private fun calculateTotalPages(story: Story): Int {
        // Simple calculation based on content length and illustrations
        val contentPages = (story.content.length / 200) + 1
        val illustrationPages = story.illustrations.size
        return maxOf(contentPages, illustrationPages, 1)
    }

    // ==================== Interactive Elements Operations ====================

    /**
     * Handle interaction with story element
     */
    fun handleInteraction(elementTarget: String) {
        val currentElement = _currentInteractiveElement.value ?: return

        if (currentElement.target == elementTarget) {
            // Correct interaction
            val newCompletedInteractions = _completedInteractions.value + elementTarget
            _completedInteractions.value = newCompletedInteractions

            // Add points
            _interactionScore.value += currentElement.points

            // Highlight the interacted word/element
            _highlightedWords.value = _highlightedWords.value + elementTarget

            // Play success audio feedback
            playInteractionFeedback(true)

            // Move to next interactive element
            moveToNextInteractiveElement()
        } else {
            // Incorrect interaction - provide gentle feedback
            playInteractionFeedback(false)
        }
    }

    /**
     * Move to next interactive element
     */
    private fun moveToNextInteractiveElement() {
        val story = _currentStory.value ?: return
        val currentElement = _currentInteractiveElement.value ?: return

        val currentIndex = story.interactiveElements.indexOf(currentElement)
        if (currentIndex < story.interactiveElements.size - 1) {
            _currentInteractiveElement.value = story.interactiveElements[currentIndex + 1]
        } else {
            // All interactions completed
            _currentInteractiveElement.value = null
        }
    }

    /**
     * Update interactive element based on current page
     */
    private fun updateInteractiveElement() {
        val story = _currentStory.value ?: return
        val currentPageNum = _currentPage.value

        // Find interactive element for current page (simplified logic)
        val pageElement = story.interactiveElements.getOrNull(currentPageNum)
        _currentInteractiveElement.value = pageElement
    }

    // ==================== Audio Operations ====================

    /**
     * Play story audio narration
     */
    private fun playStoryAudio(audioPath: String) {
        viewModelScope.launch {
            try {
                // For now, use TTS to read the story content
                val story = _currentStory.value
                story?.let {
                    audioManager.speak("Now reading: ${it.title}")
                }
            } catch (e: Exception) {
                // Audio playback failed - continue without audio
            }
        }
    }

    /**
     * Play interaction feedback audio
     */
    private fun playInteractionFeedback(isCorrect: Boolean) {
        viewModelScope.launch {
            try {
                if (isCorrect) {
                    audioManager.playSuccessSound()
                } else {
                    audioManager.speak("Try again!")
                }
            } catch (e: Exception) {
                // Audio feedback failed - continue silently
            }
        }
    }

    /**
     * Stop audio playback
     */
    fun stopAudio() {
        viewModelScope.launch {
            audioManager.stopAudio()
        }
    }

    /**
     * Toggle audio enabled/disabled
     */
    fun toggleAudio() {
        viewModelScope.launch {
            audioManager.toggleAudio()
        }
    }

    // ==================== Utility Operations ====================

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Reset filters
     */
    fun resetFilters() {
        _selectedCategory.value = null
        _selectedDifficulty.value = null
        _searchQuery.value = ""
        loadStories()
    }

    /**
     * Get story completion status
     */
    fun isStoryCompleted(): Boolean {
        val story = _currentStory.value ?: return false
        return _readingProgress.value >= 1.0f &&
               _completedInteractions.value.size >= story.interactiveElements.size
    }
}
