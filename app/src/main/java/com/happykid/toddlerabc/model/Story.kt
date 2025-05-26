package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.happykid.toddlerabc.data.ContentTypeConverters

/**
 * Story entity for interactive story system
 * Phase 12: Content Expansion - Interactive stories with educational integration
 *
 * Represents educational stories with letter recognition highlights,
 * phonics integration, and progressive difficulty levels.
 */
@Entity(tableName = "stories")
@TypeConverters(ContentTypeConverters::class)
data class Story(
    @PrimaryKey
    val id: String,
    
    /**
     * Story title for display
     */
    val title: String,
    
    /**
     * Story content with markup for letter highlighting
     */
    val content: String,
    
    /**
     * Target letters for recognition practice
     */
    val targetLetters: List<String>,
    
    /**
     * Phonics words featured in the story
     */
    val phonicsWords: List<String>,
    
    /**
     * Difficulty level (1-5) for adaptive progression
     */
    val difficultyLevel: Int,
    
    /**
     * Estimated reading time in minutes
     */
    val estimatedReadingTime: Int,
    
    /**
     * Age range recommendation (e.g., "2-3", "3-4")
     */
    val ageRange: String,
    
    /**
     * Educational objectives for this story
     */
    val educationalObjectives: List<String>,
    
    /**
     * Story category (e.g., "alphabet", "phonics", "vocabulary")
     */
    val category: String,
    
    /**
     * Whether story requires completion of prerequisites
     */
    val hasPrerequisites: Boolean,
    
    /**
     * List of prerequisite story IDs
     */
    val prerequisites: List<String>,
    
    /**
     * Story unlock criteria based on achievements
     */
    val unlockCriteria: String?,
    
    /**
     * Audio narration file path (optional)
     */
    val audioPath: String?,
    
    /**
     * Illustration image paths for story scenes
     */
    val illustrations: List<String>,
    
    /**
     * Interactive elements configuration
     */
    val interactiveElements: List<InteractiveElement>,
    
    /**
     * Story creation timestamp
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Last update timestamp
     */
    val updatedAt: Long = System.currentTimeMillis(),
    
    /**
     * Whether story is active/published
     */
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Get default stories for initial app setup
         */
        fun getDefaultStories(): List<Story> {
            return listOf(
                Story(
                    id = "story_alphabet_adventure",
                    title = "Alphabet Adventure",
                    content = "Once upon a time, there was a little <highlight>A</highlight> who loved to play. " +
                            "The <highlight>A</highlight> met a bouncing <highlight>B</highlight> and a curious <highlight>C</highlight>. " +
                            "Together they went on an amazing adventure through the alphabet land!",
                    targetLetters = listOf("A", "B", "C"),
                    phonicsWords = listOf("apple", "ball", "cat"),
                    difficultyLevel = 1,
                    estimatedReadingTime = 3,
                    ageRange = "2-3",
                    educationalObjectives = listOf("Letter recognition", "Phonics awareness", "Story comprehension"),
                    category = "alphabet",
                    hasPrerequisites = false,
                    prerequisites = emptyList(),
                    unlockCriteria = null,
                    audioPath = "stories/alphabet_adventure.mp3",
                    illustrations = listOf("stories/alphabet_adventure_1.png", "stories/alphabet_adventure_2.png"),
                    interactiveElements = listOf(
                        InteractiveElement("tap_letter", "A", "Tap the letter A"),
                        InteractiveElement("tap_letter", "B", "Tap the letter B"),
                        InteractiveElement("tap_letter", "C", "Tap the letter C")
                    )
                ),
                
                Story(
                    id = "story_phonics_fun",
                    title = "Phonics Fun with Friends",
                    content = "The <highlight>cat</highlight> sat on a <highlight>mat</highlight>. " +
                            "A <highlight>bat</highlight> flew by and said 'Hello <highlight>cat</highlight>!' " +
                            "They became best friends and played together every day.",
                    targetLetters = listOf("C", "A", "T", "M", "B"),
                    phonicsWords = listOf("cat", "mat", "bat", "sat"),
                    difficultyLevel = 2,
                    estimatedReadingTime = 4,
                    ageRange = "3-4",
                    educationalObjectives = listOf("CVC word recognition", "Rhyming patterns", "Reading fluency"),
                    category = "phonics",
                    hasPrerequisites = true,
                    prerequisites = listOf("story_alphabet_adventure"),
                    unlockCriteria = "complete_alphabet_basics",
                    audioPath = "stories/phonics_fun.mp3",
                    illustrations = listOf("stories/phonics_fun_1.png", "stories/phonics_fun_2.png", "stories/phonics_fun_3.png"),
                    interactiveElements = listOf(
                        InteractiveElement("tap_word", "cat", "Tap the word 'cat'"),
                        InteractiveElement("tap_word", "mat", "Tap the word 'mat'"),
                        InteractiveElement("tap_word", "bat", "Tap the word 'bat'")
                    )
                ),
                
                Story(
                    id = "story_vocabulary_garden",
                    title = "The Vocabulary Garden",
                    content = "In the beautiful garden, there are many <highlight>flowers</highlight>. " +
                            "Red <highlight>roses</highlight>, yellow <highlight>sunflowers</highlight>, and purple <highlight>violets</highlight>. " +
                            "The <highlight>butterfly</highlight> visits each flower and says their names.",
                    targetLetters = listOf("F", "R", "S", "V", "B"),
                    phonicsWords = listOf("flower", "rose", "sun", "violet"),
                    difficultyLevel = 3,
                    estimatedReadingTime = 5,
                    ageRange = "3-4",
                    educationalObjectives = listOf("Vocabulary expansion", "Color recognition", "Nature awareness"),
                    category = "vocabulary",
                    hasPrerequisites = true,
                    prerequisites = listOf("story_alphabet_adventure", "story_phonics_fun"),
                    unlockCriteria = "complete_phonics_basics",
                    audioPath = "stories/vocabulary_garden.mp3",
                    illustrations = listOf("stories/vocabulary_garden_1.png", "stories/vocabulary_garden_2.png"),
                    interactiveElements = listOf(
                        InteractiveElement("tap_word", "flowers", "Tap the flowers"),
                        InteractiveElement("tap_word", "butterfly", "Tap the butterfly"),
                        InteractiveElement("color_match", "red", "Find the red roses")
                    )
                )
            )
        }
    }
}

/**
 * Interactive element within a story
 */
data class InteractiveElement(
    /**
     * Type of interaction (tap_letter, tap_word, color_match, etc.)
     */
    val type: String,
    
    /**
     * Target element for interaction
     */
    val target: String,
    
    /**
     * Instruction text for the interaction
     */
    val instruction: String,
    
    /**
     * Optional feedback message on successful interaction
     */
    val feedback: String? = null,
    
    /**
     * Points awarded for successful interaction
     */
    val points: Int = 10
)
