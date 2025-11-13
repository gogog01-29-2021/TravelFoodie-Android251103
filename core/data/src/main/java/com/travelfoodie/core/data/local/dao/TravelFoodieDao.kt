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

    @Query("SELECT * FROM trips WHERE startDate >= :currentTime ORDER BY startDate ASC")
    suspend fun getUpcomingTrips(currentTime: Long): List<TripEntity>
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

    @Query("SELECT * FROM regions WHERE tripId = :tripId ORDER BY `order` ASC")
    suspend fun getRegionsByTripId(tripId: String): List<RegionEntity>

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

    @Query("SELECT * FROM pois WHERE regionId = :regionId")
    suspend fun getPoiByRegionId(regionId: String): List<PoiEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoi(poi: PoiEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPois(pois: List<PoiEntity>)

    @Delete
    suspend fun deletePoi(poi: PoiEntity)

    @Query("DELETE FROM pois WHERE regionId = :regionId")
    suspend fun deletePoiByRegionId(regionId: String)
}

@Dao
interface RestaurantDao {
    @Query("SELECT * FROM restaurants WHERE regionId = :regionId ORDER BY rating DESC")
    fun getRestaurantsByRegion(regionId: String): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE regionId = :regionId ORDER BY rating DESC")
    suspend fun getRestaurantsByRegionId(regionId: String): List<RestaurantEntity>

    @Query("SELECT * FROM restaurants WHERE restaurantId = :restaurantId")
    suspend fun getRestaurantById(restaurantId: String): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Delete
    suspend fun deleteRestaurant(restaurant: RestaurantEntity)

    @Query("DELETE FROM restaurants WHERE regionId = :regionId")
    suspend fun deleteRestaurantsByRegionId(regionId: String)
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

    @Query("SELECT * FROM receipts WHERE restaurantId = :restaurantId ORDER BY createdAt DESC")
    fun getReceiptsByRestaurant(restaurantId: String): Flow<List<ReceiptEntity>>

    @Query("SELECT * FROM receipts WHERE receiptId = :receiptId")
    suspend fun getReceiptById(receiptId: String): ReceiptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Update
    suspend fun updateReceipt(receipt: ReceiptEntity)

    @Delete
    suspend fun deleteReceipt(receipt: ReceiptEntity)

    @Query("DELETE FROM receipts WHERE receiptId = :receiptId")
    suspend fun deleteReceiptById(receiptId: String)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAllMessages()
}

@Dao
interface FriendDao {
    @Query("SELECT * FROM friends WHERE userId = :userId ORDER BY name ASC")
    fun getFriendsByUser(userId: String): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE userId = :userId AND contactType = :contactType")
    fun getFriendsByType(userId: String, contactType: String): Flow<List<FriendEntity>>

    @Query("SELECT * FROM friends WHERE friendId = :friendId")
    suspend fun getFriendById(friendId: Long): FriendEntity?

    @Query("SELECT * FROM friends WHERE userId = :userId AND contactValue = :contactValue")
    suspend fun getFriendByContact(userId: String, contactValue: String): FriendEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriends(friends: List<FriendEntity>)

    @Update
    suspend fun updateFriend(friend: FriendEntity)

    @Delete
    suspend fun deleteFriend(friend: FriendEntity)
}

@Dao
interface TripInvitationDao {
    @Query("SELECT * FROM trip_invitations WHERE tripId = :tripId")
    fun getInvitationsByTrip(tripId: String): Flow<List<TripInvitationEntity>>

    @Query("SELECT * FROM trip_invitations WHERE friendId = :friendId ORDER BY sentAt DESC")
    fun getInvitationsByFriend(friendId: Long): Flow<List<TripInvitationEntity>>

    @Query("SELECT * FROM trip_invitations WHERE invitationId = :invitationId")
    suspend fun getInvitationById(invitationId: Long): TripInvitationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvitation(invitation: TripInvitationEntity): Long

    @Update
    suspend fun updateInvitation(invitation: TripInvitationEntity)

    @Delete
    suspend fun deleteInvitation(invitation: TripInvitationEntity)

    @Query("DELETE FROM trip_invitations WHERE tripId = :tripId")
    suspend fun deleteInvitationsByTrip(tripId: String)
}
