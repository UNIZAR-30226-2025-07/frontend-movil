package eina.unizar.frontend_movil.ui.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.viewmodel.*
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Chat

// Añade esto en tu archivo de pantallas (ej: ChatsScreen.kt)
@Composable
fun ChatsScreen(navController: NavController) {
    // Datos de ejemplo
    val sampleChats = listOf(
        Chat(
            id = "1",
            friendName = "JugadorPro123",
            lastMessage = "¿Vamos a jugar esta noche?",
            timestamp = "10:30 AM",
            unreadCount = 2
        ),
        Chat(
            id = "2",
            friendName = "AliciaGamer",
            lastMessage = "¡Buena partida!",
            timestamp = "9:45 AM",
            unreadCount = 0
        ),
        Chat(
            id = "3",
            friendName = "CarlosElTáctico",
            lastMessage = "Necesito ayuda con la misión...",
            timestamp = "Ayer",
            unreadCount = 1
        ),
        Chat(
            id = "4",
            friendName = "AnaDestructora",
            lastMessage = "🔥🔥🔥",
            timestamp = "Ayer",
            unreadCount = 3
        )
    )

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

        // Lista de chats
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            items(sampleChats) { chat ->
                ChatItem(
                    chat = chat,
                    onChatClick = { navController.navigate("chat/${chat.id}") }
                )
            }
        }
    }
}

@Composable
fun ChatItem(chat: Chat, onChatClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onChatClick() },
        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (puedes reemplazar con imagen real)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(Color.LightGray, CircleShape)
                    .align(Alignment.CenterVertically)
            )

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
                        fontWeight = FontWeight.Bold
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
                        color = TextWhite.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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