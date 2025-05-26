package com.happykid.toddlerabc.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Audio Manager for Happy Kid app
 * Phase 4: Centralized audio management with Media3 and TTS integration
 * Phase 7: Enhanced with speech recognition integration and prompts
 * Phase 9: Enhanced with phonics-specific audio features and word pronunciation
 *
 * Provides letter pronunciation, audio feedback, speech prompts, phonics blending,
 * word pronunciation, and media playback functionality with Windows emulator
 * optimizations and proper resource management.
 */
@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AudioManager"
        private const val TTS_UTTERANCE_ID = "letter_pronunciation"
    }

    // TTS Engine
    private var textToSpeech: TextToSpeech? = null
    private var isTtsInitialized = false

    // ExoPlayer for future audio file support
    private var exoPlayer: ExoPlayer? = null

    // Audio state management
    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentVolume = MutableStateFlow(0.8f)
    val currentVolume: StateFlow<Float> = _currentVolume.asStateFlow()

    init {
        initializeAudio()
    }

    /**
     * Initialize audio components with Windows emulator optimizations
     */
    private fun initializeAudio() {
        Log.d(TAG, "Initializing audio components for Windows emulator compatibility")

        // Initialize TTS
        initializeTTS()

        // Initialize ExoPlayer for future use
        initializeExoPlayer()
    }

    /**
     * Initialize Text-to-Speech engine
     */
    private fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "TTS language not supported, using default")
                } else {
                    isTtsInitialized = true
                    Log.d(TAG, "TTS initialized successfully")
                }

                // Configure TTS settings for toddler-friendly speech
                textToSpeech?.setSpeechRate(0.8f) // Slightly slower for clarity
                textToSpeech?.setPitch(1.1f) // Slightly higher pitch for children

                // Set utterance progress listener
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isPlaying.value = true
                        Log.d(TAG, "TTS started: $utteranceId")
                    }

                    override fun onDone(utteranceId: String?) {
                        _isPlaying.value = false
                        Log.d(TAG, "TTS completed: $utteranceId")
                    }

                    override fun onError(utteranceId: String?) {
                        _isPlaying.value = false
                        Log.e(TAG, "TTS error: $utteranceId")
                    }
                })
            } else {
                Log.e(TAG, "TTS initialization failed")
            }
        }
    }

    /**
     * Initialize ExoPlayer for future audio file support
     */
    private fun initializeExoPlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(context).build()
            Log.d(TAG, "ExoPlayer initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "ExoPlayer initialization failed", e)
        }
    }

    /**
     * Play letter pronunciation using TTS
     */
    fun playLetterPronunciation(letter: Char) {
        if (!_isAudioEnabled.value || !isTtsInitialized) {
            Log.d(TAG, "Audio disabled or TTS not initialized")
            return
        }

        val letterName = "Letter ${letter.uppercaseChar()}"
        val utteranceId = "${TTS_UTTERANCE_ID}_${letter.lowercaseChar()}"

        Log.d(TAG, "Playing pronunciation for letter: $letter")

        textToSpeech?.speak(
            letterName,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    /**
     * Play letter sound (phonics)
     */
    fun playLetterSound(letter: Char) {
        if (!_isAudioEnabled.value || !isTtsInitialized) {
            Log.d(TAG, "Audio disabled or TTS not initialized")
            return
        }

        // Create phonetic sound for the letter
        val phoneticSound = getPhoneticSound(letter)
        val utteranceId = "${TTS_UTTERANCE_ID}_sound_${letter.lowercaseChar()}"

        Log.d(TAG, "Playing phonetic sound for letter: $letter")

        textToSpeech?.speak(
            phoneticSound,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    /**
     * Get phonetic sound for a letter
     */
    private fun getPhoneticSound(letter: Char): String {
        return when (letter.uppercaseChar()) {
            'A' -> "ah"
            'B' -> "buh"
            'C' -> "kuh"
            'D' -> "duh"
            'E' -> "eh"
            'F' -> "fuh"
            'G' -> "guh"
            'H' -> "huh"
            'I' -> "ih"
            'J' -> "juh"
            'K' -> "kuh"
            'L' -> "luh"
            'M' -> "muh"
            'N' -> "nuh"
            'O' -> "oh"
            'P' -> "puh"
            'Q' -> "kwuh"
            'R' -> "ruh"
            'S' -> "suh"
            'T' -> "tuh"
            'U' -> "uh"
            'V' -> "vuh"
            'W' -> "wuh"
            'X' -> "ksuh"
            'Y' -> "yuh"
            'Z' -> "zuh"
            else -> letter.toString()
        }
    }

    /**
     * Play success sound for completed practice
     */
    fun playSuccessSound() {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val successPhrases = listOf(
            "Great job!",
            "Well done!",
            "Excellent!",
            "Good work!"
        )

        val phrase = successPhrases.random()
        textToSpeech?.speak(phrase, TextToSpeech.QUEUE_ADD, null, "success_sound")
    }

    /**
     * Speak custom text for speech recognition prompts
     * Phase 7: Speech recognition integration
     */
    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val utteranceId = "speech_prompt_${System.currentTimeMillis()}"
        textToSpeech?.speak(text, queueMode, null, utteranceId)

        Log.d(TAG, "Speaking: $text")
    }

    /**
     * Play speech recognition encouragement
     * Phase 7: Positive reinforcement for vocal efforts
     */
    fun playEncouragement() {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val encouragements = listOf(
            "Good try!",
            "I heard you!",
            "Nice speaking!",
            "Keep trying!",
            "You're doing great!"
        )

        val phrase = encouragements.random()
        speak(phrase, TextToSpeech.QUEUE_ADD)
    }

    /**
     * Play speech recognition prompt for letter
     * Phase 7: Letter-specific speech prompts
     */
    fun playSpeechPrompt(letter: Char) {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val prompt = "Say the letter ${letter.uppercaseChar()}"
        speak(prompt)
    }

    /**
     * Toggle audio on/off
     */
    fun toggleAudio() {
        _isAudioEnabled.value = !_isAudioEnabled.value
        Log.d(TAG, "Audio toggled: ${_isAudioEnabled.value}")
    }

    /**
     * Set audio enabled state
     */
    fun setAudioEnabled(enabled: Boolean) {
        _isAudioEnabled.value = enabled
        Log.d(TAG, "Audio enabled set to: $enabled")
    }

    /**
     * Set volume level (0.0 to 1.0)
     */
    fun setVolume(volume: Float) {
        val clampedVolume = volume.coerceIn(0.0f, 1.0f)
        _currentVolume.value = clampedVolume

        // Apply volume to TTS if available
        textToSpeech?.let { tts ->
            // TTS doesn't have direct volume control, but we can adjust speech rate as a proxy
            val adjustedRate = 0.6f + (clampedVolume * 0.4f) // Range: 0.6 to 1.0
            tts.setSpeechRate(adjustedRate)
        }

        Log.d(TAG, "Volume set to: $clampedVolume")
    }

    /**
     * Stop all audio playback
     */
    fun stopAudio() {
        textToSpeech?.stop()
        exoPlayer?.stop()
        _isPlaying.value = false
        Log.d(TAG, "All audio stopped")
    }

    // Phase 9: Phonics-specific audio methods

    /**
     * Pronounce a complete word
     */
    fun pronounceWord(word: String) {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val utteranceId = "word_pronunciation_${word.lowercase()}"
        Log.d(TAG, "Pronouncing word: $word")

        textToSpeech?.speak(
            word,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    /**
     * Demonstrate phonics blending for a word
     * Example: "cat" -> "c-a-t" -> "cat"
     */
    fun demonstrateBlending(word: String, blendingSteps: List<String>) {
        if (!_isAudioEnabled.value || !isTtsInitialized || blendingSteps.isEmpty()) return

        Log.d(TAG, "Demonstrating blending for word: $word")

        // Speak each blending step with pauses
        blendingSteps.forEachIndexed { index, step ->
            val utteranceId = "blending_step_${word}_$index"
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD

            textToSpeech?.speak(
                step,
                queueMode,
                null,
                utteranceId
            )

            // Add a brief pause between steps (simulated by adding silence)
            if (index < blendingSteps.size - 1) {
                textToSpeech?.playSilentUtterance(300, TextToSpeech.QUEUE_ADD, null)
            }
        }

        // Finally, pronounce the complete word
        textToSpeech?.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
        textToSpeech?.speak(
            word,
            TextToSpeech.QUEUE_ADD,
            null,
            "blending_final_$word"
        )
    }

    /**
     * Pronounce individual phonemes in a word
     */
    fun pronouncePhonemes(phonemes: List<String>) {
        if (!_isAudioEnabled.value || !isTtsInitialized || phonemes.isEmpty()) return

        Log.d(TAG, "Pronouncing phonemes: ${phonemes.joinToString("-")}")

        phonemes.forEachIndexed { index, phoneme ->
            val utteranceId = "phoneme_$index"
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD

            textToSpeech?.speak(
                phoneme,
                queueMode,
                null,
                utteranceId
            )

            // Brief pause between phonemes
            if (index < phonemes.size - 1) {
                textToSpeech?.playSilentUtterance(200, TextToSpeech.QUEUE_ADD, null)
            }
        }
    }

    /**
     * Provide pronunciation coaching feedback
     */
    fun providePronunciationFeedback(isCorrect: Boolean, targetWord: String) {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val feedback = if (isCorrect) {
            listOf(
                "Perfect pronunciation!",
                "You said $targetWord correctly!",
                "Excellent speaking!",
                "Great job saying $targetWord!"
            ).random()
        } else {
            listOf(
                "Let's try $targetWord again",
                "Listen carefully: $targetWord",
                "Here's how to say $targetWord",
                "Try saying $targetWord like this"
            ).random()
        }

        speak(feedback)

        // If incorrect, demonstrate the correct pronunciation
        if (!isCorrect) {
            textToSpeech?.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)
            pronounceWord(targetWord)
        }
    }

    /**
     * Play word family rhyming demonstration
     */
    fun demonstrateRhyming(wordFamily: String, words: List<String>) {
        if (!_isAudioEnabled.value || !isTtsInitialized || words.isEmpty()) return

        Log.d(TAG, "Demonstrating rhyming for family: $wordFamily")

        val intro = "Listen to these rhyming words"
        speak(intro)

        textToSpeech?.playSilentUtterance(500, TextToSpeech.QUEUE_ADD, null)

        words.forEachIndexed { index, word ->
            val utteranceId = "rhyme_${wordFamily}_$index"
            textToSpeech?.speak(
                word,
                TextToSpeech.QUEUE_ADD,
                null,
                utteranceId
            )

            // Pause between words
            textToSpeech?.playSilentUtterance(400, TextToSpeech.QUEUE_ADD, null)
        }

        // Conclude with family pattern
        textToSpeech?.speak(
            "They all end with $wordFamily",
            TextToSpeech.QUEUE_ADD,
            null,
            "rhyme_conclusion"
        )
    }

    /**
     * Provide reading comprehension audio support
     */
    fun provideReadingSupport(word: String, definition: String) {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val support = "$word means $definition"
        speak(support)
    }

    /**
     * Play phonics activity instructions
     */
    fun playPhonicsInstructions(activityType: String) {
        if (!_isAudioEnabled.value || !isTtsInitialized) return

        val instructions = when (activityType.lowercase()) {
            "word_recognition" -> "Look at the word and say it out loud"
            "sound_blending" -> "Listen to the sounds and blend them together"
            "sight_word_practice" -> "Look at this word and remember how it looks"
            "pronunciation" -> "Listen carefully and repeat the word"
            "word_building" -> "Put the letters together to make a word"
            "rhyming_games" -> "Find words that sound the same at the end"
            else -> "Let's practice reading together"
        }

        speak(instructions)
    }

    /**
     * Release audio resources
     */
    fun release() {
        Log.d(TAG, "Releasing audio resources")

        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null

        exoPlayer?.release()
        exoPlayer = null

        isTtsInitialized = false
    }
}
