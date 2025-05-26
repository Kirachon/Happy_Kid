package com.happykid.toddlerabc.data

import androidx.room.*
import com.happykid.toddlerabc.model.FontPreference
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for font preferences
 * Phase 5: Custom fonts integration with Room database
 */
@Dao
interface FontPreferenceDao {

    /**
     * Get current font preference as Flow for reactive updates
     */
    @Query("SELECT * FROM font_preferences WHERE id = 1")
    fun getFontPreferenceFlow(): Flow<FontPreference?>

    /**
     * Get current font preference (suspend function)
     */
    @Query("SELECT * FROM font_preferences WHERE id = 1")
    suspend fun getFontPreference(): FontPreference?

    /**
     * Insert or update font preference
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFontPreference(fontPreference: FontPreference)

    /**
     * Update font family
     */
    @Query("UPDATE font_preferences SET selectedFontFamily = :fontFamily, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateFontFamily(fontFamily: String, timestamp: Long = System.currentTimeMillis())

    /**
     * Update font size scale
     */
    @Query("UPDATE font_preferences SET fontSize = :fontSize, lastUpdated = :timestamp WHERE id = 1")
    suspend fun updateFontSize(fontSize: Float, timestamp: Long = System.currentTimeMillis())

    /**
     * Reset to default font preferences
     */
    @Query("DELETE FROM font_preferences WHERE id = 1")
    suspend fun resetFontPreferences()

    /**
     * Initialize default font preference if not exists
     */
    suspend fun initializeDefaultIfNotExists() {
        val existing = getFontPreference()
        if (existing == null) {
            insertFontPreference(FontPreference())
        }
    }
}
