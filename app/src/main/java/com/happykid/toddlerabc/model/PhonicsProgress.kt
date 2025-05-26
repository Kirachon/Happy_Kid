package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Phonics Progress data class for the Happy Kid app
 * Phase 9: Phonics and Early Reading Engine progress tracking
 *
 * Tracks detailed phonics learning metrics including word recognition,
 * blending accuracy, reading comprehension, and milestone achievements.
 */
@Entity(tableName = "phonics_progress")
data class PhonicsProgress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Word identification
    val wordId: Long, // Foreign key to PhonicsWord
    val word: String, // Denormalized for easier queries
    val category: WordCategory,
    
    // Session information
    val sessionTimestamp: Long = System.currentTimeMillis(),
    val sessionDurationMs: Long = 0,
    val activityType: PhonicsActivityType,
    
    // Recognition metrics
    val recognitionAccuracy: Float = 0f, // 0-100%
    val recognitionTimeMs: Long = 0, // Time to recognize word
    val attemptsRequired: Int = 1, // Number of attempts needed
    
    // Blending metrics
    val blendingAccuracy: Float = 0f, // Accuracy in sound blending
    val blendingStepsCompleted: Int = 0, // How many blending steps completed
    val blendingTimeMs: Long = 0, // Time to complete blending
    
    // Pronunciation metrics
    val pronunciationAccuracy: Float = 0f, // Speech recognition accuracy
    val pronunciationAttempts: Int = 0, // Number of pronunciation attempts
    val speechRecognitionSuccess: Boolean = false,
    
    // Reading comprehension
    val comprehensionAccuracy: Float = 0f, // Understanding of word meaning
    val contextualUsage: Boolean = false, // Used word in context correctly
    
    // Learning indicators
    val isFirstAttempt: Boolean = false,
    val isImprovement: Boolean = false, // Better than previous attempt
    val milestoneAchieved: String? = null, // "first_recognition", "mastery", etc.
    
    // Difficulty and assistance
    val difficultyLevel: Int = 1, // 1-5 scale
    val hintsUsed: Int = 0, // Number of hints provided
    val assistanceRequired: Boolean = false,
    
    // Engagement metrics
    val engagementScore: Float = 0f, // 0-100% based on interaction
    val sessionCompleted: Boolean = true,
    val voluntaryRepetition: Boolean = false, // Child chose to repeat
    
    // Performance indicators
    val isSuccessful: Boolean = false, // Overall session success
    val confidenceLevel: Float = 0f, // Child's confidence (0-100%)
    val frustrationLevel: Float = 0f, // Detected frustration (0-100%)
    
    // Technical metrics (Windows emulator optimization)
    val frameRate: Float = 60f,
    val touchLatencyMs: Float = 0f,
    val audioLatencyMs: Float = 0f,
    val renderingPerformance: Float = 100f
) {
    
    /**
     * Calculate overall learning score
     */
    val overallScore: Float
        get() = (recognitionAccuracy + blendingAccuracy + pronunciationAccuracy + comprehensionAccuracy) / 4f
    
    /**
     * Check if this represents mastery of the word
     */
    val indicatesMastery: Boolean
        get() = overallScore >= 80f && attemptsRequired <= 2 && !assistanceRequired
    
    /**
     * Calculate learning efficiency (accuracy per time)
     */
    val learningEfficiency: Float
        get() = if (sessionDurationMs > 0) {
            (overallScore / (sessionDurationMs / 1000f)) * 10f // Score per 10 seconds
        } else 0f
    
    companion object {
        
        /**
         * Create a successful phonics session
         */
        fun createSuccessfulSession(
            wordId: Long,
            word: String,
            category: WordCategory,
            activityType: PhonicsActivityType,
            recognitionAccuracy: Float = 85f,
            sessionDurationMs: Long = 30000L
        ): PhonicsProgress {
            return PhonicsProgress(
                wordId = wordId,
                word = word,
                category = category,
                activityType = activityType,
                recognitionAccuracy = recognitionAccuracy,
                blendingAccuracy = recognitionAccuracy - 5f,
                pronunciationAccuracy = recognitionAccuracy - 10f,
                comprehensionAccuracy = recognitionAccuracy,
                sessionDurationMs = sessionDurationMs,
                isSuccessful = true,
                confidenceLevel = 80f,
                engagementScore = 90f,
                milestoneAchieved = if (recognitionAccuracy >= 80f) "mastery" else null
            )
        }
        
        /**
         * Create a learning session with assistance
         */
        fun createAssistedSession(
            wordId: Long,
            word: String,
            category: WordCategory,
            activityType: PhonicsActivityType,
            hintsUsed: Int = 2
        ): PhonicsProgress {
            return PhonicsProgress(
                wordId = wordId,
                word = word,
                category = category,
                activityType = activityType,
                recognitionAccuracy = 60f,
                blendingAccuracy = 55f,
                pronunciationAccuracy = 50f,
                comprehensionAccuracy = 65f,
                hintsUsed = hintsUsed,
                assistanceRequired = true,
                attemptsRequired = 3,
                isSuccessful = true,
                confidenceLevel = 60f,
                engagementScore = 75f
            )
        }
    }
}

