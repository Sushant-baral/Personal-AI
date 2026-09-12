package com.jarvis.app.data.model

import com.google.gson.annotations.SerializedName

// Wire models - shaped to match the Phase 1 FastAPI schemas exactly
// (app/schemas/chat.py in the backend).

data class ChatRequest(
    val message: String,
    @SerializedName("conversation_id") val conversationId: Int? = null,
)

data class ChatResponse(
    val answer: String,
    @SerializedName("conversation_id") val conversationId: Int,
)

// ---- Local UI / persistence models (client-side only) ----
// The Phase 1 backend has no endpoint to list conversations or fetch a past
// conversation's messages, so the app keeps its own local copy of message
// history to power the "Recent" list and reopened chats. New messages still
// go through /chat with the matching conversation_id so the assistant's own
// context on the backend stays consistent.

enum class MessageRole { USER, ASSISTANT }

data class ChatMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val timestamp: Long,
    val isError: Boolean = false,
)

data class ConversationRecord(
    val id: Int,
    val title: String,
    val updatedAt: Long,
    val messages: List<ChatMessage>,
)
