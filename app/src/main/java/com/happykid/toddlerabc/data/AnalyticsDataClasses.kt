package com.happykid.toddlerabc.data

import com.happykid.toddlerabc.model.MasteryLevel

/**
 * Additional data classes for analytics functionality
 * Phase 10: Adaptive Learning and Progress Tracking
 *
 * Contains supporting data classes that are referenced by analytics components
 * but not defined in the main model files.
 */

/**
 * Difficulty recommendation for adaptive learning
 */
data class DifficultyRecommendation(
    val currentLevel: Int,
    val recommendedLevel: Int,
    val reason: String,
    val confidence: Float
)
