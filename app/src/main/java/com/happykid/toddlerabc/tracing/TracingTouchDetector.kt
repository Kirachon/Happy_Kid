package com.happykid.toddlerabc.tracing

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import com.happykid.toddlerabc.util.WindowsDeviceUtils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

/**
 * Tracing Touch Detector for Happy_Kid App
 * Phase 8: Advanced touch tracking and gesture recognition for letter tracing
 *
 * Provides precise touch tracking optimized for toddler finger movements with
 * Windows emulator compatibility, pressure sensitivity, and noise reduction.
 */
@Singleton
class TracingTouchDetector @Inject constructor() {

    companion object {
        private const val TAG = "TracingTouchDetector"
        private const val TOUCH_TOLERANCE = 15f // Pixels
        private const val PRESSURE_THRESHOLD = 0.3f
        private const val VELOCITY_SMOOTHING_FACTOR = 0.7f
        private const val MAX_TOUCH_POINTS = 1000 // Prevent memory issues
        private const val NOISE_REDUCTION_THRESHOLD = 5f // Pixels
    }

    private val isWindowsEmulator = WindowsDeviceUtils.isWindowsEmulator()

    // Touch tracking state
    private val _touchPath = MutableStateFlow<List<TracingPoint>>(emptyList())
    val touchPath: StateFlow<List<TracingPoint>> = _touchPath.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _currentStroke = MutableStateFlow<TracingStroke?>(null)
    val currentStroke: StateFlow<TracingStroke?> = _currentStroke.asStateFlow()

    private val _tracingMetrics = MutableStateFlow(TracingMetrics())
    val tracingMetrics: StateFlow<TracingMetrics> = _tracingMetrics.asStateFlow()

    // Internal tracking variables
    private var lastTouchPoint: TracingPoint? = null
    private var strokeStartTime = 0L
    private var totalDistance = 0f
    private var smoothedVelocity = 0f

