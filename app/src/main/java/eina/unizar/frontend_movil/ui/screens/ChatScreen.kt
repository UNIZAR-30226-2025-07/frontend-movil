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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.viewmodel.*
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(navController: NavController, userId: String?, friendId: String?, friendName: String?) {
    // Estado para el mensaje que se está escribiendo
    var messageText by remember { mutableStateOf("") }

    // Estado para los mensajes obtenidos del backend
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val authPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    val token = authPreferences.getString("access_token", null)

    val headers = mapOf(
        "Content-Type" to "application/json",
        "Auth"  to (token ?: "")
    )

    // Función para cargar mensajes
    suspend fun loadMessages() {
        try {
            if (friendId != null && userId != null) {
                val messagesResponse = Functions.getWithHeaders(
                    "messages/get_messages/$userId/$friendId",
                    headers
                )

                Log.d("chatsScreen", "messagesResponse: $messagesResponse")

                if (messagesResponse != null) {
                    val jsonArray = JSONArray(messagesResponse)
                    val fetchedMessages = mutableListOf<Message>()

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        fetchedMessages.add(
                            Message(
                                id = jsonObject.getString("id"),
                                text = jsonObject.getString("content"),
                                timestamp = jsonObject.getString("date"),
                                sentByUser = jsonObject.getString("id_friend_emisor") == userId
                            )
                        )
                    }

                    messages = fetchedMessages
                }
            }
        } catch (e: Exception) {
            errorMessage = "Error al cargar mensajes: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Cargar mensajes inicialmente
    LaunchedEffect(userId, friendId) {
        isLoading = true
        loadMessages()
    }

    // Implementar polling para actualizar mensajes cada 3 segundos
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(3000) // 3 segundos
            loadMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
    ) {
        // Header del chat
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardGray.copy(alpha = 0.3f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextWhite
                )
            }

            Text(
                text = friendName.toString(), // Cambia esto por el nombre del amigo si lo tienes
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Contenido principal
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = TextWhite)
                }
            }
            errorMessage != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = errorMessage ?: "Error desconocido",
                        color = Color.Red
                    )
                }
            }
            messages.isEmpty() -> {
                // Mensaje cuando no hay mensajes
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Esto está muy vacío, sé el primero en escribir",
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            else -> {
                // Lista de mensajes (cuando sí hay mensajes)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    reverseLayout = true
                ) {
                    items(messages.reversed()) { message ->
                        MessageBubble(message = message)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Campo de entrada de mensajes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(CardGray.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = {
                            Text(
                                text = "Escribe un mensaje...",
                                color = TextWhite.copy(alpha = 0.5f))
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {

                                                val dateFormat1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'", Locale.getDefault())
                                                val formattedDate = dateFormat1.format(Date(System.currentTimeMillis()))
                                                val dateFormat2 = SimpleDateFormat("HH:mm", Locale.getDefault())
                                                val date = dateFormat2.format(Date(System.currentTimeMillis()))


                                                val requestBody = JSONObject(
                                                    mapOf(
                                                        "id" to formattedDate,
                                                        "id_friend_emisor" to userId,
                                                        "id_friend_receptor" to friendId,
                                                        "content" to messageText,
                                                        "date" to date
                                                    )
                                                )

                                                val response = Functions.postWithHeaders(
                                                    "messages/add_message",
                                                    headers,
                                                    requestBody
                                                )

                                                Log.d("chatScreen", "Response: $response")

                                                if (response != null) {
                                                    withContext(Dispatchers.Main) {
                                                        messages = messages + Message(
                                                            id = "temp-${System.currentTimeMillis()}",
                                                            text = messageText,
                                                            timestamp = "Ahora",
                                                            sentByUser = true
                                                        )
                                                        messageText = ""
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e("ChatScreen", "Error al enviar mensaje: ${e.message}")
                                            }
                                        }
                                    }
                                }
                            )
                            {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Enviar",
                                    tint = TextWhite
                                )
                            }
                        },
                        textStyle = LocalTextStyle.current.copy(color = TextWhite)
                    )
                }
            }
        }
    }
}


@Composable
fun MessageBubble(message: Message) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = if (message.sentByUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (message.sentByUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = if (message.sentByUser) GreenMessage else CardGray.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = TextWhite,
                    fontSize = 16.sp
                )
            }
            
            Text(
                text = message.timestamp,
                color = TextWhite.copy(alpha = 0.6f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

