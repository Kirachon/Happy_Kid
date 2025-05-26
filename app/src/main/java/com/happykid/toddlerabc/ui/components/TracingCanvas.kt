package com.happykid.toddlerabc.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.happykid.toddlerabc.tracing.LetterFormationGuide
import com.happykid.toddlerabc.tracing.TracingPoint
import com.happykid.toddlerabc.tracing.TracingTouchDetector
import com.happykid.toddlerabc.haptic.HapticFeedbackManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Tracing Canvas Component for Happy_Kid App
 * Phase 8: Interactive drawing surface with real-time feedback
 *
 * Provides smooth tracing experience with pressure sensitivity, visual guides,
 * and real-time feedback optimized for toddler interaction patterns.
 */
@Composable
fun TracingCanvas(
    letterGuide: LetterFormationGuide,
    touchDetector: TracingTouchDetector,
    hapticManager: HapticFeedbackManager,
    onTracingComplete: (Float) -> Unit, // Accuracy percentage
    modifier: Modifier = Modifier,
    showGuides: Boolean = true,
    difficultyLevel: Int = 1
) {
    val touchPath by touchDetector.touchPath.collectAsState()
    val isTracking by touchDetector.isTracking.collectAsState()
    val tracingMetrics by touchDetector.tracingMetrics.collectAsState()

    var canvasSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val density = LocalDensity.current

    // Colors for different feedback states
    val guideColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    val tracingColor = MaterialTheme.colorScheme.primary
    val successColor = Color(0xFF4CAF50)
    val errorColor = Color(0xFFFF5722)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Tracing metrics display
        TracingMetricsDisplay(
            metrics = tracingMetrics,
            isTracking = isTracking,
            modifier = Modifier.padding(16.dp)
        )

        // Main tracing canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                touchDetector.startTracking()
                                hapticManager.provideGuidanceFeedback()
                            },
                            onDragEnd = {
                                touchDetector.stopTracking()
                                val accuracy = calculateTracingAccuracy(touchPath, letterGuide)
                                onTracingComplete(accuracy)

                                if (accuracy >= 80f) {
                                    hapticManager.provideSuccessFeedback()
                                } else if (accuracy >= 60f) {
                                    hapticManager.provideEncouragementFeedback()
                                } else {
                                    hapticManager.provideErrorFeedback()
                                }
                            },
                            onDrag = { change, _ ->
                                touchDetector.processTouchInput(change)
                            }
                        )
                    }
            ) {
                canvasSize = size

                // Draw letter formation guides
                if (showGuides) {
                    drawLetterGuides(
                        letterGuide = letterGuide,
                        difficultyLevel = difficultyLevel,
                        guideColor = guideColor,
                        canvasSize = size
                    )
                }

                // Draw user's tracing path
                drawTracingPath(
                    touchPath = touchPath,
                    tracingColor = tracingColor,
                    canvasSize = size
                )

                // Draw real-time feedback
                drawRealTimeFeedback(
                    touchPath = touchPath,
                    letterGuide = letterGuide,
                    successColor = successColor,
                    errorColor = errorColor
                )
            }
        }

        // Tracing controls
        TracingControls(
            onClear = { touchDetector.clearTracing() },
            onUndo = { /* TODO: Implement undo */ },
            isTracking = isTracking,
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Draw letter formation guides based on difficulty level
 */
private fun DrawScope.drawLetterGuides(
    letterGuide: LetterFormationGuide,
    difficultyLevel: Int,
    guideColor: Color,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    val scaleFactor = minOf(
        canvasSize.width / letterGuide.boundingBox.width,
        canvasSize.height / letterGuide.boundingBox.height
    ) * 0.8f

    val offsetX = (canvasSize.width - letterGuide.boundingBox.width * scaleFactor) / 2f
    val offsetY = (canvasSize.height - letterGuide.boundingBox.height * scaleFactor) / 2f

    when (difficultyLevel) {
        1 -> drawBeginnerGuides(letterGuide, scaleFactor, offsetX, offsetY, guideColor)
        2 -> drawEasyGuides(letterGuide, scaleFactor, offsetX, offsetY, guideColor)
        3 -> drawMediumGuides(letterGuide, scaleFactor, offsetX, offsetY, guideColor)
        4 -> drawHardGuides(letterGuide, scaleFactor, offsetX, offsetY, guideColor)
        5 -> drawExpertGuides(letterGuide, scaleFactor, offsetX, offsetY, guideColor)
    }
}

/**
 * Draw beginner level guides (full guidance with dots)
 */
private fun DrawScope.drawBeginnerGuides(
    letterGuide: LetterFormationGuide,
    scaleFactor: Float,
    offsetX: Float,
    offsetY: Float,
    guideColor: Color
) {
    letterGuide.strokes.forEach { stroke ->
        val startX = stroke.startPoint.x * scaleFactor + offsetX
        val startY = stroke.startPoint.y * scaleFactor + offsetY
        val endX = stroke.endPoint.x * scaleFactor + offsetX
        val endY = stroke.endPoint.y * scaleFactor + offsetY

        // Draw dotted path
        drawPath(
            path = Path().apply {
                moveTo(startX, startY)
                lineTo(endX, endY)
            },
            color = guideColor,
            style = Stroke(
                width = 8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )

        // Draw start point
        drawCircle(
            color = Color.Green,
            radius = 12.dp.toPx(),
            center = Offset(startX, startY)
        )

        // Draw stroke number
        drawContext.canvas.nativeCanvas.drawText(
            stroke.strokeNumber.toString(),
            startX - 6.dp.toPx(),
            startY + 6.dp.toPx(),
            android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 24.sp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
            }
        )
    }
}

/**
 * Draw easy level guides (guided lines with arrows)
 */
private fun DrawScope.drawEasyGuides(
    letterGuide: LetterFormationGuide,
    scaleFactor: Float,
    offsetX: Float,
    offsetY: Float,
    guideColor: Color
) {
    letterGuide.strokes.forEach { stroke ->
        val startX = stroke.startPoint.x * scaleFactor + offsetX
        val startY = stroke.startPoint.y * scaleFactor + offsetY
        val endX = stroke.endPoint.x * scaleFactor + offsetX
        val endY = stroke.endPoint.y * scaleFactor + offsetY

        // Draw solid guide line
        drawLine(
            color = guideColor,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 4.dp.toPx()
        )

        // Draw direction arrow
        drawDirectionArrow(
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            color = guideColor.copy(alpha = 0.8f)
        )
    }
}

/**
 * Draw medium level guides (outline only)
 */
private fun DrawScope.drawMediumGuides(
    letterGuide: LetterFormationGuide,
    scaleFactor: Float,
    offsetX: Float,
    offsetY: Float,
    guideColor: Color
) {
    val path = letterGuide.getFormationPath()
    val scaledPath = Path().apply {
        addPath(path, Offset(offsetX, offsetY))
        transform(Matrix().apply { scale(scaleFactor, scaleFactor) })
    }

    drawPath(
        path = scaledPath,
        color = guideColor.copy(alpha = 0.5f),
        style = Stroke(width = 2.dp.toPx())
    )
}

/**
 * Draw hard level guides (faded outline)
 */
private fun DrawScope.drawHardGuides(
    letterGuide: LetterFormationGuide,
    scaleFactor: Float,
    offsetX: Float,
    offsetY: Float,
    guideColor: Color
) {
    val path = letterGuide.getFormationPath()
    val scaledPath = Path().apply {
        addPath(path, Offset(offsetX, offsetY))
        transform(Matrix().apply { scale(scaleFactor, scaleFactor) })
    }

    drawPath(
        path = scaledPath,
        color = guideColor.copy(alpha = 0.2f),
        style = Stroke(width = 1.dp.toPx())
    )
}

/**
 * Draw expert level guides (no guidance)
 */
private fun DrawScope.drawExpertGuides(
    letterGuide: LetterFormationGuide,
    scaleFactor: Float,
    offsetX: Float,
    offsetY: Float,
    guideColor: Color
) {
    // No guides for expert level
}

/**
 * Draw direction arrow for stroke guidance
 */
private fun DrawScope.drawDirectionArrow(
    start: Offset,
    end: Offset,
    color: Color
) {
    val arrowLength = 20.dp.toPx()
    val arrowAngle = 30f * kotlin.math.PI / 180f

    val dx = end.x - start.x
    val dy = end.y - start.y
    val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble())

    val arrowX1 = end.x - arrowLength * kotlin.math.cos(angle - arrowAngle).toFloat()
    val arrowY1 = end.y - arrowLength * kotlin.math.sin(angle - arrowAngle).toFloat()
    val arrowX2 = end.x - arrowLength * kotlin.math.cos(angle + arrowAngle).toFloat()
    val arrowY2 = end.y - arrowLength * kotlin.math.sin(angle + arrowAngle).toFloat()

    // Draw arrow lines
    drawLine(color = color, start = end, end = Offset(arrowX1, arrowY1), strokeWidth = 3.dp.toPx())
    drawLine(color = color, start = end, end = Offset(arrowX2, arrowY2), strokeWidth = 3.dp.toPx())
}

