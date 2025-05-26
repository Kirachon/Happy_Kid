package com.happykid.toddlerabc.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.happykid.toddlerabc.model.Letter
import com.happykid.toddlerabc.model.FontPreference
import com.happykid.toddlerabc.model.TracingProgress
import com.happykid.toddlerabc.model.PhonicsWord
import com.happykid.toddlerabc.model.PhonicsProgress
import com.happykid.toddlerabc.model.LearningAnalytics
import com.happykid.toddlerabc.model.Achievement
import com.happykid.toddlerabc.model.LearningPath
import com.happykid.toddlerabc.model.ParentalAuth
import com.happykid.toddlerabc.model.ParentalControls
import com.happykid.toddlerabc.model.AppSession
import com.happykid.toddlerabc.model.Story
import com.happykid.toddlerabc.model.VocabularyWord
import com.happykid.toddlerabc.model.Activity
import com.happykid.toddlerabc.model.ContentProgress
import com.happykid.toddlerabc.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Room database for Happy Kid app
 * Manages letter progress and learning data
 * Phase 5: Enhanced with font preferences
 * Phase 8: Enhanced with tracing progress tracking
 * Phase 9: Enhanced with phonics words and reading progress
 * Phase 10: Enhanced with learning analytics, achievements, and adaptive learning
 * Phase 11: Enhanced with parental authentication and controls
 * Phase 12: Enhanced with content expansion (stories, vocabulary, activities)
 */
