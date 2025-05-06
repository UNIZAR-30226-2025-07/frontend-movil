package eina.unizar.frontend_movil.ui.navigation

import MainMenuScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eina.unizar.frontend_movil.ui.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Un solo NavHost para toda la navegación
    NavHost(navController = navController, startDestination = "main_menu") {
        composable("main_menu") {
            MainMenuScreen(navController)
        }
        composable("login_screen") {
            LoginScreen(navController)
        }
        composable("game") {
            GameScreen(navController)
        }
        composable("friends") {
            FriendsScreen(navController)
        }
        composable("play") {
            GameTypeSelectionScreen(navController)
        }
        composable("create-private-room") {
            PrivateRoomScreen(navController)
        }
        composable("join-private-room") {
            JoinPrivateRoomScreen(navController)
        }
        composable(
            route = "waiting-room/{code}",
            arguments = listOf(
                navArgument("code") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code") ?: ""
            WaitingRoomScreen(
                navController = navController,
                roomCode = code
            )
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
        composable("new_account") {
            NewAccountScreen(navController)  // Asegúrate de tener esta pantalla configurada
        }
        composable("profile_settings") {
            ProfileSettingsScreen(navController)  // Asegúrate de tener esta pantalla configurada
        }
        composable("game") {
            GameTypeSelectionScreen(navController)
        }
        composable("create-private-room") {
            PrivateRoomScreen(navController)
        }
        composable("forgot_password") {
            ForgotPasswordScreen(navController)
        }
        composable("chats") {
            ChatsScreen(navController)
        }
        composable(
            route = "chatScreen/{userId}/{friendId}/{friendName}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("friendId") { type = NavType.StringType },
                navArgument("friendName") {type = NavType.StringType}
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            val friendId = backStackEntry.arguments?.getString("friendId") ?: ""
            val friendName = backStackEntry.arguments?.getString("friendName") ?: ""
            ChatScreen(navController, userId, friendId, friendName)
        }
    }
}