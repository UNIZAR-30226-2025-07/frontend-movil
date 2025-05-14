package eina.unizar.frontend_movil.ui.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class Chat(
    val id: String,
    val friendName: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int
)