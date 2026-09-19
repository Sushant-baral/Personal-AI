package com.jarvis.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jarvis.app.data.model.ConversationRecord

/**
 * Lightweight local cache of conversations, keyed by the backend's
 * conversation_id. The Phase 1 backend can only create/continue a
 * conversation via POST /chat - there's no endpoint yet to list past
 * conversations or fetch their messages - so the app is the source of
 * truth for history, purely for display.
 */
class ConversationStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("jarvis_conversations", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadAll(): List<ConversationRecord> {
        val json = prefs.getString(KEY_CONVERSATIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<ConversationRecord>>() {}.type
        return runCatching { gson.fromJson<List<ConversationRecord>>(json, type) }
            .getOrDefault(emptyList())
            .sortedByDescending { it.updatedAt }
    }

    fun save(record: ConversationRecord) {
        val all = loadAll().toMutableList()
        val index = all.indexOfFirst { it.id == record.id }
        if (index >= 0) all[index] = record else all.add(0, record)
        prefs.edit()
            .putString(KEY_CONVERSATIONS, gson.toJson(all))
            .apply()
    }

    fun get(id: Int): ConversationRecord? = loadAll().find { it.id == id }

    companion object {
        private const val KEY_CONVERSATIONS = "conversations"
    }
}
