package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Phonics Word data class for the Happy Kid app
 * Phase 9: Phonics and Early Reading Engine database entity
 *
 * Represents words used in phonics instruction including CVC words,
 * sight words, and progressive reading vocabulary suitable for toddlers.
 */
@Entity(tableName = "phonics_words")
data class PhonicsWord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Word identification
    val word: String,
    val category: WordCategory,
    val difficultyLevel: Int = 1, // 1-5 scale
    
    // Phonics breakdown
    val phonemes: List<String>, // Individual sounds: ["b", "a", "t"]
    val syllables: List<String>, // Syllable breakdown: ["bat"]
    val letterSounds: List<String>, // Letter-sound mapping
    
    // Learning metadata
    val isSightWord: Boolean = false,
    val isHighFrequency: Boolean = false,
    val ageAppropriate: IntRange = 2..4, // Age range in years
    
    // Audio resources
    val audioFileName: String? = null,
    val pronunciationGuide: String = "",
    
    // Visual aids
    val imageFileName: String? = null,
    val colorHex: String = "#4CAF50",
    
    // Learning progress
    val timesPresented: Int = 0,
    val timesRecognized: Int = 0,
    val lastPresentedTimestamp: Long = 0L,
    
    // Phonics instruction data
    val blendingSteps: List<String> = emptyList(), // ["b-a", "ba-t", "bat"]
    val rhymingWords: List<String> = emptyList(),
    val wordFamily: String? = null // "-at", "-an", etc.
) {
    
    /**
     * Calculate recognition accuracy percentage
     */
    val recognitionAccuracy: Float
        get() = if (timesPresented > 0) {
            (timesRecognized.toFloat() / timesPresented.toFloat()) * 100f
        } else 0f
    
    /**
     * Check if word is mastered (80% accuracy with minimum attempts)
     */
    val isMastered: Boolean
        get() = timesPresented >= 3 && recognitionAccuracy >= 80f
    
    companion object {
        
        /**
         * Generate default CVC words for phonics instruction
         */
        fun getDefaultCVCWords(): List<PhonicsWord> = listOf(
            // -at family
            PhonicsWord(
                word = "cat",
                category = WordCategory.CVC,
                difficultyLevel = 1,
                phonemes = listOf("k", "æ", "t"),
                syllables = listOf("cat"),
                letterSounds = listOf("c=/k/", "a=/æ/", "t=/t/"),
                blendingSteps = listOf("c-a", "ca-t", "cat"),
                rhymingWords = listOf("bat", "hat", "mat", "rat"),
                wordFamily = "-at",
                pronunciationGuide = "/kæt/"
            ),
            PhonicsWord(
                word = "bat",
                category = WordCategory.CVC,
                difficultyLevel = 1,
                phonemes = listOf("b", "æ", "t"),
                syllables = listOf("bat"),
                letterSounds = listOf("b=/b/", "a=/æ/", "t=/t/"),
                blendingSteps = listOf("b-a", "ba-t", "bat"),
                rhymingWords = listOf("cat", "hat", "mat", "rat"),
                wordFamily = "-at",
                pronunciationGuide = "/bæt/"
            ),
            PhonicsWord(
                word = "hat",
                category = WordCategory.CVC,
                difficultyLevel = 1,
                phonemes = listOf("h", "æ", "t"),
                syllables = listOf("hat"),
                letterSounds = listOf("h=/h/", "a=/æ/", "t=/t/"),
                blendingSteps = listOf("h-a", "ha-t", "hat"),
                rhymingWords = listOf("cat", "bat", "mat", "rat"),
                wordFamily = "-at",
                pronunciationGuide = "/hæt/"
            ),
            
            // -an family
            PhonicsWord(
                word = "man",
                category = WordCategory.CVC,
                difficultyLevel = 1,
                phonemes = listOf("m", "æ", "n"),
                syllables = listOf("man"),
                letterSounds = listOf("m=/m/", "a=/æ/", "n=/n/"),
                blendingSteps = listOf("m-a", "ma-n", "man"),
                rhymingWords = listOf("can", "fan", "pan", "ran"),
                wordFamily = "-an",
                pronunciationGuide = "/mæn/"
            ),
            PhonicsWord(
                word = "can",
                category = WordCategory.CVC,
                difficultyLevel = 1,
                phonemes = listOf("k", "æ", "n"),
                syllables = listOf("can"),
                letterSounds = listOf("c=/k/", "a=/æ/", "n=/n/"),
                blendingSteps = listOf("c-a", "ca-n", "can"),
                rhymingWords = listOf("man", "fan", "pan", "ran"),
                wordFamily = "-an",
                pronunciationGuide = "/kæn/"
            ),
            
            // -ig family
            PhonicsWord(
                word = "big",
                category = WordCategory.CVC,
                difficultyLevel = 2,
                phonemes = listOf("b", "ɪ", "g"),
                syllables = listOf("big"),
                letterSounds = listOf("b=/b/", "i=/ɪ/", "g=/g/"),
                blendingSteps = listOf("b-i", "bi-g", "big"),
                rhymingWords = listOf("dig", "fig", "pig", "wig"),
                wordFamily = "-ig",
                pronunciationGuide = "/bɪg/"
            ),
            PhonicsWord(
                word = "pig",
                category = WordCategory.CVC,
                difficultyLevel = 2,
                phonemes = listOf("p", "ɪ", "g"),
                syllables = listOf("pig"),
                letterSounds = listOf("p=/p/", "i=/ɪ/", "g=/g/"),
                blendingSteps = listOf("p-i", "pi-g", "pig"),
                rhymingWords = listOf("big", "dig", "fig", "wig"),
                wordFamily = "-ig",
                pronunciationGuide = "/pɪg/"
            ),
            
            // -og family
            PhonicsWord(
                word = "dog",
                category = WordCategory.CVC,
                difficultyLevel = 2,
                phonemes = listOf("d", "ɔ", "g"),
                syllables = listOf("dog"),
                letterSounds = listOf("d=/d/", "o=/ɔ/", "g=/g/"),
                blendingSteps = listOf("d-o", "do-g", "dog"),
                rhymingWords = listOf("log", "fog", "hog", "jog"),
                wordFamily = "-og",
                pronunciationGuide = "/dɔg/"
            ),
            PhonicsWord(
                word = "log",
                category = WordCategory.CVC,
                difficultyLevel = 2,
                phonemes = listOf("l", "ɔ", "g"),
                syllables = listOf("log"),
                letterSounds = listOf("l=/l/", "o=/ɔ/", "g=/g/"),
                blendingSteps = listOf("l-o", "lo-g", "log"),
                rhymingWords = listOf("dog", "fog", "hog", "jog"),
                wordFamily = "-og",
                pronunciationGuide = "/lɔg/"
            )
        )
        
        /**
         * Generate default sight words for early reading
         */
        fun getDefaultSightWords(): List<PhonicsWord> = listOf(
            PhonicsWord(
                word = "the",
                category = WordCategory.SIGHT_WORD,
                difficultyLevel = 1,
                phonemes = listOf("ð", "ə"),
                syllables = listOf("the"),
                letterSounds = listOf("th=/ð/", "e=/ə/"),
                isSightWord = true,
                isHighFrequency = true,
                pronunciationGuide = "/ðə/"
            ),
            PhonicsWord(
                word = "and",
                category = WordCategory.SIGHT_WORD,
                difficultyLevel = 1,
                phonemes = listOf("æ", "n", "d"),
                syllables = listOf("and"),
                letterSounds = listOf("a=/æ/", "n=/n/", "d=/d/"),
                isSightWord = true,
                isHighFrequency = true,
                pronunciationGuide = "/ænd/"
            ),
            PhonicsWord(
                word = "you",
                category = WordCategory.SIGHT_WORD,
                difficultyLevel = 2,
                phonemes = listOf("j", "u"),
                syllables = listOf("you"),
                letterSounds = listOf("y=/j/", "ou=/u/"),
                isSightWord = true,
                isHighFrequency = true,
                pronunciationGuide = "/ju/"
            ),
            PhonicsWord(
                word = "see",
                category = WordCategory.SIGHT_WORD,
                difficultyLevel = 2,
                phonemes = listOf("s", "i"),
                syllables = listOf("see"),
                letterSounds = listOf("s=/s/", "ee=/i/"),
                isSightWord = true,
                isHighFrequency = true,
                pronunciationGuide = "/si/"
            ),
            PhonicsWord(
                word = "go",
                category = WordCategory.SIGHT_WORD,
                difficultyLevel = 1,
                phonemes = listOf("g", "oʊ"),
                syllables = listOf("go"),
                letterSounds = listOf("g=/g/", "o=/oʊ/"),
                isSightWord = true,
                isHighFrequency = true,
                pronunciationGuide = "/goʊ/"
            )
        )
    }
}

/**
 * Word categories for phonics instruction
 */
enum class WordCategory {
    CVC,           // Consonant-Vowel-Consonant (cat, dog, big)
    SIGHT_WORD,    // High-frequency words (the, and, you)
    WORD_FAMILY,   // Rhyming word groups (-at, -an, -ig)
    COMPOUND,      // Simple compound words (sunhat, doghouse)
    SIMPLE_SENTENCE // Very basic sentences (I see a cat)
}
