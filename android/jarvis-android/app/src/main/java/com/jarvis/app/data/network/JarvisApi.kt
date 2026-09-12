package com.jarvis.app.data.network

import com.jarvis.app.data.model.ChatRequest
import com.jarvis.app.data.model.ChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface JarvisApi {
    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}
