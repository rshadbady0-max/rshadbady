package com.example.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.EnglishMasteryApplication
import com.example.ui.home.HomeScreen
import com.example.ui.home.HomeViewModel
import com.example.ui.quiz.QuizScreen
import com.example.ui.quiz.QuizViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.stats.StatsScreen
import com.example.ui.stats.StatsViewModel
import com.example.ui.vocabulary.VocabularyScreen
import com.example.ui.vocabulary.VocabularyViewModel

sealed class Screen(
    val route: String,
    val titleArabic: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    object Home : Screen("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "nav_home_tab")
    object Vocabulary : Screen("vocabulary", "الكلمات", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_vocabulary_tab")
    object Quiz : Screen("quiz", "الاختبارات", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_quiz_tab")
    object Stats : Screen("stats", "الإحصائيات", Icons.Filled.BarChart, Icons.Outlined.BarChart, "nav_stats_tab")
    object Settings : Screen("settings", "الإعدادات", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings_tab")
}

@Composable
fun MainAppNavigation(
    application: EnglishMasteryApplication,
    isDarkMode: Boolean
) {
    val navController = rememberNavController()
    val repository = application.repository

    val items = listOf(
        Screen.Home,
        Screen.Vocabulary,
        Screen.Quiz,
        Screen.Stats,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    val isSelected = currentRoute?.startsWith(screen.route) == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.titleArabic
                            )
                        },
                        label = {
                            Text(
                                text = screen.titleArabic,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier.testTag(screen.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory(repository))
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToVocabulary = { level ->
                        if (level != null) {
                            navController.navigate("${Screen.Vocabulary.route}?level=$level")
                        } else {
                            navController.navigate(Screen.Vocabulary.route)
                        }
                    },
                    onNavigateToQuiz = {
                        navController.navigate(Screen.Quiz.route)
                    }
                )
            }

            composable(
                route = "${Screen.Vocabulary.route}?level={level}",
                arguments = listOf(navArgument("level") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val levelArg = backStackEntry.arguments?.getString("level")
                val vocabularyViewModel: VocabularyViewModel = viewModel(factory = VocabularyViewModel.Factory(repository))
                VocabularyScreen(
                    viewModel = vocabularyViewModel,
                    initialLevel = levelArg
                )
            }

            composable(Screen.Quiz.route) {
                val quizViewModel: QuizViewModel = viewModel(factory = QuizViewModel.Factory(repository))
                QuizScreen(viewModel = quizViewModel)
            }

            composable(Screen.Stats.route) {
                val statsViewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory(repository))
                StatsScreen(viewModel = statsViewModel)
            }

            composable(Screen.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(repository))
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