@Database(
    entities = [
        Letter::class,
        FontPreference::class,
        TracingProgress::class,
        PhonicsWord::class,
        PhonicsProgress::class,
        LearningAnalytics::class,
        Achievement::class,
        LearningPath::class,
        ParentalAuth::class,
        ParentalControls::class,
        AppSession::class,
        Story::class,
        VocabularyWord::class,
        Activity::class,
        ContentProgress::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(PhonicsTypeConverters::class, AnalyticsTypeConverters::class, ContentTypeConverters::class)
abstract class HappyKidDatabase : RoomDatabase() {

    abstract fun letterDao(): LetterDao
    abstract fun fontPreferenceDao(): FontPreferenceDao
    abstract fun tracingProgressDao(): TracingProgressDao
    abstract fun phonicsDao(): PhonicsDao
    abstract fun phonicsProgressDao(): PhonicsProgressDao
    abstract fun learningAnalyticsDao(): LearningAnalyticsDao
    abstract fun parentalAuthDao(): ParentalAuthDao
    abstract fun storyDao(): StoryDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun activityDao(): ActivityDao
    abstract fun contentProgressDao(): ContentProgressDao

    companion object {
        @Volatile
        private var INSTANCE: HappyKidDatabase? = null

        /**
         * Get database instance with singleton pattern
         */
        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): HappyKidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HappyKidDatabase::class.java,
                    "happy_kid_database"
                )
                    .addCallback(HappyKidDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * Migration from version 1 to 2: Add font preferences table
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `font_preferences` (
                        `id` INTEGER NOT NULL,
                        `selectedFontFamily` TEXT NOT NULL,
                        `fontSize` REAL NOT NULL,
                        `lastUpdated` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                // Insert default font preference
                database.execSQL("""
                    INSERT OR REPLACE INTO `font_preferences`
                    (`id`, `selectedFontFamily`, `fontSize`, `lastUpdated`)
                    VALUES (1, 'default', 1.0, ${System.currentTimeMillis()})
                """.trimIndent())
            }
        }

        /**
         * Migration from version 2 to 3: Add tracing progress table
         * Phase 8: Pre-Writing and Tracing System database migration
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `tracing_progress` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `character` INTEGER NOT NULL,
                        `sessionTimestamp` INTEGER NOT NULL,
                        `sessionDurationMs` INTEGER NOT NULL,
                        `accuracyPercentage` REAL NOT NULL,
                        `completionPercentage` REAL NOT NULL,
                        `strokeAccuracy` REAL NOT NULL,
                        `totalStrokes` INTEGER NOT NULL,
                        `completedStrokes` INTEGER NOT NULL,
                        `correctStrokeOrder` INTEGER NOT NULL,
                        `averageStrokeSpeed` REAL NOT NULL,
                        `totalTouchPoints` INTEGER NOT NULL,
                        `accurateTouchPoints` INTEGER NOT NULL,
                        `averagePressure` REAL NOT NULL,
                        `smoothnessScore` REAL NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `guidanceUsed` INTEGER NOT NULL,
                        `hintsUsed` INTEGER NOT NULL,
                        `attemptsCount` INTEGER NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `isSuccessful` INTEGER NOT NULL,
                        `milestoneAchieved` TEXT,
                        `frameRate` REAL NOT NULL,
                        `touchLatencyMs` REAL NOT NULL,
                        `renderingPerformance` REAL NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 3 to 4: Add phonics words and progress tables
         * Phase 9: Phonics and Early Reading Engine database migration
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create phonics_words table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `phonics_words` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `word` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `phonemes` TEXT NOT NULL,
                        `syllables` TEXT NOT NULL,
                        `letterSounds` TEXT NOT NULL,
                        `isSightWord` INTEGER NOT NULL,
                        `isHighFrequency` INTEGER NOT NULL,
                        `ageAppropriate` TEXT NOT NULL,
                        `audioFileName` TEXT,
                        `pronunciationGuide` TEXT NOT NULL,
                        `imageFileName` TEXT,
                        `colorHex` TEXT NOT NULL,
                        `timesPresented` INTEGER NOT NULL,
                        `timesRecognized` INTEGER NOT NULL,
                        `lastPresentedTimestamp` INTEGER NOT NULL,
                        `blendingSteps` TEXT NOT NULL,
                        `rhymingWords` TEXT NOT NULL,
                        `wordFamily` TEXT
                    )
                """.trimIndent())

                // Create phonics_progress table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `phonics_progress` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `wordId` INTEGER NOT NULL,
                        `word` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `sessionTimestamp` INTEGER NOT NULL,
                        `sessionDurationMs` INTEGER NOT NULL,
                        `activityType` TEXT NOT NULL,
                        `recognitionAccuracy` REAL NOT NULL,
                        `recognitionTimeMs` INTEGER NOT NULL,
                        `attemptsRequired` INTEGER NOT NULL,
                        `blendingAccuracy` REAL NOT NULL,
                        `blendingStepsCompleted` INTEGER NOT NULL,
                        `blendingTimeMs` INTEGER NOT NULL,
                        `pronunciationAccuracy` REAL NOT NULL,
                        `pronunciationAttempts` INTEGER NOT NULL,
                        `speechRecognitionSuccess` INTEGER NOT NULL,
                        `comprehensionAccuracy` REAL NOT NULL,
                        `contextualUsage` INTEGER NOT NULL,
                        `isFirstAttempt` INTEGER NOT NULL,
                        `isImprovement` INTEGER NOT NULL,
                        `milestoneAchieved` TEXT,
                        `difficultyLevel` INTEGER NOT NULL,
                        `hintsUsed` INTEGER NOT NULL,
                        `assistanceRequired` INTEGER NOT NULL,
                        `engagementScore` REAL NOT NULL,
                        `sessionCompleted` INTEGER NOT NULL,
                        `voluntaryRepetition` INTEGER NOT NULL,
                        `isSuccessful` INTEGER NOT NULL,
                        `confidenceLevel` REAL NOT NULL,
                        `frustrationLevel` REAL NOT NULL,
                        `frameRate` REAL NOT NULL,
                        `touchLatencyMs` REAL NOT NULL,
                        `audioLatencyMs` REAL NOT NULL,
                        `renderingPerformance` REAL NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 4 to 5: Add learning analytics, achievements, and learning paths
         * Phase 10: Adaptive Learning and Progress Tracking database migration
         */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create learning_analytics table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_analytics` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `activityType` TEXT NOT NULL,
                        `sessionTimestamp` INTEGER NOT NULL,
                        `sessionDurationMs` INTEGER NOT NULL,
                        `accuracyPercentage` REAL NOT NULL,
                        `completionPercentage` REAL NOT NULL,
                        `attemptsCount` INTEGER NOT NULL,
                        `hintsUsed` INTEGER NOT NULL,
                        `errorsCount` INTEGER NOT NULL,
                        `contentId` TEXT NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `adaptiveDifficultyAdjustment` INTEGER NOT NULL,
                        `engagementScore` REAL NOT NULL,
                        `focusTimeMs` INTEGER NOT NULL,
                        `distractionEvents` INTEGER NOT NULL,
                        `masteryLevel` TEXT NOT NULL,
                        `improvementRate` REAL NOT NULL,
                        `retentionScore` REAL NOT NULL,
                        `recommendedNextActivity` TEXT,
                        `suggestedDifficultyLevel` INTEGER NOT NULL,
                        `learningPathProgress` REAL NOT NULL,
                        `timeOfDay` INTEGER NOT NULL,
                        `dayOfWeek` INTEGER NOT NULL,
                        `sessionSequenceNumber` INTEGER NOT NULL,
                        `frameRate` REAL NOT NULL,
                        `responseTimeMs` REAL NOT NULL,
                        `devicePerformanceScore` REAL NOT NULL
                    )
                """.trimIndent())

                // Create achievements table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `achievements` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `achievementId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `iconResource` TEXT NOT NULL,
                        `badgeColor` TEXT NOT NULL,
                        `pointsAwarded` INTEGER NOT NULL,
                        `difficultyTier` INTEGER NOT NULL,
                        `isUnlocked` INTEGER NOT NULL,
                        `unlockedTimestamp` INTEGER,
                        `progressCurrent` INTEGER NOT NULL,
                        `progressTarget` INTEGER NOT NULL,
                        `progressPercentage` REAL NOT NULL,
                        `requirementType` TEXT NOT NULL,
                        `requirementValue` TEXT NOT NULL,
                        `prerequisiteAchievements` TEXT,
                        `celebrationMessage` TEXT NOT NULL,
                        `celebrationAnimation` TEXT NOT NULL,
                        `shareableText` TEXT
                    )
                """.trimIndent())

                // Create learning_paths table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `learning_paths` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pathId` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `pathName` TEXT NOT NULL,
                        `pathDescription` TEXT NOT NULL,
                        `pathType` TEXT NOT NULL,
                        `currentStepIndex` INTEGER NOT NULL,
                        `totalSteps` INTEGER NOT NULL,
                        `progressPercentage` REAL NOT NULL,
                        `estimatedCompletionDays` INTEGER NOT NULL,
                        `isAdaptive` INTEGER NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `personalizedContent` TEXT,
                        `startedTimestamp` INTEGER,
                        `lastAccessedTimestamp` INTEGER,
                        `completedTimestamp` INTEGER,
                        `averageAccuracy` REAL NOT NULL,
                        `totalTimeSpentMs` INTEGER NOT NULL,
                        `masteredSteps` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Migration from version 5 to 6 (Phase 11: Parental Controls)
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create parental_auth table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `parental_auth` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `isAuthEnabled` INTEGER NOT NULL,
                        `authMethod` TEXT NOT NULL,
                        `pinHash` TEXT,
                        `biometricEnabled` INTEGER NOT NULL,
                        `sessionTimeoutMinutes` INTEGER NOT NULL,
                        `lastAuthTimestamp` INTEGER NOT NULL,
                        `failedAttempts` INTEGER NOT NULL,
                        `lockoutUntil` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create parental_controls table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `parental_controls` (
                        `id` INTEGER PRIMARY KEY NOT NULL,
                        `sessionTimeLimitMinutes` INTEGER NOT NULL,
                        `dailyTimeLimitMinutes` INTEGER NOT NULL,
                        `allowedTimeStart` TEXT NOT NULL,
                        `allowedTimeEnd` TEXT NOT NULL,
                        `weekendTimeLimitMinutes` INTEGER NOT NULL,
                        `contentFilterLevel` TEXT NOT NULL,
                        `allowSpeechRecognition` INTEGER NOT NULL,
                        `allowTracingActivities` INTEGER NOT NULL,
                        `allowPhonicsActivities` INTEGER NOT NULL,
                        `allowAlphabetActivities` INTEGER NOT NULL,
                        `requireParentalApprovalForNewFeatures` INTEGER NOT NULL,
                        `enableUsageReports` INTEGER NOT NULL,
                        `enableAchievementNotifications` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create app_sessions table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `app_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `startTime` INTEGER NOT NULL,
                        `endTime` INTEGER,
                        `durationMinutes` INTEGER NOT NULL,
                        `activitiesAccessed` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Initialize default values
                database.execSQL("""
                    INSERT OR IGNORE INTO `parental_auth`
                    (`id`, `isAuthEnabled`, `authMethod`, `biometricEnabled`, `sessionTimeoutMinutes`,
                     `lastAuthTimestamp`, `failedAttempts`, `lockoutUntil`, `createdAt`, `updatedAt`)
                    VALUES (1, 0, 'PIN', 0, 30, 0, 0, 0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent())

                database.execSQL("""
                    INSERT OR IGNORE INTO `parental_controls`
                    (`id`, `sessionTimeLimitMinutes`, `dailyTimeLimitMinutes`, `allowedTimeStart`, `allowedTimeEnd`,
                     `weekendTimeLimitMinutes`, `contentFilterLevel`, `allowSpeechRecognition`, `allowTracingActivities`,
                     `allowPhonicsActivities`, `allowAlphabetActivities`, `requireParentalApprovalForNewFeatures`,
                     `enableUsageReports`, `enableAchievementNotifications`, `createdAt`, `updatedAt`)
                    VALUES (1, 30, 60, '08:00', '20:00', 90, 'AGE_APPROPRIATE', 1, 1, 1, 1, 1, 1, 1,
                            ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
                """.trimIndent())
            }
        }

        /**
         * Migration from version 6 to 7: Add content expansion tables
         * Phase 12: Content Expansion - Stories, vocabulary, activities, and content progress
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create stories table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stories` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `targetLetters` TEXT NOT NULL,
                        `phonicsWords` TEXT NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `estimatedReadingTime` INTEGER NOT NULL,
                        `ageRange` TEXT NOT NULL,
                        `educationalObjectives` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `hasPrerequisites` INTEGER NOT NULL,
                        `prerequisites` TEXT NOT NULL,
                        `unlockCriteria` TEXT,
                        `audioPath` TEXT,
                        `illustrations` TEXT NOT NULL,
                        `interactiveElements` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create vocabulary_words table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `vocabulary_words` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `word` TEXT NOT NULL,
                        `definition` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `ageRange` TEXT NOT NULL,
                        `imagePath` TEXT NOT NULL,
                        `audioPath` TEXT,
                        `phoneticSpelling` TEXT,
                        `relatedWords` TEXT NOT NULL,
                        `exampleSentence` TEXT,
                        `associatedLetters` TEXT NOT NULL,
                        `frequencyScore` INTEGER NOT NULL,
                        `activities` TEXT NOT NULL,
                        `unlockCriteria` TEXT,
                        `isCoreVocabulary` INTEGER NOT NULL,
                        `repetitionInterval` INTEGER NOT NULL,
                        `nextReviewDate` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create activities table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `activities` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `difficultyLevel` INTEGER NOT NULL,
                        `ageRange` TEXT NOT NULL,
                        `estimatedTime` INTEGER NOT NULL,
                        `educationalObjectives` TEXT NOT NULL,
                        `targetSkills` TEXT NOT NULL,
                        `configuration` TEXT NOT NULL,
                        `prerequisites` TEXT NOT NULL,
                        `unlockCriteria` TEXT,
                        `completionPoints` INTEGER NOT NULL,
                        `achievementId` TEXT,
                        `supportsAdaptiveDifficulty` INTEGER NOT NULL,
                        `minimumAccuracy` REAL NOT NULL,
                        `maxAttempts` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `isActive` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create content_progress table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `content_progress` (
                        `id` TEXT PRIMARY KEY NOT NULL,
                        `contentId` TEXT NOT NULL,
                        `contentType` TEXT NOT NULL,
                        `userId` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `completionPercentage` REAL NOT NULL,
                        `attemptCount` INTEGER NOT NULL,
                        `successCount` INTEGER NOT NULL,
                        `bestAccuracy` REAL NOT NULL,
                        `averageAccuracy` REAL NOT NULL,
                        `totalTimeSpent` INTEGER NOT NULL,
                        `averageTimePerAttempt` INTEGER NOT NULL,
                        `lastInteractionAt` INTEGER NOT NULL,
                        `firstStartedAt` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        `masteredAt` INTEGER,
                        `completionDifficulty` INTEGER,
                        `hintsUsed` INTEGER NOT NULL,
                        `mistakeCount` INTEGER NOT NULL,
                        `learningStreak` INTEGER NOT NULL,
                        `engagementScore` REAL NOT NULL,
                        `retentionScore` REAL NOT NULL,
                        `nextReviewDate` INTEGER,
                        `repetitionInterval` INTEGER NOT NULL,
                        `metadata` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        /**
         * Database callback to populate initial data
         */
        private class HappyKidDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateDatabase(
                            database.letterDao(),
                            database.phonicsDao(),
                            database.storyDao(),
                            database.vocabularyDao(),
                            database.activityDao()
                        )
                    }
                }
            }

            /**
             * Populate database with initial alphabet, phonics, and content expansion data
             * Phase 9: Enhanced with phonics words initialization
             * Phase 12: Enhanced with stories, vocabulary, and activities initialization
             */
            private suspend fun populateDatabase(
                letterDao: LetterDao,
                phonicsDao: PhonicsDao,
                storyDao: StoryDao,
                vocabularyDao: VocabularyDao,
                activityDao: ActivityDao
            ) {
                // Clear any existing data
                letterDao.deleteAllLetters()
                phonicsDao.deleteAllPhonicsWords()
                storyDao.deleteAllStories()
                vocabularyDao.deleteAllVocabularyWords()
                activityDao.deleteAllActivities()

                // Insert all 26 letters with default values
                val letters = Letter.getAllLetters()
                letterDao.insertLetters(letters)

                // Insert default phonics words (CVC words and sight words)
                val cvcWords = PhonicsWord.getDefaultCVCWords()
                val sightWords = PhonicsWord.getDefaultSightWords()
                phonicsDao.insertPhonicsWords(cvcWords + sightWords)

                // Insert default stories
                val stories = Story.getDefaultStories()
                storyDao.insertStories(stories)

                // Insert default vocabulary words
                val vocabularyWords = VocabularyWord.getDefaultVocabularyWords()
                vocabularyDao.insertVocabularyWords(vocabularyWords)

                // Insert default activities
                val activities = Activity.getDefaultActivities()
                activityDao.insertActivities(activities)
            }
        }
    }
}
