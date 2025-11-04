package com.travelfoodie.core.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

interface GooglePlacesApi {
    @GET("maps/api/place/textsearch/json")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "ko"
    ): PlacesSearchResponse
}

@JsonClass(generateAdapter = true)
data class PlacesSearchResponse(
    @Json(name = "results") val results: List<PlaceResult>,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class PlaceResult(
    @Json(name = "place_id") val placeId: String,
    @Json(name = "name") val name: String,
    @Json(name = "formatted_address") val formattedAddress: String?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "types") val types: List<String>?,
    @Json(name = "photos") val photos: List<PlacePhoto>?,
    @Json(name = "geometry") val geometry: PlaceGeometry?
)

@JsonClass(generateAdapter = true)
data class PlaceGeometry(
    @Json(name = "location") val location: PlaceLocation
)

@JsonClass(generateAdapter = true)
data class PlaceLocation(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double
)

@JsonClass(generateAdapter = true)
data class PlacePhoto(
    @Json(name = "photo_reference") val photoReference: String,
    @Json(name = "width") val width: Int,
    @Json(name = "height") val height: Int
)
