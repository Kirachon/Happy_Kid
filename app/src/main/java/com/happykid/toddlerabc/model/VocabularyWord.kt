package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.happykid.toddlerabc.data.ContentTypeConverters

/**
 * Vocabulary word entity for expanded word learning
 * Phase 12: Content Expansion - Vocabulary expansion with visual associations
 *
 * Represents age-appropriate words with visual associations, audio pronunciation,
 * and spaced repetition learning algorithms.
 */
@Entity(tableName = "vocabulary_words")
@TypeConverters(ContentTypeConverters::class)
data class VocabularyWord(
    @PrimaryKey
    val id: String,
    
    /**
     * The vocabulary word
     */
    val word: String,
    
    /**
     * Word definition in toddler-friendly language
     */
    val definition: String,
    
    /**
     * Word category (animals, colors, food, family, etc.)
     */
    val category: String,
    
    /**
     * Difficulty level (1-5) for progressive learning
     */
    val difficultyLevel: Int,
    
    /**
     * Age range recommendation (e.g., "1-2", "2-3", "3-4")
     */
    val ageRange: String,
    
    /**
     * Associated image path for visual learning
     */
    val imagePath: String,
    
    /**
     * Audio pronunciation file path
     */
    val audioPath: String?,
    
    /**
     * Phonetic spelling for pronunciation guide
     */
    val phoneticSpelling: String?,
    
    /**
     * Related words for vocabulary expansion
     */
    val relatedWords: List<String>,
    
    /**
     * Example sentence using the word
     */
    val exampleSentence: String?,
    
    /**
     * Associated letters for alphabet connection
     */
    val associatedLetters: List<String>,
    
    /**
     * Word frequency score (higher = more common)
     */
    val frequencyScore: Int,
    
    /**
     * Learning activities associated with this word
     */
    val activities: List<String>,
    
    /**
     * Unlock criteria for this word
     */
    val unlockCriteria: String?,
    
    /**
     * Whether word is part of core vocabulary
     */
    val isCoreVocabulary: Boolean,
    
    /**
     * Spaced repetition interval in days
     */
    val repetitionInterval: Int = 1,
    
    /**
     * Next review date for spaced repetition
     */
    val nextReviewDate: Long = System.currentTimeMillis(),
    
    /**
     * Word creation timestamp
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * Last update timestamp
     */
    val updatedAt: Long = System.currentTimeMillis(),
    
    /**
     * Whether word is active in learning system
     */
    val isActive: Boolean = true
) {
    companion object {
        /**
         * Get default vocabulary words for initial app setup
         */
        fun getDefaultVocabularyWords(): List<VocabularyWord> {
            return listOf(
                // Animals category
                VocabularyWord(
                    id = "vocab_cat",
                    word = "cat",
                    definition = "A soft, furry animal that says meow",
                    category = "animals",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/animals/cat.png",
                    audioPath = "vocabulary/animals/cat.mp3",
                    phoneticSpelling = "kat",
                    relatedWords = listOf("kitten", "meow", "pet"),
                    exampleSentence = "The cat is sleeping on the mat.",
                    associatedLetters = listOf("C", "A", "T"),
                    frequencyScore = 95,
                    activities = listOf("matching", "pronunciation", "tracing"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                ),
                
                VocabularyWord(
                    id = "vocab_dog",
                    word = "dog",
                    definition = "A friendly animal that says woof and wags its tail",
                    category = "animals",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/animals/dog.png",
                    audioPath = "vocabulary/animals/dog.mp3",
                    phoneticSpelling = "dawg",
                    relatedWords = listOf("puppy", "woof", "pet"),
                    exampleSentence = "The dog likes to play fetch.",
                    associatedLetters = listOf("D", "O", "G"),
                    frequencyScore = 98,
                    activities = listOf("matching", "pronunciation", "tracing"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                ),
                
                VocabularyWord(
                    id = "vocab_bird",
                    word = "bird",
                    definition = "An animal with wings that can fly in the sky",
                    category = "animals",
                    difficultyLevel = 2,
                    ageRange = "2-3",
                    imagePath = "vocabulary/animals/bird.png",
                    audioPath = "vocabulary/animals/bird.mp3",
                    phoneticSpelling = "burd",
                    relatedWords = listOf("fly", "wings", "nest"),
                    exampleSentence = "The bird sings a beautiful song.",
                    associatedLetters = listOf("B", "I", "R", "D"),
                    frequencyScore = 85,
                    activities = listOf("matching", "pronunciation", "sorting"),
                    unlockCriteria = "complete_basic_animals",
                    isCoreVocabulary = true
                ),
                
                // Colors category
                VocabularyWord(
                    id = "vocab_red",
                    word = "red",
                    definition = "The color of apples and fire trucks",
                    category = "colors",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/colors/red.png",
                    audioPath = "vocabulary/colors/red.mp3",
                    phoneticSpelling = "red",
                    relatedWords = listOf("apple", "fire", "rose"),
                    exampleSentence = "The red apple is sweet.",
                    associatedLetters = listOf("R", "E", "D"),
                    frequencyScore = 92,
                    activities = listOf("color_matching", "pronunciation", "sorting"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                ),
                
                VocabularyWord(
                    id = "vocab_blue",
                    word = "blue",
                    definition = "The color of the sky and ocean",
                    category = "colors",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/colors/blue.png",
                    audioPath = "vocabulary/colors/blue.mp3",
                    phoneticSpelling = "bloo",
                    relatedWords = listOf("sky", "ocean", "water"),
                    exampleSentence = "The blue sky is beautiful.",
                    associatedLetters = listOf("B", "L", "U", "E"),
                    frequencyScore = 90,
                    activities = listOf("color_matching", "pronunciation", "sorting"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                ),
                
                // Food category
                VocabularyWord(
                    id = "vocab_apple",
                    word = "apple",
                    definition = "A round, sweet fruit that grows on trees",
                    category = "food",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/food/apple.png",
                    audioPath = "vocabulary/food/apple.mp3",
                    phoneticSpelling = "ap-ul",
                    relatedWords = listOf("fruit", "tree", "red"),
                    exampleSentence = "I eat a red apple for snack.",
                    associatedLetters = listOf("A", "P", "P", "L", "E"),
                    frequencyScore = 88,
                    activities = listOf("matching", "pronunciation", "counting"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                ),
                
                VocabularyWord(
                    id = "vocab_banana",
                    word = "banana",
                    definition = "A long, yellow fruit that monkeys love",
                    category = "food",
                    difficultyLevel = 2,
                    ageRange = "2-3",
                    imagePath = "vocabulary/food/banana.png",
                    audioPath = "vocabulary/food/banana.mp3",
                    phoneticSpelling = "buh-nan-uh",
                    relatedWords = listOf("yellow", "fruit", "monkey"),
                    exampleSentence = "The banana is yellow and sweet.",
                    associatedLetters = listOf("B", "A", "N", "A", "N", "A"),
                    frequencyScore = 82,
                    activities = listOf("matching", "pronunciation", "pattern"),
                    unlockCriteria = "complete_basic_food",
                    isCoreVocabulary = true
                ),
                
                // Family category
                VocabularyWord(
                    id = "vocab_mama",
                    word = "mama",
                    definition = "Another word for mommy or mother",
                    category = "family",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/family/mama.png",
                    audioPath = "vocabulary/family/mama.mp3",
                    phoneticSpelling = "mah-mah",
                    relatedWords = listOf("mommy", "mother", "family"),
                    exampleSentence = "I love my mama very much.",
                    associatedLetters = listOf("M", "A", "M", "A"),
                    frequencyScore = 100,
                    activities = listOf("pronunciation", "recognition", "emotional"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                ),
                
                VocabularyWord(
                    id = "vocab_dada",
                    word = "dada",
                    definition = "Another word for daddy or father",
                    category = "family",
                    difficultyLevel = 1,
                    ageRange = "1-2",
                    imagePath = "vocabulary/family/dada.png",
                    audioPath = "vocabulary/family/dada.mp3",
                    phoneticSpelling = "dah-dah",
                    relatedWords = listOf("daddy", "father", "family"),
                    exampleSentence = "Dada reads me bedtime stories.",
                    associatedLetters = listOf("D", "A", "D", "A"),
                    frequencyScore = 100,
                    activities = listOf("pronunciation", "recognition", "emotional"),
                    unlockCriteria = null,
                    isCoreVocabulary = true
                )
            )
        }
        
        /**
         * Get vocabulary words by category
         */
        fun getWordsByCategory(category: String): List<VocabularyWord> {
            return getDefaultVocabularyWords().filter { it.category == category }
        }
        
        /**
         * Get vocabulary words by age range
         */
        fun getWordsByAgeRange(ageRange: String): List<VocabularyWord> {
            return getDefaultVocabularyWords().filter { it.ageRange == ageRange }
        }
        
        /**
         * Get core vocabulary words
         */
        fun getCoreVocabulary(): List<VocabularyWord> {
            return getDefaultVocabularyWords().filter { it.isCoreVocabulary }
        }
    }
}
