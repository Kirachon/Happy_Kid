package com.happykid.toddlerabc.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.happykid.toddlerabc.ui.components.TracingCanvas
import com.happykid.toddlerabc.viewmodel.TracingViewModel

/**
 * Tracing Screen for Happy_Kid App
 * Phase 8: Complete pre-writing and tracing system
 *
 * Provides letter selection, tracing canvas, progress tracking,
 * and navigation controls for comprehensive tracing experience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TracingScreen(
    onNavigateBack: () -> Unit,
    viewModel: TracingViewModel = hiltViewModel()
) {
    val letters by viewModel.letters.collectAsState()
    val currentLetter by viewModel.currentLetter.collectAsState()
    val currentLetterGuide by viewModel.currentLetterGuide.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showGuides by viewModel.showGuides.collectAsState()
    val difficultyLevel by viewModel.difficultyLevel.collectAsState()
    val lastTracingAccuracy by viewModel.lastTracingAccuracy.collectAsState()
    val isAudioEnabled by viewModel.isAudioEnabled.collectAsState()
    val isTracking by viewModel.isTracking.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Letter Tracing",
                        style = MaterialTheme.typography.titleLarge
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
                    // Audio toggle
                    IconButton(onClick = { viewModel.toggleAudio() }) {
                        Icon(
                            imageVector = if (isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isAudioEnabled) "Mute Audio" else "Enable Audio",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Guide toggle
                    IconButton(onClick = { viewModel.toggleGuides() }) {
                        Icon(
                            imageVector = if (showGuides) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showGuides) "Hide Guides" else "Show Guides",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Instructions and current letter display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Trace the letter with your finger!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    currentLetter?.let { letter ->
                        Text(
                            text = "Current Letter: ${letter.character}",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Show last accuracy if available
                        if (lastTracingAccuracy > 0f) {
                            Text(
                                text = "Last Accuracy: ${lastTracingAccuracy.toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    lastTracingAccuracy >= 75f -> MaterialTheme.colorScheme.primary
                                    lastTracingAccuracy >= 60f -> MaterialTheme.colorScheme.secondary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Letter selection row
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Select a Letter:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(letters) { letter ->
                            FilterChip(
                                onClick = { viewModel.selectLetter(letter) },
                                label = {
                                    Text(
                                        text = letter.character.toString(),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                },
                                selected = currentLetter?.character == letter.character,
                                enabled = !isTracking
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.getPreviousLetter() },
                    enabled = !isTracking
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIos,
                        contentDescription = "Previous Letter",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }

                Button(
                    onClick = { viewModel.clearTracing() },
                    enabled = !isTracking
                ) {
                    Text("Clear")
                }

                OutlinedButton(
                    onClick = { viewModel.getNextLetter() },
                    enabled = !isTracking
                ) {
                    Text("Next")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next Letter",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tracing Canvas
            if (currentLetter != null && currentLetterGuide != null) {
                TracingCanvas(
                    letterGuide = currentLetterGuide!!,
                    touchDetector = viewModel.getTouchDetector(),
                    hapticManager = viewModel.getHapticManager(),
                    onTracingComplete = viewModel::onTracingComplete,
                    showGuides = showGuides,
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
                            text = "Select a letter to start tracing!",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Error handling
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Show error message briefly then clear
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
        }
    }
}
