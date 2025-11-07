package com.travelfoodie.core.data.remote.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

// Kakao API Models
@JsonClass(generateAdapter = true)
data class KakaoPlaceResponse(
    @Json(name = "documents") val documents: List<KakaoPlace>,
    @Json(name = "meta") val meta: KakaoMeta
)

@JsonClass(generateAdapter = true)
data class KakaoPlace(
    @Json(name = "id") val id: String,
    @Json(name = "place_name") val placeName: String,
    @Json(name = "category_name") val categoryName: String,
    @Json(name = "phone") val phone: String?,
    @Json(name = "address_name") val addressName: String,
    @Json(name = "road_address_name") val roadAddressName: String?,
    @Json(name = "x") val x: String, // longitude
    @Json(name = "y") val y: String, // latitude
    @Json(name = "place_url") val placeUrl: String?
)

@JsonClass(generateAdapter = true)
data class KakaoMeta(
    @Json(name = "total_count") val totalCount: Int,
    @Json(name = "pageable_count") val pageableCount: Int,
    @Json(name = "is_end") val isEnd: Boolean
)

// Naver API Models
@JsonClass(generateAdapter = true)
data class NaverSearchResponse(
    @Json(name = "items") val items: List<NaverPlace>,
    @Json(name = "total") val total: Int,
    @Json(name = "start") val start: Int,
    @Json(name = "display") val display: Int
)

@JsonClass(generateAdapter = true)
data class NaverPlace(
    @Json(name = "title") val title: String,
    @Json(name = "link") val link: String,
    @Json(name = "category") val category: String,
    @Json(name = "description") val description: String?,
    @Json(name = "telephone") val telephone: String?,
    @Json(name = "address") val address: String,
    @Json(name = "roadAddress") val roadAddress: String?,
    @Json(name = "mapx") val mapx: String, // longitude * 10000000
    @Json(name = "mapy") val mapy: String  // latitude * 10000000
)

// Kakao API Service
interface KakaoApiService {
    @GET("v2/local/search/keyword.json")
    suspend fun searchPlaces(
        @Header("Authorization") authorization: String,
        @Query("query") query: String,
        @Query("x") longitude: Double?,
        @Query("y") latitude: Double?,
        @Query("radius") radius: Int = 5000,
        @Query("size") size: Int = 15
    ): KakaoPlaceResponse

    @GET("v2/local/search/category.json")
    suspend fun searchByCategory(
        @Header("Authorization") authorization: String,
        @Query("category_group_code") categoryCode: String, // FD6 for restaurants
        @Query("x") longitude: Double,
        @Query("y") latitude: Double,
        @Query("radius") radius: Int = 5000,
        @Query("size") size: Int = 15
    ): KakaoPlaceResponse
}

// Naver API Service
interface NaverApiService {
    @GET("v1/search/local.json")
    suspend fun searchLocal(
        @Header("X-Naver-Client-Id") clientId: String,
        @Header("X-Naver-Client-Secret") clientSecret: String,
        @Query("query") query: String,
        @Query("display") display: Int = 10,
        @Query("start") start: Int = 1,
        @Query("sort") sort: String = "random"
    ): NaverSearchResponse
}