    /**
     * Start tracking touch input for tracing
     */
    fun startTracking() {
        _isTracking.value = true
        _touchPath.value = emptyList()
        _currentStroke.value = null
        resetMetrics()
        strokeStartTime = System.currentTimeMillis()

        if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
            Log.d(TAG, "Started tracing touch tracking for Windows emulator")
        }
    }

    /**
     * Stop tracking touch input
     */
    fun stopTracking() {
        _isTracking.value = false
        finalizeCurrentStroke()

        if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
            Log.d(TAG, "Stopped tracing touch tracking")
        }
    }

    /**
     * Process touch input change
     */
    fun processTouchInput(change: PointerInputChange): TracingPoint? {
        if (!_isTracking.value) return null

        val currentTime = System.currentTimeMillis()
        val position = change.position
        val pressure = change.pressure

        // Apply Windows emulator touch scaling if needed
        val adjustedPosition = if (isWindowsEmulator) {
            Offset(
                x = position.x * 1.0f, // No scaling needed for tracing precision
                y = position.y * 1.0f
            )
        } else {
            position
        }

        // Create tracing point
        val tracingPoint = TracingPoint(
            position = adjustedPosition,
            pressure = pressure,
            timestamp = currentTime,
            velocity = calculateVelocity(adjustedPosition, currentTime)
        )

        // Apply noise reduction
        val filteredPoint = applyNoiseReduction(tracingPoint)

        // Add to path if it passes quality checks
        if (isValidTouchPoint(filteredPoint)) {
            addTouchPoint(filteredPoint)
            updateMetrics(filteredPoint)

            // Touch latency tracking for Windows emulator optimization
            if (isWindowsEmulator && WindowsDeviceUtils.isWindowsDebugMode()) {
                Log.v(TAG, "Touch latency: ${currentTime - strokeStartTime}ms")
            }

            return filteredPoint
        }

        return null
    }

    /**
     * Calculate velocity between touch points
     */
    private fun calculateVelocity(currentPosition: Offset, currentTime: Long): Float {
        val lastPoint = lastTouchPoint
        if (lastPoint == null) return 0f

        val deltaTime = (currentTime - lastPoint.timestamp).toFloat()
        if (deltaTime <= 0) return smoothedVelocity

        val distance = sqrt(
            (currentPosition.x - lastPoint.position.x).pow(2) +
            (currentPosition.y - lastPoint.position.y).pow(2)
        )

        val instantVelocity = distance / (deltaTime / 1000f) // pixels per second

        // Apply smoothing to reduce noise
        smoothedVelocity = (VELOCITY_SMOOTHING_FACTOR * smoothedVelocity) +
                          ((1 - VELOCITY_SMOOTHING_FACTOR) * instantVelocity)

        return smoothedVelocity
    }

    /**
     * Apply noise reduction to touch input
     */
    private fun applyNoiseReduction(point: TracingPoint): TracingPoint {
        val lastPoint = lastTouchPoint ?: return point

        val distance = sqrt(
            (point.position.x - lastPoint.position.x).pow(2) +
            (point.position.y - lastPoint.position.y).pow(2)
        )

        // If movement is too small, it might be noise
        return if (distance < NOISE_REDUCTION_THRESHOLD) {
            // Use interpolated position to smooth movement
            val interpolatedX = (point.position.x + lastPoint.position.x) / 2f
            val interpolatedY = (point.position.y + lastPoint.position.y) / 2f

            point.copy(position = Offset(interpolatedX, interpolatedY))
        } else {
            point
        }
    }

    /**
     * Check if touch point is valid for tracing
     */
    private fun isValidTouchPoint(point: TracingPoint): Boolean {
        // Check pressure threshold
        if (point.pressure < PRESSURE_THRESHOLD) return false

        // Check if too close to last point (avoid duplicate points)
        val lastPoint = lastTouchPoint
        if (lastPoint != null) {
            val distance = sqrt(
                (point.position.x - lastPoint.position.x).pow(2) +
                (point.position.y - lastPoint.position.y).pow(2)
            )
            if (distance < TOUCH_TOLERANCE) return false
        }

        return true
    }

    /**
     * Add touch point to current path
     */
    private fun addTouchPoint(point: TracingPoint) {
        val currentPath = _touchPath.value.toMutableList()

        // Prevent memory issues with too many points
        if (currentPath.size >= MAX_TOUCH_POINTS) {
            currentPath.removeAt(0)
        }

        currentPath.add(point)
        _touchPath.value = currentPath
        lastTouchPoint = point
    }

    /**
     * Update tracing metrics
     */
    private fun updateMetrics(point: TracingPoint) {
        val currentMetrics = _tracingMetrics.value
        val lastPoint = lastTouchPoint

        if (lastPoint != null) {
            val distance = sqrt(
                (point.position.x - lastPoint.position.x).pow(2) +
                (point.position.y - lastPoint.position.y).pow(2)
            )
            totalDistance += distance
        }

        val updatedMetrics = currentMetrics.copy(
            totalPoints = _touchPath.value.size,
            totalDistance = totalDistance,
            averagePressure = calculateAveragePressure(),
            averageVelocity = smoothedVelocity,
            smoothnessScore = calculateSmoothnessScore()
        )

        _tracingMetrics.value = updatedMetrics
    }

    /**
     * Calculate average pressure across all points
     */
    private fun calculateAveragePressure(): Float {
        val points = _touchPath.value
        if (points.isEmpty()) return 0f

        return points.map { it.pressure }.average().toFloat()
    }

    /**
     * Calculate smoothness score (0-100)
     */
    private fun calculateSmoothnessScore(): Float {
        val points = _touchPath.value
        if (points.size < 3) return 100f

        var totalVariation = 0f
        var count = 0

        for (i in 1 until points.size - 1) {
            val prev = points[i - 1]
            val current = points[i]
            val next = points[i + 1]

            // Calculate direction change
            val angle1 = atan2(
                current.position.y - prev.position.y,
                current.position.x - prev.position.x
            )
            val angle2 = atan2(
                next.position.y - current.position.y,
                next.position.x - current.position.x
            )

            var angleDiff = abs(angle2 - angle1)
            if (angleDiff > PI) angleDiff = 2 * PI.toFloat() - angleDiff

            totalVariation += angleDiff.toFloat()
            count++
        }

        val averageVariation = if (count > 0) totalVariation / count else 0f
        return (100f - (averageVariation * 100f / PI.toFloat())).coerceIn(0f, 100f)
    }

    /**
     * Finalize current stroke
     */
    private fun finalizeCurrentStroke() {
        val points = _touchPath.value
        if (points.isEmpty()) return

        val stroke = TracingStroke(
            points = points,
            startTime = strokeStartTime,
            endTime = System.currentTimeMillis(),
            totalDistance = totalDistance,
            averagePressure = calculateAveragePressure(),
            smoothnessScore = calculateSmoothnessScore()
        )

        _currentStroke.value = stroke
    }

    /**
     * Reset tracking metrics
     */
    private fun resetMetrics() {
        _tracingMetrics.value = TracingMetrics()
        lastTouchPoint = null
        totalDistance = 0f
        smoothedVelocity = 0f
    }

    /**
     * Clear current tracing data
     */
    fun clearTracing() {
        _touchPath.value = emptyList()
        _currentStroke.value = null
        resetMetrics()
    }

    /**
     * Get current path as simplified points for rendering
     */
    fun getSimplifiedPath(tolerance: Float = 5f): List<Offset> {
        return simplifyPath(_touchPath.value.map { it.position }, tolerance)
    }

    /**
     * Simplify path using Douglas-Peucker algorithm
     */
    private fun simplifyPath(points: List<Offset>, tolerance: Float): List<Offset> {
        if (points.size <= 2) return points

        return douglasPeucker(points, tolerance)
    }

    /**
     * Douglas-Peucker path simplification algorithm
     */
    private fun douglasPeucker(points: List<Offset>, tolerance: Float): List<Offset> {
        if (points.size <= 2) return points

        var maxDistance = 0f
        var maxIndex = 0

        val start = points.first()
        val end = points.last()

        for (i in 1 until points.size - 1) {
            val distance = perpendicularDistance(points[i], start, end)
            if (distance > maxDistance) {
                maxDistance = distance
                maxIndex = i
            }
        }

        return if (maxDistance > tolerance) {
            val left = douglasPeucker(points.subList(0, maxIndex + 1), tolerance)
            val right = douglasPeucker(points.subList(maxIndex, points.size), tolerance)
            left.dropLast(1) + right
        } else {
            listOf(start, end)
        }
    }

    /**
     * Calculate perpendicular distance from point to line
     */
    private fun perpendicularDistance(point: Offset, lineStart: Offset, lineEnd: Offset): Float {
        val dx = lineEnd.x - lineStart.x
        val dy = lineEnd.y - lineStart.y

        if (dx == 0f && dy == 0f) {
            return sqrt((point.x - lineStart.x).pow(2) + (point.y - lineStart.y).pow(2))
        }

        val t = ((point.x - lineStart.x) * dx + (point.y - lineStart.y) * dy) / (dx * dx + dy * dy)
        val clampedT = t.coerceIn(0f, 1f)

        val projectionX = lineStart.x + clampedT * dx
        val projectionY = lineStart.y + clampedT * dy

        return sqrt((point.x - projectionX).pow(2) + (point.y - projectionY).pow(2))
    }
}

/**
 * Represents a single touch point in tracing
 */
data class TracingPoint(
    val position: Offset,
    val pressure: Float,
    val timestamp: Long,
    val velocity: Float
)

/**
 * Represents a complete tracing stroke
 */
data class TracingStroke(
    val points: List<TracingPoint>,
    val startTime: Long,
    val endTime: Long,
    val totalDistance: Float,
    val averagePressure: Float,
    val smoothnessScore: Float
) {
    val duration: Long get() = endTime - startTime
    val averageVelocity: Float get() = if (duration > 0) totalDistance / (duration / 1000f) else 0f
}

/**
 * Real-time tracing metrics
 */
data class TracingMetrics(
    val totalPoints: Int = 0,
    val totalDistance: Float = 0f,
    val averagePressure: Float = 0f,
    val averageVelocity: Float = 0f,
    val smoothnessScore: Float = 100f
)
