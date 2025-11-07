package com.travelfoodie.core.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIApi {
    @POST("v1/chat/completions")
    suspend fun getChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: ChatCompletionRequest
    ): ChatCompletionResponse
}

@JsonClass(generateAdapter = true)
data class ChatCompletionRequest(
    @Json(name = "model") val model: String = "gpt-3.5-turbo",
    @Json(name = "messages") val messages: List<ChatMessage>,
    @Json(name = "temperature") val temperature: Double = 0.7,
    @Json(name = "max_tokens") val maxTokens: Int = 1000
)

@JsonClass(generateAdapter = true)
data class ChatMessage(
    @Json(name = "role") val role: String,
    @Json(name = "content") val content: String
)

@JsonClass(generateAdapter = true)
data class ChatCompletionResponse(
    @Json(name = "id") val id: String,
    @Json(name = "choices") val choices: List<ChatChoice>
)

@JsonClass(generateAdapter = true)
data class ChatChoice(
    @Json(name = "message") val message: ChatMessage,
    @Json(name = "finish_reason") val finishReason: String
)
