package com.travelfoodie.core.data.local.dao

import androidx.room.*
import com.travelfoodie.core.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE tripId = :tripId ORDER BY timestamp ASC")
    fun getMessagesByTripId(tripId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE tripId = :tripId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(tripId: String): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE messageId = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE tripId = :tripId")
    suspend fun deleteMessagesByTripId(tripId: String)

    @Query("UPDATE chat_messages SET isRead = 1 WHERE tripId = :tripId AND senderId != :currentUserId")
    suspend fun markMessagesAsRead(tripId: String, currentUserId: String)
}