/**
 * Draw user's tracing path
 */
private fun DrawScope.drawTracingPath(
    touchPath: List<TracingPoint>,
    tracingColor: Color,
    canvasSize: androidx.compose.ui.geometry.Size
) {
    if (touchPath.size < 2) return

    val path = Path()
    touchPath.forEachIndexed { index, point ->
        if (index == 0) {
            path.moveTo(point.position.x, point.position.y)
        } else {
            path.lineTo(point.position.x, point.position.y)
        }
    }

    drawPath(
        path = path,
        color = tracingColor,
        style = Stroke(
            width = 6.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Draw real-time feedback indicators
 */
private fun DrawScope.drawRealTimeFeedback(
    touchPath: List<TracingPoint>,
    letterGuide: LetterFormationGuide,
    successColor: Color,
    errorColor: Color
) {
    // Draw accuracy indicators along the path
    touchPath.forEach { point ->
        val isAccurate = isPointAccurate(point, letterGuide)
        val indicatorColor = if (isAccurate) successColor else errorColor

        drawCircle(
            color = indicatorColor.copy(alpha = 0.3f),
            radius = 8.dp.toPx(),
            center = point.position
        )
    }
}

/**
 * Check if a point is accurate relative to the letter guide
 */
private fun isPointAccurate(point: TracingPoint, letterGuide: LetterFormationGuide): Boolean {
    // Simplified accuracy check - in a real implementation, this would be more sophisticated
    return true // Placeholder
}

/**
 * Calculate tracing accuracy percentage
 */
private fun calculateTracingAccuracy(
    touchPath: List<TracingPoint>,
    letterGuide: LetterFormationGuide
): Float {
    if (touchPath.isEmpty()) return 0f

    // Simplified accuracy calculation - in a real implementation, this would be more sophisticated
    val completionPercentage = minOf(touchPath.size / 50f, 1f) * 100f
    return completionPercentage.coerceIn(0f, 100f)
}

/**
 * Tracing metrics display component
 */
@Composable
private fun TracingMetricsDisplay(
    metrics: com.happykid.toddlerabc.tracing.TracingMetrics,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MetricItem("Points", metrics.totalPoints.toString())
            MetricItem("Distance", "${metrics.totalDistance.toInt()}px")
            MetricItem("Smoothness", "${metrics.smoothnessScore.toInt()}%")
            MetricItem("Status", if (isTracking) "Tracing..." else "Ready")
        }
    }
}

/**
 * Individual metric item
 */
@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
    }
}

/**
 * Tracing controls component
 */
@Composable
private fun TracingControls(
    onClear: () -> Unit,
    onUndo: () -> Unit,
    isTracking: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(
            onClick = onClear,
            enabled = !isTracking
        ) {
            Text("Clear")
        }

        OutlinedButton(
            onClick = onUndo,
            enabled = !isTracking
        ) {
            Text("Undo")
        }
    }
}
