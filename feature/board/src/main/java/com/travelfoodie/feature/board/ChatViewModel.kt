package com.travelfoodie.feature.board

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelfoodie.core.data.local.dao.ChatMessageDao
import com.travelfoodie.core.data.local.entity.ChatMessageEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatMessageDao: ChatMessageDao,
    private val firebaseSync: FirebaseChatSync
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    private var currentTripId: String? = null
    private val currentUserId = "user_temp_id" // TODO: Get from auth

    fun loadMessages(tripId: String) {
        currentTripId = tripId
        viewModelScope.launch {
            chatMessageDao.getMessagesByTripId(tripId).collect { messageList ->
                _messages.value = messageList
            }
        }

        // Sync with Firebase
        firebaseSync.syncMessages(tripId)
    }

    fun sendMessage(messageText: String) {
        val tripId = currentTripId ?: return

        val message = ChatMessageEntity(
            messageId = UUID.randomUUID().toString(),
            tripId = tripId,
            senderId = currentUserId,
            senderName = "사용자", // TODO: Get from user profile
            message = messageText,
            imageUrl = null,
            timestamp = System.currentTimeMillis(),
            isRead = false
        )

        viewModelScope.launch {
            // Save locally
            chatMessageDao.insertMessage(message)

            // Sync to Firebase
            firebaseSync.sendMessage(tripId, message)
        }
    }
}
