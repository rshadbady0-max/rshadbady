package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.EnglishMasteryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EnglishMasteryApplication

        setContent {
            val isDarkMode by app.repository.preferencesManager.isDarkMode.collectAsStateWithLifecycle()

            EnglishMasteryTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainAppNavigation(
                        application = app,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}
