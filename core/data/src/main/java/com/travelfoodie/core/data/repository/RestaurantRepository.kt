package com.travelfoodie.core.data.repository

import android.content.Context
import com.travelfoodie.core.data.BuildConfig
import com.travelfoodie.core.data.local.dao.RestaurantDao
import com.travelfoodie.core.data.local.entity.RestaurantEntity
import com.travelfoodie.core.data.remote.ChatCompletionRequest
import com.travelfoodie.core.data.remote.ChatMessage
import com.travelfoodie.core.data.remote.GooglePlacesApi
import com.travelfoodie.core.data.remote.OpenAIApi
import com.travelfoodie.core.data.remote.api.KakaoApiService
import com.travelfoodie.core.data.remote.api.NaverApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restaurantDao: RestaurantDao,
    private val kakaoApiService: KakaoApiService,
    private val naverApiService: NaverApiService,
    private val openAIApi: OpenAIApi,
    private val googlePlacesApi: GooglePlacesApi
) {
    fun getRestaurantsByRegion(regionId: String): Flow<List<RestaurantEntity>> {
        return restaurantDao.getRestaurantsByRegion(regionId)
    }

    suspend fun getRestaurantById(restaurantId: String): RestaurantEntity? {
        return restaurantDao.getRestaurantById(restaurantId)
    }

    suspend fun fetchAndSaveRestaurants(
        regionId: String,
        regionName: String,
        lat: Double,
        lng: Double,
        kakaoApiKey: String
    ): List<RestaurantEntity> {
        return try {
            val response = kakaoApiService.searchByCategory(
                authorization = "KakaoAK $kakaoApiKey",
                categoryCode = "FD6", // Food category
                longitude = lng,
                latitude = lat,
                radius = 5000,
                size = 10
            )

            val restaurants = response.documents.take(10).map { place ->
                RestaurantEntity(
                    restaurantId = UUID.randomUUID().toString(),
                    regionId = regionId,
                    name = place.placeName,
                    category = place.categoryName.split(">").lastOrNull()?.trim() ?: "음식점",
                    rating = (3.5f + Math.random().toFloat() * 1.5f).toFloat(), // Mock rating
                    distance = null,
                    lat = place.y.toDouble(),
                    lng = place.x.toDouble(),
                    menu = null,
                    hours = null,
                    reservable = false,
                    imageUrl = null
                )
            }

            restaurantDao.insertRestaurants(restaurants)
            restaurants
        } catch (e: Exception) {
            e.printStackTrace()
            // Return mock data if API fails
            createMockRestaurants(regionId, regionName, lat, lng)
        }
    }

    suspend fun createMockRestaurants(
        regionId: String,
        regionName: String,
        lat: Double,
        lng: Double
    ): List<RestaurantEntity> = coroutineScope {
        android.util.Log.d("RestaurantRepository", "=== createMockRestaurants START ===")
        android.util.Log.d("RestaurantRepository", "regionId: $regionId, regionName: $regionName")
        android.util.Log.d("RestaurantRepository", "OpenAI Key configured: ${BuildConfig.OPENAI_API_KEY.isNotEmpty()}")
        android.util.Log.d("RestaurantRepository", "Google Places Key configured: ${BuildConfig.GOOGLE_PLACES_API_KEY.isNotEmpty()}")

        try {
            // Call both APIs in parallel
            val chatGptDeferred = async { getRestaurantsFromChatGPT(regionName) }
            val googlePlacesDeferred = async { getRestaurantsFromGooglePlaces(regionName) }

            val chatGptResults = try {
                chatGptDeferred.await()
            } catch (e: Exception) {
                android.util.Log.e("RestaurantRepository", "❌ ChatGPT API error: ${e.message}", e)
                emptyList()
            }

            val googlePlacesResults = try {
                googlePlacesDeferred.await()
            } catch (e: Exception) {
                android.util.Log.e("RestaurantRepository", "❌ Google Places API error: ${e.message}", e)
                emptyList()
            }

            android.util.Log.d("RestaurantRepository", "✅ ChatGPT returned ${chatGptResults.size} restaurants")
            android.util.Log.d("RestaurantRepository", "✅ Google Places returned ${googlePlacesResults.size} restaurants")

            // Combine results from both APIs
            var combinedResults = (chatGptResults + googlePlacesResults)
                .map { it.copy(regionId = regionId) }
                .take(10) // Limit to 10 total restaurants

            // Fallback: If both APIs failed, use real restaurant data
            if (combinedResults.isEmpty()) {
                android.util.Log.w("RestaurantRepository", "⚠️ Both APIs failed! Using fallback real restaurants")
                combinedResults = getFallbackRestaurants(regionId, regionName, lat, lng)
            }

            android.util.Log.d("RestaurantRepository", "📊 Combined total: ${combinedResults.size} restaurants")
            combinedResults.forEach {
                android.util.Log.d("RestaurantRepository", "  - ${it.name} (${it.category}) - Rating: ${it.rating}")
            }

            // Insert into database
            restaurantDao.insertRestaurants(combinedResults)
            android.util.Log.d("RestaurantRepository", "💾 Successfully inserted ${combinedResults.size} restaurants into database")
            android.util.Log.d("RestaurantRepository", "=== createMockRestaurants END ===")

            combinedResults
        } catch (e: Exception) {
            android.util.Log.e("RestaurantRepository", "❌ ERROR in createMockRestaurants: ${e.message}", e)

            // Last resort: return fallback real restaurants
            val fallbackData = getFallbackRestaurants(regionId, regionName, lat, lng)
            restaurantDao.insertRestaurants(fallbackData)
            fallbackData
        }
    }

    /**
     * Fallback to real world-famous restaurants when APIs fail
     */
    private fun getFallbackRestaurants(
        regionId: String,
        regionName: String,
        lat: Double,
        lng: Double
    ): List<RestaurantEntity> {
        android.util.Log.d("RestaurantRepository", "Using fallback real restaurants for: $regionName")

        // Match region to known cities and return their real famous restaurants
        val restaurants = when {
            regionName.contains("파리", ignoreCase = true) || regionName.contains("Paris", ignoreCase = true) -> listOf(
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Le Jules Verne", "양식", 4.8f, null, lat, lng, "미슐랭 스타 레스토랑", "12:00-22:00", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "L'Ami Jean", "양식", 4.7f, null, lat, lng, "전통 프랑스 요리", "12:00-21:30", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Breizh Café", "양식", 4.6f, null, lat, lng, "유명한 크레페", "11:30-22:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Café de Flore", "카페", 4.5f, null, lat, lng, "역사적인 카페", "07:00-01:30", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Angelina Paris", "카페", 4.6f, null, lat, lng, "핫초콜릿으로 유명", "08:00-19:00", false, null)
            )
            regionName.contains("도쿄", ignoreCase = true) || regionName.contains("Tokyo", ignoreCase = true) -> listOf(
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "스시 다이와", "일식", 4.8f, null, lat, lng, "츠키지 최고의 스시", "05:00-13:30", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "이치란 라멘", "일식", 4.6f, null, lat, lng, "유명 라멘 체인", "24시간", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "긴자 쿄베이", "일식", 4.9f, null, lat, lng, "미슐랭 3스타", "12:00-15:00, 17:00-22:00", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "츠타야 서점 카페", "카페", 4.4f, null, lat, lng, "책과 카페", "07:00-02:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "야키니쿠 점보", "일식", 4.7f, null, lat, lng, "고급 고기 레스토랑", "11:30-23:00", true, null)
            )
            regionName.contains("서울", ignoreCase = true) || regionName.contains("Seoul", ignoreCase = true) -> listOf(
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "광장시장", "한식", 4.7f, null, lat, lng, "빈대떡, 마약김밥", "08:00-23:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "명동교자", "한식", 4.6f, null, lat, lng, "유명 칼국수 맛집", "10:30-22:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "진옥화 할매 원조 닭한마리", "한식", 4.5f, null, lat, lng, "닭한마리 원조", "10:00-22:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "이태원 삼원가든", "한식", 4.8f, null, lat, lng, "프리미엄 한우", "12:00-22:00", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "카페 연남동", "카페", 4.4f, null, lat, lng, "감성 카페", "11:00-22:00", false, null)
            )
            regionName.contains("뉴욕", ignoreCase = true) || regionName.contains("New York", ignoreCase = true) || regionName.contains("NYC", ignoreCase = true) -> listOf(
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Katz's Delicatessen", "양식", 4.7f, null, lat, lng, "유명 델리", "08:00-22:45", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Joe's Pizza", "양식", 4.6f, null, lat, lng, "뉴욕 스타일 피자", "10:00-04:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Shake Shack", "양식", 4.5f, null, lat, lng, "유명 버거 체인", "10:30-23:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Per Se", "양식", 4.9f, null, lat, lng, "미슐랭 3스타", "17:30-21:30", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "Café Sabarsky", "카페", 4.5f, null, lat, lng, "오스트리아 카페", "09:00-21:00", false, null)
            )
            else -> listOf(
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "$regionName 전통 레스토랑", "한식", 4.5f, null, lat, lng, "현지 전통 요리", "11:00-22:00", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "$regionName 인기 식당", "양식", 4.4f, null, lat, lng, "인기 메뉴", "10:00-23:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "$regionName 카페", "카페", 4.3f, null, lat, lng, "커피와 디저트", "08:00-22:00", false, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "$regionName 고급 레스토랑", "양식", 4.6f, null, lat, lng, "파인 다이닝", "18:00-23:00", true, null),
                RestaurantEntity(UUID.randomUUID().toString(), regionId, "$regionName 길거리 음식", "분식", 4.2f, null, lat, lng, "현지 스트리트 푸드", "09:00-21:00", false, null)
            )
        }

        android.util.Log.d("RestaurantRepository", "Fallback generated ${restaurants.size} real restaurants")
        return restaurants
    }

    /**
     * Deprecated - keeping for backward compatibility
     */
    @Deprecated("Use getFallbackRestaurants instead")
    private fun createFallbackMockRestaurants(
        regionId: String,
        regionName: String,
        lat: Double,
        lng: Double
    ): List<RestaurantEntity> {
        return listOf(
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 전통 한식당",
                category = "한식",
                rating = 4.5f,
                distance = 0.5,
                lat = lat + 0.001,
                lng = lng + 0.001,
                menu = "불고기, 비빔밥, 된장찌개",
                hours = "10:00 - 22:00",
                reservable = true,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 이탈리안 레스토랑",
                category = "양식",
                rating = 4.3f,
                distance = 0.8,
                lat = lat + 0.002,
                lng = lng - 0.001,
                menu = "파스타, 피자, 리조또",
                hours = "11:00 - 23:00",
                reservable = true,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 초밥집",
                category = "일식",
                rating = 4.7f,
                distance = 1.2,
                lat = lat - 0.001,
                lng = lng + 0.002,
                menu = "초밥, 사시미, 우동",
                hours = "12:00 - 22:00",
                reservable = false,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 중화요리",
                category = "중식",
                rating = 4.2f,
                distance = 1.5,
                lat = lat + 0.003,
                lng = lng + 0.003,
                menu = "짜장면, 짬뽕, 탕수육",
                hours = "11:00 - 21:00",
                reservable = false,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 카페",
                category = "카페",
                rating = 4.4f,
                distance = 0.3,
                lat = lat - 0.002,
                lng = lng - 0.002,
                menu = "아메리카노, 라떼, 케이크",
                hours = "08:00 - 22:00",
                reservable = false,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 고기집",
                category = "한식",
                rating = 4.6f,
                distance = 2.0,
                lat = lat + 0.004,
                lng = lng - 0.003,
                menu = "삼겹살, 갈비, 목살",
                hours = "17:00 - 24:00",
                reservable = true,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 분식집",
                category = "분식",
                rating = 4.1f,
                distance = 0.6,
                lat = lat - 0.003,
                lng = lng + 0.001,
                menu = "떡볶이, 김밥, 순대",
                hours = "09:00 - 20:00",
                reservable = false,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 치킨집",
                category = "치킨",
                rating = 4.3f,
                distance = 1.0,
                lat = lat + 0.002,
                lng = lng + 0.004,
                menu = "후라이드, 양념치킨, 반반",
                hours = "16:00 - 02:00",
                reservable = false,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 베이커리",
                category = "베이커리",
                rating = 4.5f,
                distance = 0.7,
                lat = lat - 0.001,
                lng = lng - 0.004,
                menu = "크루아상, 바게트, 케이크",
                hours = "07:00 - 21:00",
                reservable = false,
                imageUrl = null
            ),
            RestaurantEntity(
                restaurantId = UUID.randomUUID().toString(),
                regionId = regionId,
                name = "$regionName 해산물 요리",
                category = "해산물",
                rating = 4.4f,
                distance = 1.8,
                lat = lat + 0.005,
                lng = lng + 0.002,
                menu = "회, 조개구이, 해물탕",
                hours = "11:00 - 22:00",
                reservable = true,
                imageUrl = null
            )
        )
    }

    suspend fun insertRestaurant(restaurant: RestaurantEntity) {
        restaurantDao.insertRestaurant(restaurant)
    }

    suspend fun deleteRestaurant(restaurant: RestaurantEntity) {
        restaurantDao.deleteRestaurant(restaurant)
    }

    suspend fun deleteRestaurantsByRegionId(regionId: String) {
        restaurantDao.deleteRestaurantsByRegionId(regionId)
    }

    /**
     * Get restaurant recommendations from ChatGPT
     */
    private suspend fun getRestaurantsFromChatGPT(regionName: String): List<RestaurantEntity> {
        if (BuildConfig.OPENAI_API_KEY.isEmpty()) {
            android.util.Log.w("RestaurantRepository", "OpenAI API key not configured")
            return emptyList()
        }

        return try {
            val prompt = """
                Recommend 5 must-visit restaurants in $regionName.
                For each restaurant, provide:
                - name: restaurant name
                - category: one of (한식, 중식, 일식, 양식, 카페, 분식, 치킨, 베이커리, 해산물, 기타)
                - rating: a rating between 4.0 and 5.0
                - menu: 3 popular menu items (comma separated)

                Return ONLY a JSON array in this exact format:
                [{"name":"...", "category":"...", "rating":4.5, "menu":"..."}]
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    ChatMessage(role = "system", content = "You are a food expert providing restaurant recommendations."),
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
            android.util.Log.d("RestaurantRepository", "ChatGPT response: $content")

            // Parse JSON response
            parseChatGPTResponse(content)
        } catch (e: Exception) {
            android.util.Log.e("RestaurantRepository", "ChatGPT API call failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun parseChatGPTResponse(jsonString: String): List<RestaurantEntity> {
        return try {
            // Strip markdown code blocks if present (```json ... ``` or ``` ... ```)
            var cleanJson = jsonString.trim()
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson
                    .substringAfter("```json")
                    .substringAfter("```")  // Handle both ```json and ``` markers
                    .substringBeforeLast("```")
                    .trim()
            }

            val jsonArray = JSONArray(cleanJson)
            val restaurants = mutableListOf<RestaurantEntity>()

            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                restaurants.add(
                    RestaurantEntity(
                        restaurantId = UUID.randomUUID().toString(),
                        regionId = "", // Will be set later
                        name = json.optString("name", "Unknown Restaurant"),
                        category = json.optString("category", "기타"),
                        rating = json.optDouble("rating", 4.5).toFloat(),
                        distance = null,
                        lat = 0.0, // Will need geocoding for actual location
                        lng = 0.0,
                        menu = json.optString("menu", ""),
                        hours = null,
                        reservable = false,
                        imageUrl = null
                    )
                )
            }

            restaurants
        } catch (e: Exception) {
            android.util.Log.e("RestaurantRepository", "Failed to parse ChatGPT JSON: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Get restaurants from Google Places API
     */
    private suspend fun getRestaurantsFromGooglePlaces(regionName: String): List<RestaurantEntity> {
        if (BuildConfig.GOOGLE_PLACES_API_KEY.isEmpty()) {
            android.util.Log.w("RestaurantRepository", "Google Places API key not configured")
            return emptyList()
        }

        return try {
            val query = "$regionName restaurants"
            val response = googlePlacesApi.searchPlaces(
                query = query,
                apiKey = BuildConfig.GOOGLE_PLACES_API_KEY,
                language = "ko"
            )

            if (response.status != "OK") {
                android.util.Log.w("RestaurantRepository", "Google Places API returned status: ${response.status}")
                return emptyList()
            }

            response.results.take(5).map { place ->
                RestaurantEntity(
                    restaurantId = UUID.randomUUID().toString(),
                    regionId = "", // Will be set later
                    name = place.name,
                    category = mapPlaceTypeToCategory(place.types),
                    rating = place.rating?.toFloat() ?: 4.0f,
                    distance = null,
                    lat = place.geometry?.location?.lat ?: 0.0,
                    lng = place.geometry?.location?.lng ?: 0.0,
                    menu = null,
                    hours = null,
                    reservable = false,
                    imageUrl = null
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("RestaurantRepository", "Google Places API call failed: ${e.message}", e)
            emptyList()
        }
    }

    private fun mapPlaceTypeToCategory(types: List<String>?): String {
        if (types.isNullOrEmpty()) return "기타"

        return when {
            types.any { it in listOf("restaurant", "food") } -> "음식점"
            types.any { it == "cafe" } -> "카페"
            types.any { it == "bakery" } -> "베이커리"
            types.any { it == "bar" } -> "바"
            else -> "음식점"
        }
    }
}
