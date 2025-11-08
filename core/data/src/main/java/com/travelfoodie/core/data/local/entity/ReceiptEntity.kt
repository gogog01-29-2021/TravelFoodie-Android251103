package com.travelfoodie.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["restaurantId"],
            childColumns = ["restaurantId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("restaurantId")]
)
data class ReceiptEntity(
    @PrimaryKey
    val receiptId: String,
    val restaurantId: String? = null,
    val merchantName: String,
    val total: Double,
    val imageUrl: String,
    val ocrText: String? = null,
    val createdAt: Long
)
