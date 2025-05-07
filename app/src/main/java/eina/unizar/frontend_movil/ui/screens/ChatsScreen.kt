package eina.unizar.frontend_movil.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.models.Chat
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import eina.unizar.frontend_movil.ui.functions.Functions
import org.json.JSONObject
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import org.json.JSONArray
import kotlinx.coroutines.delay

@Composable
fun ChatsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) } // Solo para la carga inicial
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isInitialLoad by remember { mutableStateOf(true) } // Flag para diferenciar carga inicial
    var lastKnownMessages by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var unreadChats by remember { mutableStateOf(mutableMapOf<String, Boolean>()) }

    // Preferencias compartidas
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val authPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val userId = sharedPreferences.getString("userId", null)
    val token = authPreferences.getString("access_token", null)

    val pollingIntervalMs = 15000L

    // Función para cargar los chats
    fun areChatsEqual(oldChats: List<Chat>, newChats: List<Chat>): Boolean {
        if (oldChats.size != newChats.size) return false

        for (i in oldChats.indices) {
            val old = oldChats[i]
            val new = newChats[i]

            if (old.id != new.id ||
                old.lastMessage != new.lastMessage ||
                old.timestamp != new.timestamp ||
                old.unreadCount != new.unreadCount
            ) {
                return false
            }
        }
        return true
    }

    fun loadChats(isInitial: Boolean = false) {
        // Solo mostrar carga en la primera carga
        if (isInitial) {
            isLoading = true
        }
        errorMessage = null

        if (userId == null || token == null) {
            errorMessage = "No se encontraron credenciales"
            isLoading = false
            return
        }

        val headers = mapOf(
            "Content-Type" to "application/json",
            "Auth" to token
        )

        scope.launch {
            try {
                val friendsResponse = Functions.getWithHeaders(
                    "friends/get_friends/$userId",
                    headers
                )


                if (friendsResponse != null) {
                    val jsonArray = JSONArray(friendsResponse)
                    val chatsList = mutableListOf<Chat>()
                    val newLastMessages = mutableMapOf<String, String>()

                    for (i in 0 until jsonArray.length()) {
                        val friendshipObj = jsonArray.getJSONObject(i)

                        val id1 = friendshipObj.getString("id_friend_1")
                        val friendUser = if (id1 == userId) {
                            friendshipObj.getJSONObject("User2")
                        } else {
                            friendshipObj.getJSONObject("User1")
                        }

                        Log.d("ChatsScreen", "friendUser: $friendUser, userId: $userId")

                        val friendId = friendUser.getString("id")
                        val friendName = friendUser.getString("username")

                        var lastMessage = "Sin mensajes aún"
                        var timestamp = "Ahora"
                        var unreadCount = 0

                        try {
                            val messagesResponse = Functions.getWithHeaders(
                                "messages/get-last/$userId/$friendId",
                                headers
                            )

                            Log.d("ChatsScreen", "Messages response: $messagesResponse")

                            if (messagesResponse != null) {
                                try {
                                    val lastMessageObj = JSONObject(messagesResponse)
                                    lastMessage = lastMessageObj.getString("content")
                                    timestamp = lastMessageObj.getString("date")

                                    // Guardar el nuevo último mensaje
                                    newLastMessages[friendId] = lastMessage

                                    // Usar el endpoint específico para mensajes no leídos
                                    val unreadResponse = Functions.getWithHeaders(
                                        "messages/has_not_viewed_messages/$friendId/$userId",
                                        headers
                                    )

                                    Log.d("ChatsScreen", "Unread response: $unreadResponse")

                                    val lastEmissorId = lastMessageObj.getString("id_friend_emisor")
                                    Log.d("ChatScreen", "Last Emissor ID: $lastEmissorId, User ID: $userId")


                                    if (unreadResponse != null) {
                                        try {
                                            val isViewed = lastMessageObj.optBoolean("viewed", true)
                                            val isSentByFriend = lastEmissorId != userId

                                            Log.d("ChatScreen", "isViewed: $isViewed, isSentByFriend: $isSentByFriend")

                                            // Marcar como no leído solo si no está visto y fue enviado por el amigo
                                            if (!isViewed && isSentByFriend) {
                                                unreadCount = 1
                                            } else {
                                                unreadCount = 0
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ChatsScreen", "Error procesando respuesta de mensajes no leídos: ${e.message}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("ChatsScreen", "Error al procesar último mensaje: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "ChatsScreen",
                                "Error al cargar mensajes para $friendName: ${e.message}"
                            )
                        }

                        val chat = Chat(
                            id = friendId,
                            friendName = friendName,
                            lastMessage = lastMessage,
                            timestamp = timestamp,
                            unreadCount = unreadCount
                        )
                        chatsList.add(chat)
                    }

                    // Actualizar solo si hay cambios o es carga inicial
                    if (isInitial || !areChatsEqual(chats, chatsList)) {
                        chats = chatsList
                    }

                    // Actualizar el registro de últimos mensajes
                    lastKnownMessages = newLastMessages

                    isLoading = false
                } else {
                    if (isInitial) {
                        errorMessage = "No se pudieron cargar los amigos"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatsScreen", "Error cargando amigos: ${e.message}", e)
            }
        }
    }

    // Carga inicial
    LaunchedEffect(Unit) {
        loadChats(isInitial = true)
        isInitialLoad = false

        // Polling
        while (true) {
            delay(pollingIntervalMs)
            loadChats(isInitial = false) // Actualizaciones silenciosas
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "CHATS",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier
                .padding(vertical = 24.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Contenido según el estado
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextWhite)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error al cargar chats: $errorMessage",
                        color = Color.Red
                    )
                }
            }
            chats.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tienes chats activos",
                        color = TextWhite,
                        fontSize = 18.sp
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    items(chats) { chat ->
                        ChatItem(
                            chat = chat,
                            onChatClick = { navController.navigate("chatScreen/$userId/${chat.id}/${chat.friendName}") },
                            onMarkAsRead = { chatId ->
                                // Actualizar el mapa de chats no leídos
                                unreadChats = unreadChats.toMutableMap().apply {
                                    put(chatId, false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit, onMarkAsRead: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable {
                // Usamos el callback para marcar como leído
                onMarkAsRead(chat.id)
                onChatClick()
            },
        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar con indicador de mensajes sin leer
            Box(contentAlignment = Alignment.Center) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.Gray, CircleShape)
                )

                // Círculo rojo para indicar mensajes sin leer
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            // Contenido del chat
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.friendName,
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold
                    )
                    Text(
                        text = chat.timestamp,
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = chat.lastMessage,
                        color = TextWhite.copy(alpha = if (chat.unreadCount > 0) 1f else 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )

                    if (chat.unreadCount > 0) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(text = chat.unreadCount.toString())
                        }
                    }
                }
            }
        }
    }
}