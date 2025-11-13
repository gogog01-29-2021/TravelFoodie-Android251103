package com.travelfoodie.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.travelfoodie.core.data.local.dao.*
import com.travelfoodie.core.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        TripEntity::class,
        MemberEntity::class,
        RegionEntity::class,
        PoiEntity::class,
        RestaurantEntity::class,
        FavoriteEntity::class,
        NotifScheduleEntity::class,
        ReceiptEntity::class,
        ChatMessageEntity::class,
        FriendEntity::class,
        TripInvitationEntity::class
    ],
    version = 4, // Added FriendEntity and TripInvitationEntity
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun memberDao(): MemberDao
    abstract fun regionDao(): RegionDao
    abstract fun poiDao(): PoiDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun notifScheduleDao(): NotifScheduleDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun friendDao(): FriendDao
    abstract fun tripInvitationDao(): TripInvitationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_foodie_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
