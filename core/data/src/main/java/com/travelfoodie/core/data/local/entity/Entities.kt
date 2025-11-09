package com.travelfoodie.core.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val nickname: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trips",
    indices = [Index("userId")]
)
data class TripEntity(
    @PrimaryKey val tripId: String,
    val userId: String,
    val title: String,
    val startDate: Long,
    val endDate: Long,
    val theme: String,
    val regionName: String = "", // Store region name for API regeneration
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["tripId"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val memberId: Long = 0,
    val tripId: String,
    val name: String,
    val role: String = "member"
)

@Entity(
    tableName = "regions",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["tripId"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class RegionEntity(
    @PrimaryKey val regionId: String,
    val tripId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val order: Int = 0
)

@Entity(
    tableName = "pois",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["regionId"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("regionId")]
)
data class PoiEntity(
    @PrimaryKey val poiId: String,
    val regionId: String,
    val name: String,
    val category: String,
    val rating: Float,
    val imageUrl: String?,
    val description: String?
)

@Parcelize
@Entity(
    tableName = "restaurants",
    foreignKeys = [
        ForeignKey(
            entity = RegionEntity::class,
            parentColumns = ["regionId"],
            childColumns = ["regionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("regionId")]
)
data class RestaurantEntity(
    @PrimaryKey val restaurantId: String,
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
) : Parcelable

@Entity(
    tableName = "favorites",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["restaurantId"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("restaurantId")]
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val favoriteId: Long = 0,
    val userId: String,
    val restaurantId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notif_schedules",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["tripId"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tripId")]
)
data class NotifScheduleEntity(
    @PrimaryKey(autoGenerate = true) val scheduleId: Long = 0,
    val tripId: String,
    val fireAt: Long,
    val type: String, // "D-7", "D-3", "D-0"
    val sent: Boolean = false
)
