package com.jarvis.app.data.repository

import com.jarvis.app.data.local.ConversationStore
import com.jarvis.app.data.model.ChatMessage
import com.jarvis.app.data.model.ChatRequest
import com.jarvis.app.data.model.ConversationRecord
import com.jarvis.app.data.model.MessageRole
import com.jarvis.app.data.model.SettingsRequest
import com.jarvis.app.data.network.JarvisApi
import java.util.UUID

class ChatRepository(
    private val api: JarvisApi,
    private val store: ConversationStore,
) {

    fun recentConversations(): List<ConversationRecord> = store.loadAll()

    fun getConversation(id: Int): ConversationRecord? = store.get(id)

    fun deleteConversation(id: Int) = store.delete(id)

    /**
     * Sends [text] to the assistant, appending both the user message and the
     * reply to the given conversation's local record (creating a new one
     * locally the moment the backend hands back an id). Returns the updated
     * record on success, or throws so the caller can surface a friendly error.
     */
    suspend fun sendMessage(conversationId: Int?, text: String): ConversationRecord {
        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.USER,
            content = text,
            timestamp = System.currentTimeMillis(),
        )

        val existing = conversationId?.let { store.get(it) }
        val priorMessages = existing?.messages.orEmpty()

        val response = api.chat(ChatRequest(message = text, conversationId = conversationId))

        val assistantMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            role = MessageRole.ASSISTANT,
            content = response.answer,
            timestamp = System.currentTimeMillis(),
        )

        val updated = ConversationRecord(
            id = response.conversationId,
            title = existing?.title ?: text.take(60),
            updatedAt = System.currentTimeMillis(),
            messages = priorMessages + userMessage + assistantMessage,
        )
        store.save(updated)
        return updated
    }

    suspend fun getAssistantName(): String = api.getSettings().assistantName

    suspend fun updateAssistantName(name: String): String =
        api.updateSettings(SettingsRequest(assistantName = name)).assistantName
}
