package com.jarvis.app.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jarvis.app.ui.chat.ChatScreen
import com.jarvis.app.ui.chat.ChatViewModel
import com.jarvis.app.ui.home.HomeScreen

private const val ROUTE_HOME = "home"
private const val ROUTE_CHAT = "chat?conversationId={conversationId}"
private const val ARG_CONVERSATION_ID = "conversationId"

@Composable
fun JarvisNavHost(viewModel: ChatViewModel) {
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
    }
}
