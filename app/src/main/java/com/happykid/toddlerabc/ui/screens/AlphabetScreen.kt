package com.happykid.toddlerabc.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.happykid.toddlerabc.viewmodel.AlphabetViewModel
import com.happykid.toddlerabc.viewmodel.SpeechViewModel

import com.happykid.toddlerabc.ui.components.SpeechButton
import com.happykid.toddlerabc.ui.components.MicrophonePermissionPrompt
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Alphabet Learning Screen with Navigation Component and Audio integration
 * Phase 5: Enhanced with dynamic font support and Media3 audio
 * Phase 6: Enhanced with Windows emulator touch optimization
 * Phase 7: Enhanced with speech recognition integration
 *
 * Maintains all existing functionality from Phase 1-6 while adding
 * speech recognition capabilities for toddler vocal practice.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AlphabetScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlphabetViewModel = hiltViewModel(),
    speechViewModel: SpeechViewModel = hiltViewModel()
) {
    val letters by viewModel.letters.collectAsState()
    val learningStats by viewModel.learningStats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Phase 4: Audio state
    val isAudioEnabled by viewModel.isAudioEnabled.collectAsState()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()

    // Phase 7: Speech recognition state
    val isListening by speechViewModel.isListening.collectAsState()
    val speechResult by speechViewModel.speechResult.collectAsState()
    val canUseSpeechRecognition by speechViewModel.canUseSpeechRecognition.collectAsState()
    val showPermissionDialog by speechViewModel.showPermissionDialog.collectAsState()
    val showParentalConsentDialog by speechViewModel.showParentalConsentDialog.collectAsState()
    val speechFeedbackMessage by speechViewModel.speechFeedbackMessage.collectAsState()

    // Phase 7: Permission handling
    val microphonePermissionState = rememberPermissionState(
        android.Manifest.permission.RECORD_AUDIO
    ) { isGranted ->
        speechViewModel.onPermissionResult(android.Manifest.permission.RECORD_AUDIO, isGranted)
    }

    // Update permission rationale state
    LaunchedEffect(microphonePermissionState.status.shouldShowRationale) {
        speechViewModel.updateShouldShowRationale(microphonePermissionState.status.shouldShowRationale)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Alphabet Learning",
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
                    // Phase 4: Audio toggle button
                    IconButton(
                        onClick = { viewModel.toggleAudio() }
                    ) {
                        Icon(
                            imageVector = if (isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isAudioEnabled) "Mute Audio" else "Enable Audio",
                            tint = if (isAudioPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimaryContainer
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions
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
                        text = "Tap letters to practice and hear pronunciation!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    if (isAudioEnabled) {
                        Text(
                            text = "🔊 Audio enabled - Letters will speak when tapped",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            text = "🔇 Audio disabled - Tap the volume icon to enable",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    // Display learning statistics
                    learningStats?.let { stats ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Progress: ${stats.learnedLetters}/${stats.totalLetters} letters learned (${stats.progressPercentage}%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "Total practices: ${stats.totalPractices}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Phase 7: Speech Recognition Section
            if (canUseSpeechRecognition || showPermissionDialog) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎤 Speech Practice",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (canUseSpeechRecognition) {
                            Text(
                                text = "Tap a letter, then use the microphone to practice speaking!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SpeechButton(
                                isListening = isListening,
                                speechResult = speechResult,
                                isEnabled = canUseSpeechRecognition,
                                onStartListening = { speechViewModel.startSpeechRecognition() },
                                onStopListening = { speechViewModel.stopSpeechRecognition() }
                            )

                            // Speech feedback message
                            speechFeedbackMessage?.let { message ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = message,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            Text(
                                text = "Enable microphone permission to practice speaking!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Error message display
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Alphabet Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(letters) { letter ->
                    Button(
                        onClick = {
                            viewModel.onLetterClicked(letter.character)
                            Log.d("AlphabetScreen",
                                "Letter ${letter.character} practiced! Count: ${letter.practiceCount + 1}")
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (letter.isLearned)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = letter.character.toString(),
                                style = MaterialTheme.typography.titleLarge
                            )
                            if (letter.practiceCount > 0) {
                                Text(
                                    text = "${letter.practiceCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                            if (letter.isLearned) {
                                Text(
                                    text = "✓",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Phase 7: Permission dialogs
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { speechViewModel.dismissPermissionDialog() },
            title = {
                Text("Microphone Permission")
            },
            text = {
                Text(speechViewModel.getPermissionStatusMessage())
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        speechViewModel.dismissPermissionDialog()
                        microphonePermissionState.launchPermissionRequest()
                    }
                ) {
                    Text("Allow")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { speechViewModel.dismissPermissionDialog() }
                ) {
                    Text("Not Now")
                }
            }
        )
    }

    if (showParentalConsentDialog) {
        AlertDialog(
            onDismissRequest = { speechViewModel.dismissParentalConsentDialog() },
            title = {
                Text("Parental Consent")
            },
            text = {
                Text(speechViewModel.getParentalConsentMessage())
            },
            confirmButton = {
                TextButton(
                    onClick = { speechViewModel.onParentalConsentResponse(true) }
                ) {
                    Text("I Consent")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { speechViewModel.onParentalConsentResponse(false) }
                ) {
                    Text("Not Now")
                }
            }
        )
    }
}
