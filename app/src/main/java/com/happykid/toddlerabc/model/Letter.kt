package com.happykid.toddlerabc.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Letter data class for the Happy Kid app
 * Enhanced with Room database annotations for Phase 1
 */
@Entity(tableName = "letters")
data class Letter(
    @PrimaryKey
    val character: Char,
    val name: String = character.toString(),
    val soundDescription: String = "Letter $character",
    val isLearned: Boolean = false,
    val colorHex: String = "#4CAF50",
    val practiceCount: Int = 0,
    val lastPracticedTimestamp: Long = 0L
) {
    companion object {
        /**
         * Generate all 26 letters of the alphabet
         */
        fun getAllLetters(): List<Letter> {
            val colors = listOf(
                "#E53935", "#1E88E5", "#43A047", "#FF7043",
                "#8E24AA", "#00ACC1", "#D81B60", "#3949AB",
                "#FFB300", "#E53935", "#1E88E5", "#43A047",
                "#FF7043", "#8E24AA", "#00ACC1", "#D81B60",
                "#3949AB", "#FFB300", "#E53935", "#1E88E5",
                "#43A047", "#FF7043", "#8E24AA", "#00ACC1",
                "#D81B60", "#3949AB"
            )

            return ('A'..'Z').mapIndexed { index, char ->
                Letter(
                    character = char,
                    name = char.toString(),
                    soundDescription = "Letter $char sounds like ${char.lowercase()}",
                    colorHex = colors[index],
                    practiceCount = 0,
                    lastPracticedTimestamp = 0L
                )
            }
        }

        /**
         * Get a specific letter by character
         */
        fun getLetter(char: Char): Letter {
            return getAllLetters().find { it.character == char.uppercaseChar() }
                ?: Letter(char.uppercaseChar())
        }
    }
}
