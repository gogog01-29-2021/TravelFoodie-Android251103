package com.travelfoodie.core.data.repository

import com.travelfoodie.core.data.BuildConfig
import com.travelfoodie.core.data.local.dao.PoiDao
import com.travelfoodie.core.data.local.entity.PoiEntity
import com.travelfoodie.core.data.remote.ChatCompletionRequest
import com.travelfoodie.core.data.remote.ChatMessage
import com.travelfoodie.core.data.remote.GooglePlacesApi
import com.travelfoodie.core.data.remote.OpenAIApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PoiRepository @Inject constructor(
    private val poiDao: PoiDao,
    private val openAIApi: OpenAIApi,
    private val googlePlacesApi: GooglePlacesApi
) {
    fun getPoisByRegion(regionId: String): Flow<List<PoiEntity>> {
        return poiDao.getPoisByRegion(regionId)
    }

    suspend fun generateMockAttractions(
        regionId: String,
        regionName: String
    ): List<PoiEntity> = coroutineScope {
        android.util.Log.d("PoiRepository", "generateMockAttractions called - regionId: $regionId, regionName: $regionName")
        android.util.Log.d("PoiRepository", "Calling BOTH ChatGPT AND Google Places APIs in parallel")

        try {
            // Call both APIs in parallel
            val chatGptDeferred = async { getAttractionsFromChatGPT(regionName) }
            val googlePlacesDeferred = async { getAttractionsFromGooglePlaces(regionName) }

            val chatGptResults = try {
                chatGptDeferred.await()
            } catch (e: Exception) {
                android.util.Log.e("PoiRepository", "ChatGPT API error: ${e.message}", e)
                emptyList()
            }

            val googlePlacesResults = try {
                googlePlacesDeferred.await()
            } catch (e: Exception) {
                android.util.Log.e("PoiRepository", "Google Places API error: ${e.message}", e)
                emptyList()
            }

            android.util.Log.d("PoiRepository", "ChatGPT returned ${chatGptResults.size} attractions")
            android.util.Log.d("PoiRepository", "Google Places returned ${googlePlacesResults.size} attractions")

            // Combine results from both APIs
            val combinedResults = (chatGptResults + googlePlacesResults)
                .map { it.copy(regionId = regionId) }
                .take(10) // Limit to 10 total attractions

            android.util.Log.d("PoiRepository", "Combined total: ${combinedResults.size} attractions")
            combinedResults.forEach {
                android.util.Log.d("PoiRepository", "  - ${it.name} (${it.category}) - Rating: ${it.rating}")
            }

            // Insert into database
            poiDao.insertPois(combinedResults)
            android.util.Log.d("PoiRepository", "Successfully inserted ${combinedResults.size} POIs into database")

            combinedResults
        } catch (e: Exception) {
            android.util.Log.e("PoiRepository", "ERROR in generateMockAttractions: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get attraction recommendations from ChatGPT
     */
    private suspend fun getAttractionsFromChatGPT(regionName: String): List<PoiEntity> {
        if (BuildConfig.OPENAI_API_KEY.isEmpty()) {
            android.util.Log.w("PoiRepository", "OpenAI API key not configured")
            return emptyList()
        }

        return try {
            val prompt = """
                Recommend 5 must-visit attractions in $regionName.
                For each attraction, provide:
                - name: attraction name
                - category: one of (역사, 문화, 자연, 쇼핑, 전망대, 해변, 시장, 랜드마크)
                - rating: a rating between 4.0 and 5.0
                - description: brief description in Korean (one sentence)

                Return ONLY a JSON array in this exact format:
                [{"name":"...", "category":"...", "rating":4.5, "description":"..."}]
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage(role = "system", content = "You are a travel expert providing attraction recommendations."),
                    ChatMessage(role = "user", content = prompt)
                ),
                temperature = 0.7,
                maxTokens = 1000
            )

            val response = openAIApi.getChatCompletion(
                authorization = "Bearer ${BuildConfig.OPENAI_API_KEY}",
                request = request
            )

            val content = response.choices.firstOrNull()?.message?.content ?: return emptyList()
            android.util.Log.d("PoiRepository", "ChatGPT response: $content")

            // Parse JSON response
            parseChatGPTResponse(content)
        } catch (e: Exception) {
            android.util.Log.e("PoiRepository", "ChatGPT API call failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseChatGPTResponse(jsonString: String): List<PoiEntity> {
        return try {
            val jsonArray = JSONArray(jsonString.substringAfter("[").substringBefore("]") + "]")
            val attractions = mutableListOf<PoiEntity>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                attractions.add(
                    PoiEntity(
                        poiId = UUID.randomUUID().toString(),
                        regionId = "", // Will be set later
                        name = json.optString("name", "Unknown"),
                        category = json.optString("category", "기타"),
                        rating = json.optDouble("rating", 4.5).toFloat(),
                        imageUrl = null,
                        description = json.optString("description", "")
                    )
                )
            }

            attractions
        } catch (e: Exception) {
            android.util.Log.e("PoiRepository", "Failed to parse ChatGPT JSON: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get attractions from Google Places API
     */
    private suspend fun getAttractionsFromGooglePlaces(regionName: String): List<PoiEntity> {
        if (BuildConfig.GOOGLE_PLACES_API_KEY.isEmpty() ||
            BuildConfig.GOOGLE_PLACES_API_KEY == "YOUR_GOOGLE_PLACES_API_KEY_HERE") {
            android.util.Log.w("PoiRepository", "Google Places API key not configured")
            return emptyList()
        }

        return try {
            val query = "$regionName tourist attractions"
            val response = googlePlacesApi.searchPlaces(
                query = query,
                apiKey = BuildConfig.GOOGLE_PLACES_API_KEY,
                language = "ko"
            )

            if (response.status != "OK") {
                android.util.Log.w("PoiRepository", "Google Places API returned status: ${response.status}")
                return emptyList()
            }

            response.results.take(5).map { place ->
                PoiEntity(
                    poiId = UUID.randomUUID().toString(),
                    regionId = "", // Will be set later
                    name = place.name,
                    category = mapPlaceTypeToCategory(place.types),
                    rating = place.rating?.toFloat() ?: 4.0f,
                    imageUrl = null, // Could use photo reference to build URL
                    description = place.formattedAddress ?: ""
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("PoiRepository", "Google Places API call failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun mapPlaceTypeToCategory(types: List<String>?): String {
        if (types.isNullOrEmpty()) return "기타"

        return when {
            types.any { it in listOf("museum", "art_gallery") } -> "문화"
            types.any { it in listOf("tourist_attraction", "point_of_interest") } -> "관광지"
            types.any { it in listOf("park", "natural_feature") } -> "자연"
            types.any { it in listOf("shopping_mall", "store") } -> "쇼핑"
            types.any { it == "church" } -> "역사"
            else -> "명소"
        }
    }

    suspend fun insertPoi(poi: PoiEntity) {
        poiDao.insertPoi(poi)
    }

    suspend fun deletePoi(poi: PoiEntity) {
        poiDao.deletePoi(poi)
    }
}
