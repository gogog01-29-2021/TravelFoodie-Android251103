package com.travelfoodie.core.data.local.dao

import androidx.room.*
import com.travelfoodie.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUser(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startDate ASC")
    fun getTripsByUser(userId: String): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE tripId = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE startDate >= :currentTime ORDER BY startDate ASC LIMIT 1")
    suspend fun getNextTrip(currentTime: Long): TripEntity?
}

@Dao
interface MemberDao {
    @Query("SELECT * FROM members WHERE tripId = :tripId")
    fun getMembersByTrip(tripId: String): Flow<List<MemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)
}

@Dao
interface RegionDao {
    @Query("SELECT * FROM regions WHERE tripId = :tripId ORDER BY `order` ASC")
    fun getRegionsByTrip(tripId: String): Flow<List<RegionEntity>>

    @Query("SELECT * FROM regions WHERE regionId = :regionId")
    suspend fun getRegionById(regionId: String): RegionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegion(region: RegionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRegions(regions: List<RegionEntity>)

    @Delete
    suspend fun deleteRegion(region: RegionEntity)
}

@Dao
interface PoiDao {
    @Query("SELECT * FROM pois WHERE regionId = :regionId")
    fun getPoisByRegion(regionId: String): Flow<List<PoiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PoiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPois(pois: List<PoiEntity>)

    @Delete
    suspend fun deletePoi(poi: PoiEntity)
}

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants WHERE regionId = :regionId ORDER BY rating DESC")
    fun getRestaurantsByRegion(regionId: String): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE restaurantId = :restaurantId")
    suspend fun getRestaurantById(restaurantId: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Delete
    suspend fun deleteRestaurant(restaurant: RestaurantEntity)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun getFavoritesByUser(userId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE userId = :userId AND restaurantId = :restaurantId")
    suspend fun getFavorite(userId: String, restaurantId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}

@Dao
interface NotifScheduleDao {
    @Query("SELECT * FROM notif_schedules WHERE tripId = :tripId")
    fun getSchedulesByTrip(tripId: String): Flow<List<NotifScheduleEntity>>

    @Query("SELECT * FROM notif_schedules WHERE sent = 0 AND fireAt <= :currentTime")
    suspend fun getPendingSchedules(currentTime: Long): List<NotifScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: NotifScheduleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<NotifScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: NotifScheduleEntity)

    @Query("DELETE FROM notif_schedules WHERE tripId = :tripId")
    suspend fun deleteSchedulesByTrip(tripId: String)
}

@Dao
interface ReceiptDao {
    @Query("SELECT * FROM receipts ORDER BY createdAt DESC")
    fun getAllReceipts(): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE restaurantId = :restaurantId")
    fun getReceiptsByRestaurant(restaurantId: String): Flow<List<ReceiptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)
}
