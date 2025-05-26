package com.happykid.toddlerabc.tracing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * Letter Formation Guide System for Happy_Kid App
 * Phase 8: Comprehensive letter formation guidance with stroke order and direction
 *
 * Provides detailed stroke information, visual guides, and formation templates
 * for all 26 letters with toddler-friendly progression and Windows emulator optimization.
 */

/**
 * Represents a single stroke in letter formation
 */
data class LetterStroke(
    val strokeNumber: Int,
    val startPoint: Offset,
    val endPoint: Offset,
    val controlPoints: List<Offset> = emptyList(), // For curved strokes
    val direction: StrokeDirection,
    val strokeType: StrokeType,
    val description: String,
    val isRequired: Boolean = true
)

/**
 * Direction of stroke movement
 */
enum class StrokeDirection {
    DOWN,           // Top to bottom
    UP,             // Bottom to top
    RIGHT,          // Left to right
    LEFT,           // Right to left
    CLOCKWISE,      // Circular clockwise
    COUNTERCLOCKWISE, // Circular counterclockwise
    DIAGONAL_DOWN_RIGHT,
    DIAGONAL_DOWN_LEFT,
    DIAGONAL_UP_RIGHT,
    DIAGONAL_UP_LEFT
}

/**
 * Type of stroke
 */
enum class StrokeType {
    STRAIGHT,       // Straight line
    CURVED,         // Curved line
    CIRCLE,         // Complete circle
    ARC,            // Partial circle
    DOT             // Single point
}

/**
 * Complete letter formation guide
 */
data class LetterFormationGuide(
    val character: Char,
    val strokes: List<LetterStroke>,
    val boundingBox: LetterBounds,
    val difficulty: Int, // 1-5 scale
    val estimatedTimeSeconds: Int,
    val tips: List<String>,
    val commonMistakes: List<String>
) {
    /**
     * Get stroke by number
     */
    fun getStroke(strokeNumber: Int): LetterStroke? {
        return strokes.find { it.strokeNumber == strokeNumber }
    }
    
    /**
     * Get total number of strokes
     */
    fun getTotalStrokes(): Int = strokes.size
    
    /**
     * Check if letter has curves
     */
    fun hasCurvedStrokes(): Boolean = strokes.any { it.strokeType in listOf(StrokeType.CURVED, StrokeType.CIRCLE, StrokeType.ARC) }
    
    /**
     * Get formation path for rendering
     */
    fun getFormationPath(): Path {
        val path = Path()
        strokes.forEach { stroke ->
            when (stroke.strokeType) {
                StrokeType.STRAIGHT -> {
                    path.moveTo(stroke.startPoint.x, stroke.startPoint.y)
                    path.lineTo(stroke.endPoint.x, stroke.endPoint.y)
                }
                StrokeType.CURVED -> {
                    path.moveTo(stroke.startPoint.x, stroke.startPoint.y)
                    if (stroke.controlPoints.isNotEmpty()) {
                        path.quadraticBezierTo(
                            stroke.controlPoints[0].x, stroke.controlPoints[0].y,
                            stroke.endPoint.x, stroke.endPoint.y
                        )
                    }
                }
                StrokeType.CIRCLE, StrokeType.ARC -> {
                    // Simplified circle/arc rendering
                    path.addOval(androidx.compose.ui.geometry.Rect(
                        stroke.startPoint.x - 20f,
                        stroke.startPoint.y - 20f,
                        stroke.startPoint.x + 20f,
                        stroke.startPoint.y + 20f
                    ))
                }
                StrokeType.DOT -> {
                    path.addOval(androidx.compose.ui.geometry.Rect(
                        stroke.startPoint.x - 5f,
                        stroke.startPoint.y - 5f,
                        stroke.startPoint.x + 5f,
                        stroke.startPoint.y + 5f
                    ))
                }
            }
        }
        return path
    }
}

/**
 * Letter bounding box for consistent sizing
 */
