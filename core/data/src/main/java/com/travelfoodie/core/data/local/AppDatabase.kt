package com.travelfoodie.core.data.local

import androidx.room.Database
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
        ReceiptEntity::class
    ],
    version = 1,
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
}
