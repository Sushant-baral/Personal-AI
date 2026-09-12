package com.jarvis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jarvis.app.navigation.JarvisNavHost
import com.jarvis.app.ui.chat.ChatViewModel
import com.jarvis.app.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JarvisRoot()
        }
    }
}

@Composable
private fun JarvisRoot() {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as JarvisApp
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.factory(app.repository)
    )

    JarvisTheme {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background,
        ) {
            JarvisNavHost(viewModel = viewModel)
        }
    }
}
