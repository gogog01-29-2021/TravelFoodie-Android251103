package com.travelfoodie.core.data.repository

import android.content.Context
import com.travelfoodie.core.data.local.dao.RestaurantDao
import com.travelfoodie.core.data.local.entity.RestaurantEntity
import com.travelfoodie.core.data.remote.api.KakaoApiService
import com.travelfoodie.core.data.remote.api.NaverApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestaurantRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val restaurantDao: RestaurantDao,
    private val kakaoApiService: KakaoApiService,
    private val naverApiService: NaverApiService
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
    ): List<RestaurantEntity> {
        val mockRestaurants = listOf(
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

        restaurantDao.insertRestaurants(mockRestaurants)
        return mockRestaurants
    }

    suspend fun insertRestaurant(restaurant: RestaurantEntity) {
        restaurantDao.insertRestaurant(restaurant)
    }

    suspend fun deleteRestaurant(restaurant: RestaurantEntity) {
        restaurantDao.deleteRestaurant(restaurant)
    }
}
