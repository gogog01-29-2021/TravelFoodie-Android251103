package com.travelfoodie.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["tripId"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["senderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("tripId"),
        Index("senderId")
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val messageId: String,
    val tripId: String,
    val senderId: String,
    val senderName: String,
    val message: String,
    val imageUrl: String? = null,
    val timestamp: Long,
    val isRead: Boolean = false
)
