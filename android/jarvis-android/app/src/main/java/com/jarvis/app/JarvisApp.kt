package com.jarvis.app

import android.app.Application
import com.jarvis.app.data.local.ConversationStore
import com.jarvis.app.data.network.NetworkModule
import com.jarvis.app.data.repository.ChatRepository

/**
 * Simple manual dependency container - no DI framework needed for an app
 * this size yet.
 */
class JarvisApp : Application() {
    lateinit var repository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val store = ConversationStore(this)
        repository = ChatRepository(NetworkModule.api, store)
    }
}
