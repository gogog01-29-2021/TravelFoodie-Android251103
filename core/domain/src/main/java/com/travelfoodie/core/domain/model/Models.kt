package com.travelfoodie.core.domain.model

data class User(
    val userId: String,
    val nickname: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Trip(
    val tripId: String,
    val userId: String,
    val title: String,
    val startDate: Long,
    val endDate: Long,
    val theme: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getDaysUntilStart(): Long {
        val now = System.currentTimeMillis()
        val diff = startDate - now
        return diff / (1000 * 60 * 60 * 24)
    }

    fun isUpcoming(): Boolean = startDate > System.currentTimeMillis()
}

data class Member(
    val memberId: Long = 0,
    val tripId: String,
    val name: String,
    val role: String = "member"
)

data class Region(
    val regionId: String,
    val tripId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val order: Int = 0
)

data class Poi(
    val poiId: String,
    val regionId: String,
    val name: String,
    val category: String,
    val rating: Float,
    val imageUrl: String?,
    val description: String?
)

data class Restaurant(
    val restaurantId: String,
    val regionId: String,
    val name: String,
    val category: String,
    val rating: Float,
    val distance: Double?,
    val lat: Double,
    val lng: Double,
    val menu: String?,
    val hours: String?,
    val reservable: Boolean = false,
    val imageUrl: String?
)

data class Favorite(
    val favoriteId: Long = 0,
    val userId: String,
    val restaurantId: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class NotifSchedule(
    val scheduleId: Long = 0,
    val tripId: String,
    val fireAt: Long,
    val type: String,
    val sent: Boolean = false
)

data class Receipt(
    val receiptId: String,
    val restaurantId: String?,
    val merchantName: String,
    val total: Double,
    val imageUrl: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Result wrapper for API calls
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
