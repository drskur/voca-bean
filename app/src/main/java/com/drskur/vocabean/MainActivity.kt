package com.drskur.vocabean

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.drskur.vocabean.di.AppContainer
import com.drskur.vocabean.sound.LocalSoundPlayer
import com.drskur.vocabean.ui.landing.LandingScreen
import com.drskur.vocabean.ui.parent.ParentHomeScreen
import com.drskur.vocabean.ui.parent.SetEditorScreen
import com.drskur.vocabean.ui.quiz.QuizScreen
import com.drskur.vocabean.ui.sets.SetSelectScreen
import com.drskur.vocabean.ui.theme.VocaBeanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as VocaBeanApplication).container
        setContent {
            VocaBeanTheme {
                VocaBeanApp(container)
            }
        }
    }
}

@Composable
private fun VocaBeanApp(container: AppContainer) {
    val navController = rememberNavController()
    val soundEnabled by container.soundEnabled.collectAsState()

    CompositionLocalProvider(LocalSoundPlayer provides container.soundPlayer) {
        NavHost(navController = navController, startDestination = "landing") {
            composable("landing") {
                LandingScreen(
                    onStartClick = { navController.navigate("sets") },
                    onParentClick = { navController.navigate("parent") }
                )
            }
            composable("sets") {
                SetSelectScreen(
                    repository = container.wordSetRepository,
                    onBackClick = { navController.popBackStack() },
                    onSetClick = { setId -> navController.navigate("quiz/$setId") }
                )
            }
            composable("quiz/{setId}") { entry ->
                QuizScreen(
                    repository = container.wordSetRepository,
                    setId = entry.arguments?.getString("setId").orEmpty(),
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("parent") {
                ParentHomeScreen(
                    repository = container.wordSetRepository,
                    soundEnabled = soundEnabled,
                    onSoundEnabledChange = container::setSoundEnabled,
                    onBackClick = { navController.popBackStack() },
                    onAddSet = { navController.navigate("parent/editor") },
                    onEditSet = { setId -> navController.navigate("parent/editor?setId=$setId") }
                )
            }
            composable(
                route = "parent/editor?setId={setId}",
                arguments = listOf(
                    navArgument("setId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { entry ->
                SetEditorScreen(
                    repository = container.wordSetRepository,
                    setId = entry.arguments?.getString("setId"),
                    onDone = { navController.popBackStack() }
                )
            }
        }
    }
}