data class LetterBounds(
    val width: Float,
    val height: Float,
    val baselineOffset: Float, // Distance from bottom to baseline
    val capHeight: Float       // Height of capital letters
)

/**
 * Letter Formation Guide Factory
 * Creates formation guides for all 26 letters
 */
object LetterFormationGuideFactory {
    
    // Standard letter dimensions for consistent sizing
    private val STANDARD_BOUNDS = LetterBounds(
        width = 200f,
        height = 300f,
        baselineOffset = 50f,
        capHeight = 200f
    )
    
    /**
     * Get formation guide for a specific letter
     */
    fun getGuideForLetter(character: Char): LetterFormationGuide {
        return when (character.uppercaseChar()) {
            'A' -> createLetterA()
            'B' -> createLetterB()
            'C' -> createLetterC()
            'D' -> createLetterD()
            'E' -> createLetterE()
            'F' -> createLetterF()
            'G' -> createLetterG()
            'H' -> createLetterH()
            'I' -> createLetterI()
            'J' -> createLetterJ()
            'K' -> createLetterK()
            'L' -> createLetterL()
            'M' -> createLetterM()
            'N' -> createLetterN()
            'O' -> createLetterO()
            'P' -> createLetterP()
            'Q' -> createLetterQ()
            'R' -> createLetterR()
            'S' -> createLetterS()
            'T' -> createLetterT()
            'U' -> createLetterU()
            'V' -> createLetterV()
            'W' -> createLetterW()
            'X' -> createLetterX()
            'Y' -> createLetterY()
            'Z' -> createLetterZ()
            else -> createDefaultLetter(character)
        }
    }
    
    /**
     * Get all letter guides
     */
    fun getAllGuides(): List<LetterFormationGuide> {
        return ('A'..'Z').map { getGuideForLetter(it) }
    }
    
    /**
     * Get guides by difficulty level
     */
    fun getGuidesByDifficulty(difficulty: Int): List<LetterFormationGuide> {
        return getAllGuides().filter { it.difficulty == difficulty }
    }
    
    // Letter creation functions (showing a few examples)
    
    private fun createLetterA(): LetterFormationGuide {
        val strokes = listOf(
            LetterStroke(
                strokeNumber = 1,
                startPoint = Offset(100f, 250f),
                endPoint = Offset(50f, 50f),
                direction = StrokeDirection.DIAGONAL_UP_LEFT,
                strokeType = StrokeType.STRAIGHT,
                description = "Draw diagonal line up and left"
            ),
            LetterStroke(
                strokeNumber = 2,
                startPoint = Offset(100f, 250f),
                endPoint = Offset(150f, 50f),
                direction = StrokeDirection.DIAGONAL_UP_RIGHT,
                strokeType = StrokeType.STRAIGHT,
                description = "Draw diagonal line up and right"
            ),
            LetterStroke(
                strokeNumber = 3,
                startPoint = Offset(75f, 150f),
                endPoint = Offset(125f, 150f),
                direction = StrokeDirection.RIGHT,
                strokeType = StrokeType.STRAIGHT,
                description = "Draw horizontal line across the middle"
            )
        )
        
        return LetterFormationGuide(
            character = 'A',
            strokes = strokes,
            boundingBox = STANDARD_BOUNDS,
            difficulty = 3,
            estimatedTimeSeconds = 15,
            tips = listOf(
                "Start at the bottom center",
                "Make the triangle shape first",
                "Add the crossbar in the middle"
            ),
            commonMistakes = listOf(
                "Crossbar too high or too low",
                "Sides not straight enough",
                "Triangle not centered"
            )
        )
    }
    
