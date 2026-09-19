package com.jarvis.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jarvis.app.data.repository.ChatRepository
import com.jarvis.app.ui.chat.ChatScreen
import com.jarvis.app.ui.chat.ChatViewModel
import com.jarvis.app.ui.home.HomeScreen
import com.jarvis.app.ui.settings.SettingsScreen
import com.jarvis.app.ui.settings.SettingsViewModel

private const val ROUTE_HOME = "home"
private const val ROUTE_CHAT = "chat?conversationId={conversationId}"
private const val ROUTE_SETTINGS = "settings"
private const val ARG_CONVERSATION_ID = "conversationId"

@Composable
fun JarvisNavHost(viewModel: ChatViewModel, repository: ChatRepository) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_HOME,
    ) {
        composable(
            route = ROUTE_HOME,
            exitTransition = { fadeOut(animationSpec = androidx.compose.animation.core.tween(150)) },
            popEnterTransition = { fadeIn(animationSpec = androidx.compose.animation.core.tween(150)) },
        ) {
            HomeScreen(
                viewModel = viewModel,
                onStartNewChat = { firstMessage ->
                    viewModel.startNewConversation()
                    navController.navigate("chat?conversationId=-1")
                    viewModel.sendMessage(firstMessage)
                },
                onOpenConversation = { id ->
                    viewModel.openConversation(id)
                    navController.navigate("chat?conversationId=$id")
                },
                onOpenSettings = {
                    navController.navigate(ROUTE_SETTINGS)
                },
            )
        }

        composable(
            route = ROUTE_CHAT,
            arguments = listOf(
                navArgument(ARG_CONVERSATION_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                }
            ),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it / 6 }) + fadeIn(animationSpec = androidx.compose.animation.core.tween(200))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it / 6 }) + fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
            },
        ) {
            ChatScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }

        composable(
            route = ROUTE_SETTINGS,
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it / 6 }) + fadeIn(animationSpec = androidx.compose.animation.core.tween(200))
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it / 6 }) + fadeOut(animationSpec = androidx.compose.animation.core.tween(150))
            },
        ) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.factory(repository)
            )
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
