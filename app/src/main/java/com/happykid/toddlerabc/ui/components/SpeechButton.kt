package com.happykid.toddlerabc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happykid.toddlerabc.speech.SpeechResult

/**
 * Speech Button Component for Happy_Kid App
 * Phase 7: Animated microphone button with toddler-friendly feedback
 *
 * This component provides an engaging speech recognition interface with:
 * - Pulsing animation when listening
 * - Immediate positive feedback for vocal effort
 * - Toddler-friendly visual design
 * - Windows emulator touch optimization
 */
@Composable
fun SpeechButton(
    isListening: Boolean,
    speechResult: SpeechResult?,
    isEnabled: Boolean = true,
    targetLetter: Char? = null,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,

    modifier: Modifier = Modifier
) {
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "speech_button_animation")

    // Pulsing animation when listening
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Color animation based on speech result
    val buttonColor by animateColorAsState(
        targetValue = when {
            !isEnabled -> MaterialTheme.colorScheme.outline
            isListening -> MaterialTheme.colorScheme.primary
            speechResult is SpeechResult.Success -> Color(0xFF4CAF50) // Green for success
            speechResult is SpeechResult.VocalEffortDetected -> Color(0xFFFF9800) // Orange for effort
            else -> MaterialTheme.colorScheme.secondary
        },
        animationSpec = tween(300),
        label = "button_color"
    )

    // Icon animation
    val iconScale by animateFloatAsState(
        targetValue = when (speechResult) {
            is SpeechResult.Success, SpeechResult.VocalEffortDetected -> 1.3f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "icon_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Main speech button
        FloatingActionButton(
            onClick = {
                if (isListening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
            },
            modifier = Modifier
                .size(80.dp)
                .scale(if (isListening) pulseScale else 1f)
                .clickable(
                    enabled = isEnabled,
                    onClick = {
                        if (isListening) {
                            onStopListening()
                        } else {
                            onStartListening()
                        }
                    }
                ),
            containerColor = buttonColor,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = if (isListening) 12.dp else 6.dp
            )
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isListening) "Stop listening" else "Start speaking",
                modifier = Modifier.scale(iconScale),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status text
        Text(
            text = getStatusText(isListening, speechResult, targetLetter, isEnabled),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            ),
            color = when {
                !isEnabled -> MaterialTheme.colorScheme.outline
                speechResult is SpeechResult.Success -> Color(0xFF4CAF50)
                speechResult is SpeechResult.VocalEffortDetected -> Color(0xFFFF9800)
                isListening -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        // Feedback animation for positive results
        if (speechResult is SpeechResult.Success || speechResult is SpeechResult.VocalEffortDetected) {
            Spacer(modifier = Modifier.height(8.dp))
            PositiveFeedbackAnimation(speechResult = speechResult)
        }
    }
}

/**
 * Get appropriate status text based on current state
 */
private fun getStatusText(
    isListening: Boolean,
    speechResult: SpeechResult?,
    targetLetter: Char?,
    isEnabled: Boolean
): String {
    return when {
        !isEnabled -> "Microphone not available"
        isListening -> "Listening... 🎤"
        speechResult is SpeechResult.Success -> "Great job! 🎉"
        speechResult is SpeechResult.VocalEffortDetected -> "Good try! 👏"
        speechResult is SpeechResult.TryAgain -> "Try again! 😊"
        targetLetter != null -> "Say letter ${targetLetter.uppercaseChar()}"
        else -> "Tap to speak"
    }
}

/**
 * Positive feedback animation component
 * Phase 7: Immediate encouragement for toddler vocal efforts
 */
@Composable
private fun PositiveFeedbackAnimation(
    speechResult: SpeechResult,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "feedback_scale"
    )

    val emoji = when (speechResult) {
        is SpeechResult.Success -> "🎉"
        is SpeechResult.VocalEffortDetected -> "⭐"
        else -> "👏"
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .scale(animatedScale)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 20.sp
        )
    }
}

/**
 * Speech Recognition Status Indicator
 * Phase 7: Visual feedback for speech recognition states
 */
@Composable
fun SpeechStatusIndicator(
    isListening: Boolean,
    speechResult: SpeechResult?,
    modifier: Modifier = Modifier
) {
    val indicatorColor by animateColorAsState(
        targetValue = when {
            isListening -> MaterialTheme.colorScheme.primary
            speechResult is SpeechResult.Success -> Color(0xFF4CAF50)
            speechResult is SpeechResult.VocalEffortDetected -> Color(0xFFFF9800)
            speechResult is SpeechResult.TryAgain -> Color(0xFF2196F3)
            else -> MaterialTheme.colorScheme.outline
        },
        label = "indicator_color"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = indicatorColor,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when {
                isListening -> "Listening for speech..."
                speechResult is SpeechResult.Success -> "Speech recognized!"
                speechResult is SpeechResult.VocalEffortDetected -> "Great vocal effort!"
                speechResult is SpeechResult.TryAgain -> "Try speaking again"
                else -> "Ready to listen"
            },
            style = MaterialTheme.typography.bodySmall,
            color = indicatorColor
        )
    }
}

/**
 * Microphone Permission Prompt Component
 * Phase 7: Child and parent-friendly permission request
 */
@Composable
fun MicrophonePermissionPrompt(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Speaking Activities",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Let your child practice speaking and pronunciation with fun voice activities!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Not Now")
                }

                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Allow")
                }
            }
        }
    }
}
