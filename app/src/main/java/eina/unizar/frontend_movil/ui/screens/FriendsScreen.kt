package eina.unizar.frontend_movil.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.animation.animateContentSize
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.runtime.setValue
import eina.unizar.frontend_movil.ui.functions.Functions
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import kotlin.compareTo
import kotlin.toString


//@Composable
//fun FriendItem(
//    name: String,
//    lastSeen: String,
//    onRemove: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp),
//        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = name,
//                    color = TextWhite,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.Bold
//                )
//                Text(
//                    text = lastSeen,
//                    color = TextWhite.copy(alpha = 0.6f),
//                    fontSize = 14.sp
//                )
//            }
//            IconButton(onClick = onRemove) {
//                Icon(
//                    imageVector = Icons.Default.Delete,
//                    contentDescription = "Eliminar amigo",
//                    tint = Color.Red
//                )
//            }
//        }
//    }
//}

@Composable
fun FriendItem(
    name: String,
    lastSeen: String,
    onRemove: () -> Unit
) {
    val isOnline = lastSeen == "En línea" || lastSeen == "Ahora mismo"
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .animateContentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {}
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline)
                if (isHovered) CardGray.copy(alpha = 0.4f) else CardGray.copy(alpha = 0.3f)
            else
                if (isHovered) CardGray.copy(alpha = 0.3f) else CardGray.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHovered) 8.dp else 4.dp)
    ) {
        Box {
            if (isOnline) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x554CAF50),
                                Color.Transparent
                            ),
                            center = Offset(size.width, 0f),
                            radius = size.width * 0.8f
                        ),
                        radius = size.width,
                        center = Offset(size.width, 0f)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Avatar con indicador de estado
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF6D6D6D),
                                            Color(0xFF4D4D4D)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.first().toString().uppercase(),
                                color = TextWhite,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Indicador de estado
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = name,
                            color = TextWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = lastSeen,
                            color = TextWhite.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x44FF0000),
                                    Color(0x22FF0000)
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar amigo",
                        tint = Color.Red.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

fun formatLastConnection(isoDate: String): String {
    try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate)
        val now = System.currentTimeMillis()

        // Verificar si es fecha futura
        if (date.time > now) {
            return "En línea"
        }

        val diff = now - date.time
        return when {
            diff < 60000 -> "Ahora mismo"
            diff < 3600000 -> "Hace ${diff / 60000} min"
            diff < 86400000 -> "Hace ${diff / 3600000} h"
            else -> "Hace ${diff / 86400000} días"
        }
    } catch (e: Exception) {
        return "Desconocido"
    }
}

@Composable
fun FriendsScreen(navController: NavController) {
    var friends by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var context = androidx.compose.ui.platform.LocalContext.current
    var pendingRequestsCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val sharedPreferences = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            val authPreferences = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("userId", null)
            val token = authPreferences.getString("access_token", null)

            val headers = mapOf(
                "Content-Type" to "application/json",
                "Auth" to (token ?: "")
            )

            val response = Functions.getWithHeaders("friends/get_friends/$userId",headers)
            if (response != null) {
                val jsonArray = org.json.JSONArray(response)
                val fetchedFriends = mutableListOf<Pair<String, String>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    // Determinar cuál usuario es el amigo (el que no es el usuario actual)
                    val currentUserId = userId ?: ""
                    val id1 = jsonObject.getString("id_friend_1")

                    // Si el id_friend_1 es igual al userId actual, entonces el amigo es User2
                    val friendUser = if (id1 == currentUserId) {
                        jsonObject.getJSONObject("User2")
                    } else {
                        jsonObject.getJSONObject("User1")
                    }

                    val friendName = friendUser.getString("username")

                    // Formatear la fecha de última conexión
                    val lastConnection = friendUser.getString("lastConnection")
                    val lastConnTime = formatLastConnection(lastConnection)
                    fetchedFriends.add(friendName to lastConnTime)
                }
                val requestsResponse = Functions.getWithHeaders(
                    "friends/get_solicitudes/$userId",
                    headers
                )

                if (requestsResponse != null) {
                    val jsonArray = org.json.JSONArray(requestsResponse)
                    pendingRequestsCount = jsonArray.length()
                }

                friends = fetchedFriends
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna izquierda
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "AMIGOS",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { navController.navigate("friend_requests") },
                    colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solicitudes")
                        if (pendingRequestsCount > 0) {
                            Badge { Text(pendingRequestsCount.toString()) }
                        }
                    }
                }

                Button(
                    onClick = { navController.navigate("add_friend") },
                    colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Añadir amigo",
                            modifier = Modifier.size(20.dp)
                        )
                        Text("AÑADIR AMIGO")
                    }
                }
            }
        }

        // Columna derecha
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Lista de amigos",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardGray.copy(alpha = 0.15f))
            ) {
                if (friends.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tienes amigos aún",
                                color = TextWhite.copy(alpha = 0.7f),
                                fontSize = 16.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                } else {
                    items(friends) { (name, lastSeen) ->
                        FriendItem(
                            name = name,
                            lastSeen = lastSeen,
                            onRemove = {
                                kotlinx.coroutines.MainScope().launch {
                                    try {
                                        // Obtener credenciales
                                        val sharedPreferences = context.getSharedPreferences(
                                            "user_prefs",
                                            android.content.Context.MODE_PRIVATE
                                        )
                                        val authPreferences = context.getSharedPreferences(
                                            "auth_prefs",
                                            android.content.Context.MODE_PRIVATE
                                        )
                                        val userId = sharedPreferences.getString("userId", null)
                                        val token = authPreferences.getString("access_token", null)

                                        if (userId == null || token == null) {
                                            Log.e("FriendsScreen", "Error: Sin credenciales")
                                            return@launch
                                        }

                                        // Preparar headers
                                        val headers = mapOf(
                                            "Content-Type" to "application/json",
                                            "Auth" to token
                                        )

                                        // Obtener ID del amigo a eliminar
                                        val friendIdResponse = Functions.getWithHeaders(
                                            "main-screen/get-id/$name",
                                            headers
                                        )

                                        val friendId = try {
                                            val jsonObject = org.json.JSONObject(friendIdResponse)
                                            jsonObject.getString("id")
                                        } catch (e: Exception) {
                                            Log.e(
                                                "FriendsScreen",
                                                "Error al obtener ID del amigo: ${e.message}"
                                            )
                                            return@launch
                                        }

                                        val body = """{"id": "$friendId"}"""
                                        // Realizar petición DELETE
                                        val response = Functions.delete(
                                            "friends/del_friend/$userId",
                                            body,
                                            headers
                                        )

                                        Log.d(
                                            "FriendsScreen",
                                            "Respuesta eliminar amigo: $response"
                                        )
                                        friends = friends.filter { it.first != name }

                                    } catch (e: Exception) {
                                        Log.e(
                                            "FriendsScreen",
                                            "Error al eliminar amigo: ${e.message}"
                                        )
                                        e.printStackTrace()
                                    }

                                }
                            }
                        )
                        if (friends.indexOf(name to lastSeen) < friends.size - 1) {
                            Divider(
                                color = TextWhite.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}