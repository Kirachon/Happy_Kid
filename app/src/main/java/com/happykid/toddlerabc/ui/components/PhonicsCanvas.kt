package com.happykid.toddlerabc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happykid.toddlerabc.model.PhonicsWord
import com.happykid.toddlerabc.model.PhonicsActivityType as ModelPhonicsActivityType

/**
 * Phonics Canvas Component for Happy_Kid App
 * Phase 9: Interactive word building and phonics practice surface
 *
 * Provides interactive phonics activities including word building, sound blending,
 * and pronunciation practice optimized for toddler interaction patterns.
 */
@Composable
fun PhonicsCanvas(
    currentWord: PhonicsWord?,
    activityType: ModelPhonicsActivityType,
    onLetterSelected: (Char) -> Unit,
    onWordCompleted: (String) -> Unit,
    onBlendingStepCompleted: (Int) -> Unit,
    onPronunciationAttempt: () -> Unit,
    modifier: Modifier = Modifier,
    showHints: Boolean = true,
    difficultyLevel: Int = 1
) {
    var builtWord by remember { mutableStateOf("") }
    var currentBlendingStep by remember { mutableIntStateOf(0) }
    var showFeedback by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf("") }

    // Reset state when word changes
    LaunchedEffect(currentWord) {
        builtWord = ""
        currentBlendingStep = 0
        showFeedback = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Activity header
        PhonicsActivityHeader(
            activityType = activityType,
            currentWord = currentWord,
            modifier = Modifier.fillMaxWidth()
        )

        // Main activity area
        when (activityType) {
            ModelPhonicsActivityType.WORD_BUILDING -> {
                WordBuildingActivity(
                    targetWord = currentWord?.word ?: "",
                    builtWord = builtWord,
                    onLetterSelected = { letter ->
                        builtWord += letter
                        onLetterSelected(letter)

                        // Check if word is complete
                        if (builtWord.equals(currentWord?.word, ignoreCase = true)) {
                            onWordCompleted(builtWord)
                            showFeedback = true
                            feedbackMessage = "Great job! You built the word correctly!"
                        }
                    },
                    onClear = { builtWord = "" },
                    showHints = showHints,
                    modifier = Modifier.weight(1f)
                )
            }

            ModelPhonicsActivityType.SOUND_BLENDING -> {
                SoundBlendingActivity(
                    word = currentWord,
                    currentStep = currentBlendingStep,
                    onStepCompleted = { step ->
                        currentBlendingStep = step + 1
                        onBlendingStepCompleted(step)

                        if (currentWord != null && step >= currentWord.blendingSteps.size - 1) {
                            showFeedback = true
                            feedbackMessage = "Excellent blending!"
                        }
                    },
                    showHints = showHints,
                    modifier = Modifier.weight(1f)
                )
            }

            ModelPhonicsActivityType.SIGHT_WORD_PRACTICE -> {
                SightWordActivity(
                    word = currentWord,
                    onWordRecognized = {
                        onWordCompleted(currentWord?.word ?: "")
                        showFeedback = true
                        feedbackMessage = "Perfect! You recognized the sight word!"
                    },
                    showHints = showHints,
                    modifier = Modifier.weight(1f)
                )
            }

            ModelPhonicsActivityType.PRONUNCIATION -> {
                PronunciationActivity(
                    word = currentWord,
                    onPronunciationAttempt = onPronunciationAttempt,
                    showHints = showHints,
                    modifier = Modifier.weight(1f)
                )
            }

            else -> {
                // Default activity
                GeneralPhonicsActivity(
                    word = currentWord,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Feedback area
        if (showFeedback) {
            PhonicsActivityFeedback(
                message = feedbackMessage,
                isPositive = true,
                onDismiss = { showFeedback = false },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Activity controls
        PhonicsActivityControls(
            onHint = { /* Show hint */ },
            onRepeat = { /* Repeat audio */ },
            onNext = { /* Next activity */ },
            showHints = showHints,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Activity header showing current activity and word
 */
@Composable
private fun PhonicsActivityHeader(
    activityType: ModelPhonicsActivityType,
    currentWord: PhonicsWord?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = getActivityTitle(activityType),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            currentWord?.let { word ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (activityType == ModelPhonicsActivityType.SIGHT_WORD_PRACTICE) {
                        word.word
                    } else {
                        "Target: ${word.word}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Word building activity component
 */
@Composable
private fun WordBuildingActivity(
    targetWord: String,
    builtWord: String,
    onLetterSelected: (Char) -> Unit,
    onClear: () -> Unit,
    showHints: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Built word display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = builtWord.ifEmpty { "Build the word here..." },
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (builtWord.isEmpty()) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }

        // Letter selection area
        if (showHints && targetWord.isNotEmpty()) {
            Text(
                text = "Letters you need:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(targetWord.toCharArray().distinct()) { letter ->
                    LetterButton(
                        letter = letter,
                        onClick = { onLetterSelected(letter) },
                        isUsed = builtWord.contains(letter, ignoreCase = true)
                    )
                }
            }
        }

        // Clear button
        if (builtWord.isNotEmpty()) {
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Clear")
            }
        }
    }
}

/**
 * Letter button for word building
 */
@Composable
private fun LetterButton(
    letter: Char,
    onClick: () -> Unit,
    isUsed: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isUsed) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            .border(
                2.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(12.dp)
            )
            .clickable(enabled = !isUsed) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.uppercaseChar().toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (isUsed) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.onPrimary
            }
        )
    }
}

/**
 * Get activity title based on type
 */
private fun getActivityTitle(activityType: ModelPhonicsActivityType): String {
    return when (activityType) {
        ModelPhonicsActivityType.WORD_BUILDING -> "Build the Word"
        ModelPhonicsActivityType.SOUND_BLENDING -> "Blend the Sounds"
        ModelPhonicsActivityType.SIGHT_WORD_PRACTICE -> "Sight Word Practice"
        ModelPhonicsActivityType.PRONUNCIATION -> "Say the Word"
        ModelPhonicsActivityType.WORD_RECOGNITION -> "Recognize the Word"
        ModelPhonicsActivityType.RHYMING_GAMES -> "Find Rhyming Words"
        ModelPhonicsActivityType.READING_COMPREHENSION -> "Understand the Word"
        ModelPhonicsActivityType.WORD_FAMILY_SORTING -> "Sort Word Families"
    }
}

/**
 * Sound blending activity component
 */
@Composable
private fun SoundBlendingActivity(
    word: PhonicsWord?,
    currentStep: Int,
    onStepCompleted: (Int) -> Unit,
    showHints: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        word?.let { phonicsWord ->
            Text(
                text = "Blend these sounds together:",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(phonicsWord.blendingSteps.size) { index ->
                    BlendingStepCard(
                        step = phonicsWord.blendingSteps[index],
                        isActive = index == currentStep,
                        isCompleted = index < currentStep,
                        onClick = { onStepCompleted(index) }
                    )
                }
            }
        }
    }
}

/**
 * Sight word activity component
 */
@Composable
private fun SightWordActivity(
    word: PhonicsWord?,
    onWordRecognized: () -> Unit,
    showHints: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        word?.let { phonicsWord ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clickable { onWordRecognized() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phonicsWord.word,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Text(
                text = "Tap the word when you recognize it!",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Pronunciation activity component
 */
@Composable
private fun PronunciationActivity(
    word: PhonicsWord?,
    onPronunciationAttempt: () -> Unit,
    showHints: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        word?.let { phonicsWord ->
            Text(
                text = phonicsWord.word,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (showHints && phonicsWord.pronunciationGuide.isNotEmpty()) {
                Text(
                    text = "Pronunciation: ${phonicsWord.pronunciationGuide}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Button(
                onClick = onPronunciationAttempt,
                modifier = Modifier.size(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "🎤",
                    fontSize = 32.sp
                )
            }

            Text(
                text = "Tap the microphone and say the word",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * General phonics activity component
 */
@Composable
private fun GeneralPhonicsActivity(
    word: PhonicsWord?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        word?.let { phonicsWord ->
            Text(
                text = phonicsWord.word,
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Practice this word",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

/**
 * Blending step card component
 */
@Composable
private fun BlendingStepCard(
    step: String,
    isActive: Boolean,
    isCompleted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.primary
                isActive -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(60.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isCompleted -> MaterialTheme.colorScheme.onPrimary
                    isActive -> MaterialTheme.colorScheme.onSecondary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Activity feedback component
 */
@Composable
private fun PhonicsActivityFeedback(
    message: String,
    isPositive: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onDismiss() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = if (isPositive) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            }
        )
    }
}

/**
 * Activity controls component
 */
@Composable
private fun PhonicsActivityControls(
    onHint: () -> Unit,
    onRepeat: () -> Unit,
    onNext: () -> Unit,
    showHints: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (showHints) {
            Button(
                onClick = onHint,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Hint")
            }
        }

        Button(
            onClick = onRepeat,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text("Repeat")
        }

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Next")
        }
    }
}


