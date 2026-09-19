package com.jarvis.app.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jarvis.app.data.repository.ChatRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: ChatRepository) : ViewModel() {

    var assistantName by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(true)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            try {
                assistantName = repository.getAssistantName()
            } catch (t: Throwable) {
                errorMessage = "Couldn't load settings. Check the backend is running."
            } finally {
                isLoading = false
            }
        }
    }

    fun save(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty() || isSaving) return

        isSaving = true
        errorMessage = null
        viewModelScope.launch {
            try {
                assistantName = repository.updateAssistantName(trimmed)
            } catch (t: Throwable) {
                errorMessage = "Couldn't save. Check the backend is running."
            } finally {
                isSaving = false
            }
        }
    }

    companion object {
        fun factory(repository: ChatRepository) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(repository) as T
            }
        }
    }
}