/**
 * Types of phonics activities for progress tracking
 */
enum class PhonicsActivityType {
    WORD_RECOGNITION,    // Recognizing whole words
    SOUND_BLENDING,      // Blending individual sounds
    SIGHT_WORD_PRACTICE, // High-frequency word recognition
    PRONUNCIATION,       // Speaking and pronunciation practice
    WORD_BUILDING,       // Constructing words from letters
    RHYMING_GAMES,       // Identifying rhyming patterns
    READING_COMPREHENSION, // Understanding word meanings
    WORD_FAMILY_SORTING  // Grouping words by patterns
}

/**
 * Reading session summary for comprehensive progress tracking
 */
data class ReadingSession(
    val sessionId: String = java.util.UUID.randomUUID().toString(),
    val startTimestamp: Long = System.currentTimeMillis(),
    val endTimestamp: Long = 0L,
    val totalWordsPresented: Int = 0,
    val totalWordsRecognized: Int = 0,
    val averageAccuracy: Float = 0f,
    val activitiesCompleted: List<PhonicsActivityType> = emptyList(),
    val milestonesAchieved: List<String> = emptyList(),
    val overallEngagement: Float = 0f,
    val sessionCompleted: Boolean = false
) {
    
    /**
     * Calculate session duration in minutes
     */
    val durationMinutes: Float
        get() = if (endTimestamp > startTimestamp) {
            (endTimestamp - startTimestamp) / 60000f
        } else 0f
    
    /**
     * Calculate words per minute reading rate
     */
    val wordsPerMinute: Float
        get() = if (durationMinutes > 0) {
            totalWordsRecognized / durationMinutes
        } else 0f
}

/**
 * Overall phonics statistics across all words and sessions
 */
data class PhonicsStats(
    val totalPhonicsWords: Int,
    val totalReadingSessions: Int,
    val totalPracticeTimeMs: Long,
    val averageAccuracyAllWords: Float,
    val wordsStarted: Int,
    val wordsMastered: Int,
    val sightWordsMastered: Int,
    val cvcWordsMastered: Int,
    val currentStreak: Int, // Consecutive successful sessions
    val bestStreak: Int,
    val favoriteWordFamilies: List<String>, // Most practiced word families
    val challengingWords: List<String>, // Lowest accuracy words
    val recentImprovement: Float, // Improvement over last 10 sessions
    val readingLevel: ReadingLevel
)

/**
 * Reading level assessment for toddlers
 */
enum class ReadingLevel {
    PRE_READER,      // Beginning letter recognition
    EMERGING_READER, // CVC words and simple sight words
    EARLY_READER,    // Word families and basic sentences
    DEVELOPING_READER // Complex words and simple stories
}
