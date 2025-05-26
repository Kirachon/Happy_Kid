package com.happykid.toddlerabc.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.happykid.toddlerabc.ui.screens.AlphabetScreen
import com.happykid.toddlerabc.ui.screens.HomeScreen
import com.happykid.toddlerabc.ui.screens.SettingsScreen
import com.happykid.toddlerabc.ui.screens.TracingScreen
import com.happykid.toddlerabc.ui.screens.PhonicsScreen
import com.happykid.toddlerabc.ui.screens.StoryScreen
import com.happykid.toddlerabc.ui.screens.VocabularyScreen
import com.happykid.toddlerabc.ui.analytics.AnalyticsScreen
import com.happykid.toddlerabc.ui.auth.ParentalAuthScreen
import com.happykid.toddlerabc.ui.dashboard.ParentDashboardScreen

/**
 * Navigation routes for Happy Kid app
 * Phase 3: Defines all navigation destinations
 * Phase 7: Enhanced with speech recognition route
 * Phase 8: Enhanced with tracing route
 * Phase 9: Enhanced with phonics route
 * Phase 10: Enhanced with analytics route
 * Phase 11b: Enhanced with parent dashboard and authentication routes
 */
object HappyKidDestinations {
    const val HOME_ROUTE = "home"
    const val ALPHABET_ROUTE = "alphabet"
    const val SETTINGS_ROUTE = "settings"
    const val SPEECH_ROUTE = "speech"
    const val TRACING_ROUTE = "tracing"
    const val PHONICS_ROUTE = "phonics"
    const val STORY_ROUTE = "story"
    const val VOCABULARY_ROUTE = "vocabulary"
    const val ANALYTICS_ROUTE = "analytics"
    const val PARENT_AUTH_ROUTE = "parent_auth"
    const val PARENT_DASHBOARD_ROUTE = "parent_dashboard"
}

/**
 * Navigation graph for Happy Kid app
 * Phase 3: Implements Navigation Component with Hilt integration
 *
 * Maintains all existing functionality from Phase 1 (Room) and Phase 2 (Hilt)
 * while adding proper navigation structure for enhanced user experience.
 */
@Composable
fun HappyKidNavGraph(
    navController: NavHostController,
    startDestination: String = HappyKidDestinations.HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(HappyKidDestinations.HOME_ROUTE) {
            HomeScreen(
                onNavigateToAlphabet = {
                    navController.navigate(HappyKidDestinations.ALPHABET_ROUTE)
                },
                onNavigateToTracing = {
                    navController.navigate(HappyKidDestinations.TRACING_ROUTE)
                },
                onNavigateToPhonics = {
                    navController.navigate(HappyKidDestinations.PHONICS_ROUTE)
                },
                onNavigateToStory = {
                    navController.navigate(HappyKidDestinations.STORY_ROUTE)
                },
                onNavigateToVocabulary = {
                    navController.navigate(HappyKidDestinations.VOCABULARY_ROUTE)
                },
                onNavigateToSettings = {
                    navController.navigate(HappyKidDestinations.SETTINGS_ROUTE)
                },
                onNavigateToAnalytics = {
                    navController.navigate(HappyKidDestinations.ANALYTICS_ROUTE)
                },
                onNavigateToParentDashboard = {
                    navController.navigate(HappyKidDestinations.PARENT_AUTH_ROUTE)
                }
            )
        }

        composable(HappyKidDestinations.ALPHABET_ROUTE) {
            AlphabetScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.SETTINGS_ROUTE) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.TRACING_ROUTE) {
            TracingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.PHONICS_ROUTE) {
            PhonicsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSettings = {
                    navController.navigate(HappyKidDestinations.SETTINGS_ROUTE)
                }
            )
        }

        composable(HappyKidDestinations.STORY_ROUTE) {
            StoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.VOCABULARY_ROUTE) {
            VocabularyScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.ANALYTICS_ROUTE) {
            AnalyticsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.PARENT_AUTH_ROUTE) {
            ParentalAuthScreen(
                onAuthenticationSuccess = {
                    navController.navigate(HappyKidDestinations.PARENT_DASHBOARD_ROUTE) {
                        popUpTo(HappyKidDestinations.PARENT_AUTH_ROUTE) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(HappyKidDestinations.PARENT_DASHBOARD_ROUTE) {
            ParentDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onEndSession = {
                    navController.navigate(HappyKidDestinations.HOME_ROUTE) {
                        popUpTo(HappyKidDestinations.HOME_ROUTE) { inclusive = true }
                    }
                }
            )
        }
    }
}
