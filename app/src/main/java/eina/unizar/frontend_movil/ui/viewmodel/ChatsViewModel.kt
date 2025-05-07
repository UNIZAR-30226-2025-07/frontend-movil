package eina.unizar.frontend_movil.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import eina.unizar.frontend_movil.ui.screens.ChatsScreen
import eina.unizar.frontend_movil.ui.models.Chat


class ChatsViewModel : ViewModel() {
    // Estado de chats no leídos con StateFlow
    private val _unreadChats = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val unreadChats: StateFlow<Map<String, Boolean>> = _unreadChats

    // Estado para el último mensaje conocido
    private val _lastKnownMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val lastKnownMessages: StateFlow<Map<String, String>> = _lastKnownMessages

    // Lista de chats
    private val _chats = MutableStateFlow<List<Chat>>(emptyList())
    val chats: StateFlow<List<Chat>> = _chats

    // Marcar un chat como leído
    fun markChatAsRead(chatId: String) {
        val currentMap = _unreadChats.value.toMutableMap()
        currentMap[chatId] = false
        _unreadChats.value = currentMap
    }

    // Actualizar últimos mensajes conocidos
    fun updateLastKnownMessages(messages: Map<String, String>) {
        _lastKnownMessages.value = messages
    }

    // Marcar un chat como no leído
    fun markChatAsUnread(chatId: String) {
        val currentMap = _unreadChats.value.toMutableMap()
        currentMap[chatId] = true
        _unreadChats.value = currentMap
    }

    // Actualizar la lista de chats
    fun updateChats(newChats: List<Chat>) {
        _chats.value = newChats
    }
}