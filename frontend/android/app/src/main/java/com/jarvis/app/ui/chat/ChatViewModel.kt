package com.jarvis.app.ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jarvis.app.data.model.ChatMessage
import com.jarvis.app.data.model.ConversationRecord
import com.jarvis.app.data.repository.ChatRepository
import kotlinx.coroutines.launch

/**
 * Shared across the Home and Chat screens so that "recent" always reflects
 * the latest state and a conversation started from Home carries straight
 * into the Chat screen without a network round trip in between.
 */
class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    var recentConversations by mutableStateOf<List<ConversationRecord>>(emptyList())
        private set

    var activeConversationId by mutableStateOf<Int?>(null)
        private set

    var messages by mutableStateOf<List<ChatMessage>>(emptyList())
        private set

    var isSending by mutableStateOf(false)
        private set

    init {
        refreshRecent()
    }

    fun refreshRecent() {
        recentConversations = repository.recentConversations()
    }

    /** Opens an existing local conversation (tapped from the Recent list). */
    fun openConversation(id: Int) {
        val record = repository.getConversation(id)
        activeConversationId = record?.id
        messages = record?.messages.orEmpty()
    }

    /** Clears the active thread so the Chat screen starts a brand new conversation. */
    fun startNewConversation() {
        activeConversationId = null
        messages = emptyList()
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || isSending) return

        val optimisticUser = ChatMessage(
            id = "pending-${System.currentTimeMillis()}",
            role = com.jarvis.app.data.model.MessageRole.USER,
            content = trimmed,
            timestamp = System.currentTimeMillis(),
        )
        messages = messages + optimisticUser
        isSending = true

        viewModelScope.launch {
            try {
                val updated = repository.sendMessage(activeConversationId, trimmed)
                activeConversationId = updated.id
                messages = updated.messages
                refreshRecent()
            } catch (t: Throwable) {
                messages = messages + ChatMessage(
                    id = "error-${System.currentTimeMillis()}",
                    role = com.jarvis.app.data.model.MessageRole.ASSISTANT,
                    content = "Couldn't reach Jarvis. Check that the backend " +
                        "server and Ollama are both running, then try again.",
                    timestamp = System.currentTimeMillis(),
                    isError = true,
                )
            } finally {
                isSending = false
            }
        }
    }

    companion object {
        fun factory(repository: ChatRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChatViewModel(repository) as T
            }
        }
    }
}
