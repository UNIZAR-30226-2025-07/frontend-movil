package eina.unizar.frontend_movil


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eina.unizar.ui.screens.*


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "mainMenu") {
        composable("mainMenu") { MainMenuScreen(navController) }
        composable("game") { GameScreen(navController) }
        composable("friends") { FriendsScreen(navController) }
        composable("achievements") { AchievementsScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
    }
}


