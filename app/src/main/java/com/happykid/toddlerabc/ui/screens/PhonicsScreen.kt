package com.happykid.toddlerabc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.happykid.toddlerabc.model.PhonicsWord
import com.happykid.toddlerabc.model.WordCategory
import com.happykid.toddlerabc.ui.components.PhonicsCanvas
import com.happykid.toddlerabc.model.PhonicsActivityType
import com.happykid.toddlerabc.viewmodel.PhonicsViewModel

/**
 * Phonics Screen for Happy_Kid App
 * Phase 9: Main screen for phonics and early reading activities
 *
 * Provides comprehensive phonics learning interface with word recognition,
 * sound blending, sight word practice, and pronunciation activities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhonicsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhonicsViewModel = hiltViewModel()
) {
    // Collect state from ViewModel
    val phonicsWords by viewModel.phonicsWords.collectAsState()
    val currentWord by viewModel.currentWord.collectAsState()
    val currentActivity by viewModel.currentActivity.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val phonicsStats by viewModel.phonicsStats.collectAsState()
    val showHints by viewModel.showHints.collectAsState()
    val difficultyLevel by viewModel.difficultyLevel.collectAsState()

    // Local state for UI
    var selectedCategory by remember { mutableStateOf<WordCategory?>(null) }
    var showCategorySelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Phonics & Reading",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress overview
            phonicsStats?.let { stats ->
                PhonicsProgressOverview(
                    stats = stats,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Activity selector
            PhonicsActivitySelector(
                currentActivity = currentActivity,
                onActivitySelected = viewModel::setActivity,
                modifier = Modifier.fillMaxWidth()
            )

            // Word category filter
            WordCategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    selectedCategory = category
                    category?.let { viewModel.getWordsByCategory(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Word selection
            if (phonicsWords.isNotEmpty()) {
                WordSelectionRow(
                    words = phonicsWords,
                    currentWord = currentWord,
                    onWordSelected = viewModel::selectWord,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Main phonics canvas
            if (currentWord != null) {
                PhonicsCanvas(
                    currentWord = currentWord,
                    activityType = currentActivity,
                    onLetterSelected = viewModel::onLetterSelected,
                    onWordCompleted = viewModel::onWordCompleted,
                    onBlendingStepCompleted = viewModel::onBlendingStepCompleted,
                    onPronunciationAttempt = viewModel::onPronunciationAttempt,
                    showHints = showHints,
                    difficultyLevel = difficultyLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                // Loading or empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text(
                            text = "Select a word to start practicing!",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // Control buttons
            PhonicsControlButtons(
                onRandomWord = viewModel::getRandomWord,
                onNextWord = viewModel::getNextWord,
                onToggleHints = viewModel::toggleHints,
                showHints = showHints,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Error handling
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show error snackbar or dialog
            viewModel.clearError()
        }
    }
}

/**
 * Progress overview component
 */
@Composable
private fun PhonicsProgressOverview(
    stats: com.happykid.toddlerabc.model.PhonicsStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProgressItem(
                label = "Words Mastered",
                value = "${stats.wordsMastered}/${stats.totalPhonicsWords}"
            )
            ProgressItem(
                label = "Accuracy",
                value = "${stats.averageAccuracyAllWords.toInt()}%"
            )
            ProgressItem(
                label = "Streak",
                value = "${stats.currentStreak}"
            )
        }
    }
}

/**
 * Individual progress item
 */
@Composable
private fun ProgressItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

/**
 * Activity selector component
 */
@Composable
private fun PhonicsActivitySelector(
    currentActivity: PhonicsActivityType,
    onActivitySelected: (PhonicsActivityType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(PhonicsActivityType.values()) { activity ->
            FilterChip(
                selected = activity == currentActivity,
                onClick = { onActivitySelected(activity) },
                label = {
                    Text(
                        text = getActivityDisplayName(activity),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}

/**
 * Word category filter component
 */
@Composable
private fun WordCategoryFilter(
    selectedCategory: WordCategory?,
    onCategorySelected: (WordCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All Words") }
            )
        }

        items(WordCategory.values()) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = getCategoryDisplayName(category),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        }
    }
}

/**
 * Word selection row component
 */
@Composable
private fun WordSelectionRow(
    words: List<PhonicsWord>,
    currentWord: PhonicsWord?,
    onWordSelected: (PhonicsWord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words) { word ->
            WordCard(
                word = word,
                isSelected = word == currentWord,
                onClick = { onWordSelected(word) }
            )
        }
    }
}

/**
 * Individual word card
 */
@Composable
private fun WordCard(
    word: PhonicsWord,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = word.word,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

/**
 * Control buttons component
 */
@Composable
private fun PhonicsControlButtons(
    onRandomWord: () -> Unit,
    onNextWord: () -> Unit,
    onToggleHints: () -> Unit,
    showHints: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(
            onClick = onRandomWord,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Random Word")
        }

        Button(
            onClick = onNextWord,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Next Word")
        }

        Button(
            onClick = onToggleHints,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showHints) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        ) {
            Text(if (showHints) "Hide Hints" else "Show Hints")
        }
    }
}

/**
 * Get display name for activity type
 */
private fun getActivityDisplayName(activity: PhonicsActivityType): String {
    return when (activity) {
        PhonicsActivityType.WORD_BUILDING -> "Build Words"
        PhonicsActivityType.SOUND_BLENDING -> "Blend Sounds"
        PhonicsActivityType.SIGHT_WORD_PRACTICE -> "Sight Words"
        PhonicsActivityType.PRONUNCIATION -> "Pronunciation"
        PhonicsActivityType.WORD_RECOGNITION -> "Recognition"
        PhonicsActivityType.RHYMING_GAMES -> "Rhyming"
        PhonicsActivityType.READING_COMPREHENSION -> "Comprehension"
        PhonicsActivityType.WORD_FAMILY_SORTING -> "Word Families"
    }
}

/**
 * Get display name for word category
 */
private fun getCategoryDisplayName(category: WordCategory): String {
    return when (category) {
        WordCategory.CVC -> "CVC Words"
        WordCategory.SIGHT_WORD -> "Sight Words"
        WordCategory.WORD_FAMILY -> "Word Families"
        WordCategory.COMPOUND -> "Compound"
        WordCategory.SIMPLE_SENTENCE -> "Sentences"
    }
}
