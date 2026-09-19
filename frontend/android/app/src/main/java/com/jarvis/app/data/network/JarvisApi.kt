package com.jarvis.app.data.network

import com.jarvis.app.data.model.ChatRequest
import com.jarvis.app.data.model.ChatResponse
import com.jarvis.app.data.model.SettingsRequest
import com.jarvis.app.data.model.SettingsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface JarvisApi {
    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @GET("settings")
    suspend fun getSettings(): SettingsResponse

    @POST("settings")
    suspend fun updateSettings(@Body request: SettingsRequest): SettingsResponse
}
