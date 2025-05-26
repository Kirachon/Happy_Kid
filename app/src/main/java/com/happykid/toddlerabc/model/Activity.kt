package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.happykid.toddlerabc.data.ContentTypeConverters

/**
 * Activity entity for diversified learning activities
 * Phase 12: Content Expansion - New learning activity types beyond current modules
 *
 * Represents various learning activities including matching games, sorting activities,
 * pattern recognition exercises with difficulty progression and achievement integration.
 */
@Entity(tableName = "activities")
@TypeConverters(ContentTypeConverters::class)
data class Activity(
    @PrimaryKey
    val id: String,
    
    /**
     * Activity name for display
     */
    val name: String,
    
    /**
     * Activity description
     */
    val description: String,
    
    /**
     * Activity type (matching, sorting, pattern, memory, etc.)
     */
    val type: String,
    
    /**
     * Activity category (alphabet, phonics, vocabulary, math, etc.)
     */
    val category: String,
    
    /**
     * Difficulty level (1-5) for adaptive progression
     */
    val difficultyLevel: Int,
    
    /**
     * Age range recommendation (e.g., "2-3", "3-4")
     */
    val ageRange: String,
    
    /**
     * Estimated completion time in minutes
     */
    val estimatedTime: Int,
    
    /**
     * Educational objectives for this activity
     */
    val educationalObjectives: List<String>,
    
    /**
     * Learning skills targeted by this activity
     */
    val targetSkills: List<String>,
    
    /**
     * Activity configuration and parameters
     */
    val configuration: ActivityConfiguration,
    
    /**
     * Prerequisites for unlocking this activity
     */
    val prerequisites: List<String>,
    
    /**
     * Unlock criteria based on achievements or progress
     */
    val unlockCriteria: String?,
    
    /**
     * Points awarded for completion
     */
    val completionPoints: Int,
    
    /**
     * Achievement ID unlocked upon completion
     */
    val achievementId: String?,
    
    /**
     * Whether activity supports adaptive difficulty
     */
    val supportsAdaptiveDifficulty: Boolean,
    
    /**
     * Minimum accuracy required for progression
     */
    val minimumAccuracy: Float,
    
    /**
     * Maximum attempts allowed per session
     */
    val maxAttempts: Int,
    
    /**
     * Activity creation timestamp
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Last update timestamp
     */
    val updatedAt: Long = System.currentTimeMillis(),
    
    /**
     * Whether activity is active/published
     */
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Get default activities for initial app setup
         */
        fun getDefaultActivities(): List<Activity> {
            return listOf(
                // Matching Games
                Activity(
                    id = "activity_letter_matching",
                    name = "Letter Matching",
                    description = "Match uppercase and lowercase letters",
                    type = "matching",
                    category = "alphabet",
                    difficultyLevel = 1,
                    ageRange = "2-3",
                    estimatedTime = 5,
                    educationalObjectives = listOf("Letter recognition", "Case awareness", "Visual discrimination"),
                    targetSkills = listOf("visual_processing", "pattern_recognition", "fine_motor"),
                    configuration = ActivityConfiguration(
                        itemCount = 6,
                        timeLimit = 300,
                        showHints = true,
                        allowRetries = true,
                        parameters = mapOf(
                            "letters" to listOf("A", "B", "C", "D", "E", "F"),
                            "matchType" to "uppercase_lowercase"
                        )
                    ),
                    prerequisites = emptyList(),
                    unlockCriteria = null,
                    completionPoints = 50,
                    achievementId = "letter_matcher",
                    supportsAdaptiveDifficulty = true,
                    minimumAccuracy = 0.7f,
                    maxAttempts = 3
                ),
                
                Activity(
                    id = "activity_animal_matching",
                    name = "Animal Matching",
                    description = "Match animals with their sounds",
                    type = "matching",
                    category = "vocabulary",
                    difficultyLevel = 2,
                    ageRange = "2-4",
                    estimatedTime = 7,
                    educationalObjectives = listOf("Vocabulary expansion", "Sound association", "Animal recognition"),
                    targetSkills = listOf("auditory_processing", "vocabulary", "categorization"),
                    configuration = ActivityConfiguration(
                        itemCount = 8,
                        timeLimit = 420,
                        showHints = true,
                        allowRetries = true,
                        parameters = mapOf(
                            "animals" to listOf("cat", "dog", "cow", "duck", "pig", "sheep", "horse", "chicken"),
                            "matchType" to "animal_sound"
                        )
                    ),
                    prerequisites = listOf("complete_basic_vocabulary"),
                    unlockCriteria = "vocabulary_milestone_1",
                    completionPoints = 75,
                    achievementId = "animal_expert",
                    supportsAdaptiveDifficulty = true,
                    minimumAccuracy = 0.6f,
                    maxAttempts = 3
                ),
                
                // Sorting Activities
                Activity(
                    id = "activity_color_sorting",
                    name = "Color Sorting",
                    description = "Sort objects by their colors",
                    type = "sorting",
                    category = "vocabulary",
                    difficultyLevel = 1,
                    ageRange = "1-3",
                    estimatedTime = 6,
                    educationalObjectives = listOf("Color recognition", "Categorization", "Logical thinking"),
                    targetSkills = listOf("categorization", "color_recognition", "logical_reasoning"),
                    configuration = ActivityConfiguration(
                        itemCount = 12,
                        timeLimit = 360,
                        showHints = true,
                        allowRetries = true,
                        parameters = mapOf(
                            "colors" to listOf("red", "blue", "yellow", "green"),
                            "objects" to listOf("apple", "ball", "flower", "car", "house", "star")
                        )
                    ),
                    prerequisites = emptyList(),
                    unlockCriteria = null,
                    completionPoints = 60,
                    achievementId = "color_sorter",
                    supportsAdaptiveDifficulty = true,
                    minimumAccuracy = 0.8f,
                    maxAttempts = 3
                ),
                
                Activity(
                    id = "activity_size_sorting",
                    name = "Size Sorting",
                    description = "Sort objects from smallest to largest",
                    type = "sorting",
                    category = "math",
                    difficultyLevel = 3,
                    ageRange = "3-4",
                    estimatedTime = 8,
                    educationalObjectives = listOf("Size comparison", "Sequential ordering", "Mathematical concepts"),
                    targetSkills = listOf("size_comparison", "sequencing", "mathematical_reasoning"),
                    configuration = ActivityConfiguration(
                        itemCount = 5,
                        timeLimit = 480,
                        showHints = false,
                        allowRetries = true,
                        parameters = mapOf(
                            "objects" to listOf("ball", "car", "house", "tree", "mountain"),
                            "sortType" to "smallest_to_largest"
                        )
                    ),
                    prerequisites = listOf("complete_color_sorting"),
                    unlockCriteria = "sorting_milestone_1",
                    completionPoints = 100,
                    achievementId = "size_master",
                    supportsAdaptiveDifficulty = true,
                    minimumAccuracy = 0.6f,
                    maxAttempts = 3
                ),
                
                // Pattern Recognition
                Activity(
                    id = "activity_pattern_completion",
                    name = "Pattern Completion",
                    description = "Complete the missing part of the pattern",
                    type = "pattern",
                    category = "math",
                    difficultyLevel = 2,
                    ageRange = "3-4",
                    estimatedTime = 10,
                    educationalObjectives = listOf("Pattern recognition", "Logical reasoning", "Predictive thinking"),
                    targetSkills = listOf("pattern_recognition", "logical_reasoning", "prediction"),
                    configuration = ActivityConfiguration(
                        itemCount = 6,
                        timeLimit = 600,
                        showHints = true,
                        allowRetries = true,
                        parameters = mapOf(
                            "patternTypes" to listOf("ABAB", "AABB", "ABC"),
                            "elements" to listOf("shapes", "colors", "letters")
                        )
                    ),
                    prerequisites = listOf("complete_basic_matching"),
                    unlockCriteria = "pattern_milestone_1",
                    completionPoints = 80,
                    achievementId = "pattern_detective",
                    supportsAdaptiveDifficulty = true,
                    minimumAccuracy = 0.7f,
                    maxAttempts = 3
                ),
                
                // Memory Games
                Activity(
                    id = "activity_memory_cards",
                    name = "Memory Cards",
                    description = "Find matching pairs by remembering card positions",
                    type = "memory",
                    category = "cognitive",
                    difficultyLevel = 2,
                    ageRange = "3-4",
                    estimatedTime = 12,
                    educationalObjectives = listOf("Memory development", "Concentration", "Visual recall"),
                    targetSkills = listOf("memory", "concentration", "visual_recall"),
                    configuration = ActivityConfiguration(
                        itemCount = 8,
                        timeLimit = 720,
                        showHints = false,
                        allowRetries = true,
                        parameters = mapOf(
                            "cardTypes" to listOf("letters", "animals", "shapes"),
                            "gridSize" to "4x2"
                        )
                    ),
                    prerequisites = listOf("complete_basic_activities"),
                    unlockCriteria = "memory_milestone_1",
                    completionPoints = 90,
                    achievementId = "memory_champion",
                    supportsAdaptiveDifficulty = true,
                    minimumAccuracy = 0.5f,
                    maxAttempts = 3
                )
            )
        }
        
        /**
         * Get activities by type
         */
        fun getActivitiesByType(type: String): List<Activity> {
            return getDefaultActivities().filter { it.type == type }
        }
        
        /**
         * Get activities by category
         */
        fun getActivitiesByCategory(category: String): List<Activity> {
            return getDefaultActivities().filter { it.category == category }
        }
        
        /**
         * Get activities by difficulty level
         */
        fun getActivitiesByDifficulty(level: Int): List<Activity> {
            return getDefaultActivities().filter { it.difficultyLevel == level }
        }
        
        /**
         * Get activities by age range
         */
        fun getActivitiesByAgeRange(ageRange: String): List<Activity> {
            return getDefaultActivities().filter { it.ageRange == ageRange }
        }
    }
}

/**
 * Activity configuration parameters
 */
data class ActivityConfiguration(
    /**
     * Number of items/questions in the activity
     */
    val itemCount: Int,
    
    /**
     * Time limit in seconds (0 = no limit)
     */
    val timeLimit: Int,
    
    /**
     * Whether to show hints during activity
     */
    val showHints: Boolean,
    
    /**
     * Whether retries are allowed
     */
    val allowRetries: Boolean,
    
    /**
     * Additional activity-specific parameters
     */
    val parameters: Map<String, Any> = emptyMap()
)
