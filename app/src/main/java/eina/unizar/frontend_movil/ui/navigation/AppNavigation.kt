package eina.unizar.frontend_movil.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import eina.unizar.frontend_movil.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }
        composable("game") {
            GameScreen(navController)
        }
        composable("friends") {
            FriendsScreen(navController)
        }
        composable("friend_requests") {
            FriendRequestsScreen(navController)
        }
        composable("add_friend") {
            AddFriendScreen(navController)
        }
        composable("achievements") {
            AchievementsScreen()
        }
        composable("settings") {
            SettingsScreen()
        }
        composable("store") {
            StoreScreen(navController)
        }
        composable("login") {
            LoginScreen(navController)
        }
    }
}