    private fun createLetterO(): LetterFormationGuide {
        val strokes = listOf(
            LetterStroke(
                strokeNumber = 1,
                startPoint = Offset(100f, 50f),
                endPoint = Offset(100f, 50f),
                controlPoints = listOf(
                    Offset(150f, 75f),
                    Offset(150f, 175f),
                    Offset(100f, 200f),
                    Offset(50f, 175f),
                    Offset(50f, 75f)
                ),
                direction = StrokeDirection.COUNTERCLOCKWISE,
                strokeType = StrokeType.CIRCLE,
                description = "Draw a circle counterclockwise starting at the top"
            )
        )
        
        return LetterFormationGuide(
            character = 'O',
            strokes = strokes,
            boundingBox = STANDARD_BOUNDS,
            difficulty = 2,
            estimatedTimeSeconds = 8,
            tips = listOf(
                "Start at the top",
                "Go counterclockwise",
                "Make it round, not oval"
            ),
            commonMistakes = listOf(
                "Starting at wrong position",
                "Going clockwise instead",
                "Making it too oval"
            )
        )
    }
    
    private fun createLetterI(): LetterFormationGuide {
        val strokes = listOf(
            LetterStroke(
                strokeNumber = 1,
                startPoint = Offset(100f, 50f),
                endPoint = Offset(100f, 200f),
                direction = StrokeDirection.DOWN,
                strokeType = StrokeType.STRAIGHT,
                description = "Draw straight line down"
            ),
            LetterStroke(
                strokeNumber = 2,
                startPoint = Offset(100f, 220f),
                endPoint = Offset(100f, 230f),
                direction = StrokeDirection.DOWN,
                strokeType = StrokeType.DOT,
                description = "Add dot below the line"
            )
        )
        
        return LetterFormationGuide(
            character = 'I',
            strokes = strokes,
            boundingBox = STANDARD_BOUNDS,
            difficulty = 1,
            estimatedTimeSeconds = 5,
            tips = listOf(
                "Draw straight down",
                "Add the dot at the bottom"
            ),
            commonMistakes = listOf(
                "Line not straight",
                "Forgetting the dot"
            )
        )
    }
    
    // Placeholder implementations for other letters
    private fun createLetterB() = createDefaultLetter('B')
    private fun createLetterC() = createDefaultLetter('C')
    private fun createLetterD() = createDefaultLetter('D')
    private fun createLetterE() = createDefaultLetter('E')
    private fun createLetterF() = createDefaultLetter('F')
    private fun createLetterG() = createDefaultLetter('G')
    private fun createLetterH() = createDefaultLetter('H')
    private fun createLetterJ() = createDefaultLetter('J')
    private fun createLetterK() = createDefaultLetter('K')
    private fun createLetterL() = createDefaultLetter('L')
    private fun createLetterM() = createDefaultLetter('M')
    private fun createLetterN() = createDefaultLetter('N')
    private fun createLetterP() = createDefaultLetter('P')
    private fun createLetterQ() = createDefaultLetter('Q')
    private fun createLetterR() = createDefaultLetter('R')
    private fun createLetterS() = createDefaultLetter('S')
    private fun createLetterT() = createDefaultLetter('T')
    private fun createLetterU() = createDefaultLetter('U')
    private fun createLetterV() = createDefaultLetter('V')
    private fun createLetterW() = createDefaultLetter('W')
    private fun createLetterX() = createDefaultLetter('X')
    private fun createLetterY() = createDefaultLetter('Y')
    private fun createLetterZ() = createDefaultLetter('Z')
    
    private fun createDefaultLetter(character: Char): LetterFormationGuide {
        val strokes = listOf(
            LetterStroke(
                strokeNumber = 1,
                startPoint = Offset(100f, 50f),
                endPoint = Offset(100f, 200f),
                direction = StrokeDirection.DOWN,
                strokeType = StrokeType.STRAIGHT,
                description = "Basic stroke for letter ${character.uppercaseChar()}"
            )
        )
        
        return LetterFormationGuide(
            character = character,
            strokes = strokes,
            boundingBox = STANDARD_BOUNDS,
            difficulty = 2,
            estimatedTimeSeconds = 10,
            tips = listOf("Practice this letter step by step"),
            commonMistakes = listOf("Take your time with each stroke")
        )
    }
}
