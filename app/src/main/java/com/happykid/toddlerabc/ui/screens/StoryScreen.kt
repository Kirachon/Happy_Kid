package com.happykid.toddlerabc.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.happykid.toddlerabc.model.Story
import com.happykid.toddlerabc.viewmodel.StoryViewModel

/**
 * Story Screen for interactive story reading
 * Phase 12: Content Expansion - Interactive stories with educational integration
 *
 * Displays story library, reading interface, and interactive elements
 * with proper navigation and audio integration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: StoryViewModel = hiltViewModel()
) {
    val stories by viewModel.stories.collectAsState()
    val currentStory by viewModel.currentStory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isReading by viewModel.isReading.collectAsState()

    // Show error message if present
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Handle error display
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = if (isReading) currentStory?.title ?: "Story" else "Story Library",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    if (isReading) {
                        viewModel.stopReading()
                    } else {
                        onNavigateBack()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                if (isReading) {
                    // Audio toggle button
                    val isAudioEnabled by viewModel.isAudioEnabled.collectAsState()
                    IconButton(onClick = { viewModel.toggleAudio() }) {
                        Icon(
                            imageVector = if (isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isAudioEnabled) "Disable Audio" else "Enable Audio"
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary
            )
        )

        // Main Content
        if (isReading && currentStory != null) {
            StoryReaderContent(
                story = currentStory!!,
                viewModel = viewModel
            )
        } else {
            StoryLibraryContent(
                stories = stories,
                isLoading = isLoading,
                onStorySelected = { story ->
                    viewModel.startReading(story.id)
                },
                viewModel = viewModel
            )
        }
    }
}

/**
 * Story Library Content - Shows list of available stories
 */
@Composable
private fun StoryLibraryContent(
    stories: List<Story>,
    isLoading: Boolean,
    onStorySelected: (Story) -> Unit,
    viewModel: StoryViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search and Filter Section
        StoryFiltersSection(viewModel = viewModel)
        
        Spacer(modifier = Modifier.height(16.dp))

        // Stories List
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (stories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No stories available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(stories) { story ->
                    StoryCard(
                        story = story,
                        onClick = { onStorySelected(story) }
                    )
                }
            }
        }
    }
}

/**
 * Story Filters Section
 */
@Composable
private fun StoryFiltersSection(viewModel: StoryViewModel) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedDifficulty by viewModel.selectedDifficulty.collectAsState()

    Column {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchStories(it) },
            label = { Text("Search stories...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Category Filter
            FilterChip(
                onClick = { 
                    if (selectedCategory == "alphabet") {
                        viewModel.resetFilters()
                    } else {
                        viewModel.loadStoriesByCategory("alphabet")
                    }
                },
                label = { Text("Alphabet") },
                selected = selectedCategory == "alphabet"
            )

            FilterChip(
                onClick = { 
                    if (selectedCategory == "phonics") {
                        viewModel.resetFilters()
                    } else {
                        viewModel.loadStoriesByCategory("phonics")
                    }
                },
                label = { Text("Phonics") },
                selected = selectedCategory == "phonics"
            )

            FilterChip(
                onClick = { 
                    if (selectedCategory == "vocabulary") {
                        viewModel.resetFilters()
                    } else {
                        viewModel.loadStoriesByCategory("vocabulary")
                    }
                },
                label = { Text("Vocabulary") },
                selected = selectedCategory == "vocabulary"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Difficulty Filter
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            (1..3).forEach { level ->
                FilterChip(
                    onClick = { 
                        if (selectedDifficulty == level) {
                            viewModel.resetFilters()
                        } else {
                            viewModel.loadStoriesByDifficulty(level)
                        }
                    },
                    label = { Text("Level $level") },
                    selected = selectedDifficulty == level
                )
            }
        }
    }
}

/**
 * Story Card Component
 */
@Composable
private fun StoryCard(
    story: Story,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Story Title
            Text(
                text = story.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Story Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Difficulty and Age Range
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DifficultyChip(level = story.difficultyLevel)
                    AgeRangeChip(ageRange = story.ageRange)
                }

                // Reading Time
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Reading time",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${story.estimatedReadingTime} min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Educational Objectives
            Text(
                text = story.educationalObjectives.joinToString(" • "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

/**
 * Difficulty Chip Component
 */
@Composable
private fun DifficultyChip(level: Int) {
    val color = when (level) {
        1 -> Color(0xFF4CAF50) // Green
        2 -> Color(0xFFFF9800) // Orange
        3 -> Color(0xFFF44336) // Red
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Level $level",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Age Range Chip Component
 */
@Composable
private fun AgeRangeChip(ageRange: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Ages $ageRange",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Story Reader Content - Shows the actual story reading interface
 */
@Composable
private fun StoryReaderContent(
    story: Story,
    viewModel: StoryViewModel
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val currentInteractiveElement by viewModel.currentInteractiveElement.collectAsState()
    val highlightedWords by viewModel.highlightedWords.collectAsState()
    val interactionScore by viewModel.interactionScore.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Progress Bar
        LinearProgressIndicator(
            progress = readingProgress,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Score: $interactionScore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Story Content Area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Story Text with Interactive Elements
                StoryContentWithInteractions(
                    story = story,
                    highlightedWords = highlightedWords,
                    currentInteractiveElement = currentInteractiveElement,
                    onWordTapped = { word ->
                        viewModel.handleInteraction(word)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.previousPage() },
                enabled = currentPage > 0
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous page"
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Previous")
            }

            Text(
                text = "Page ${currentPage + 1}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Button(
                onClick = { viewModel.nextPage() }
            ) {
                Text("Next")
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next page"
                )
            }
        }
    }
}

/**
 * Story Content with Interactive Elements
 */
@Composable
private fun StoryContentWithInteractions(
    story: Story,
    highlightedWords: Set<String>,
    currentInteractiveElement: com.happykid.toddlerabc.model.InteractiveElement?,
    onWordTapped: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        // Interactive instruction
        currentInteractiveElement?.let { element ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = element.instruction,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Story content (simplified - would need more complex text parsing for real highlighting)
        Text(
            text = story.content,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 28.sp,
            textAlign = TextAlign.Justify,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    // Handle text interaction - simplified
                    currentInteractiveElement?.let { element ->
                        onWordTapped(element.target)
                    }
                }
        )
    }
}
