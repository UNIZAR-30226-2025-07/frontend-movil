package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.platform.LocalContext
import eina.unizar.frontend_movil.ui.functions.Functions
import kotlinx.coroutines.launch

@Composable
fun FriendRequestItem(
    name: String,
    time: String,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
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
                Text(
                    text = name,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    color = TextWhite.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
            IconButton(
                onClick = onAccept,
                modifier = Modifier
                    .background(SuccessGreen, shape = RoundedCornerShape(50))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Aceptar solicitud",
                    tint = TextWhite
                )
            }
        }
    }
}

//@Composable
//fun FriendRequestsScreen(navController: NavController) {
//    // Estado para almacenar las solicitudes de amistad
//    var friendRequests by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
//    var loading by remember { mutableStateOf(true) }
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    // Cargar las solicitudes al iniciar la pantalla
//    LaunchedEffect(Unit) {
//        try {
//            // Obtener credenciales
//            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
//            val authPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
//            val userId = sharedPreferences.getString("userId", null)
//            val token = authPreferences.getString("access_token", null)
//
//            if (userId == null || token == null) {
//                Log.e("FriendRequests", "Error: Sin credenciales")
//                loading = false
//                return@LaunchedEffect
//            }
//
//            // Preparar headers
//            val headers = mapOf(
//                "Content-Type" to "application/json",
//                "Auth" to token
//            )
//
//            // Obtener solicitudes de amistad pendientes
//            val response = Functions.getWithHeaders(
//                "friends/get_solicitudes/$userId",
//                headers
//            )
//
//            if (response != null) {
//                val jsonArray = org.json.JSONArray(response)
//                val fetchedRequests = mutableListOf<Pair<String, String>>()
//
//                for (i in 0 until jsonArray.length()) {
//                    val jsonObject = jsonArray.getJSONObject(i)
//
//                    // Obtener datos del remitente de la solicitud
//                    val senderId = jsonObject.getString("id_sender")
//                    val requestTime = jsonObject.getString("created_at")
//
//                    // Obtener nombre del remitente
//                    val senderResponse = Functions.getWithHeaders(
//                        "main-screen/get-username/$senderId",
//                        headers
//                    )
//
//                    val senderName = try {
//                        val senderJson = org.json.JSONObject(senderResponse)
//                        senderJson.getString("username")
//                    } catch (e: Exception) {
//                        "Usuario desconocido"
//                    }
//
//                    fetchedRequests.add(senderName to formattedTime)
//                }
//
//                friendRequests = fetchedRequests
//            }
//
//        } catch (e: Exception) {
//            Log.e("FriendRequests", "Error al cargar solicitudes: ${e.message}")
//            e.printStackTrace()
//        } finally {
//            loading = false
//        }
//    }
//    Row(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(PurpleBackground)
//            .padding(16.dp)
//            .horizontalScroll(rememberScrollState()),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        // Columna izquierda para el título
//        Column(
//            modifier = Modifier.weight(1f),
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Text(
//                text = "SOLICITUDES\nDE AMISTAD",
//                fontSize = 40.sp,
//                fontWeight = FontWeight.Bold,
//                color = TextWhite,
//                lineHeight = 45.sp,
//                modifier = Modifier
//                    .padding(vertical = 24.dp)
//                    .align(Alignment.CenterHorizontally)
//            )
//        }
//
//        // Columna derecha para la lista de solicitudes
//        Column(
//            modifier = Modifier.weight(1f),
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Text(
//                text = "Lista de solicitudes",
//                color = TextWhite,
//                fontSize = 24.sp,
//                fontWeight = FontWeight.Medium,
//                modifier = Modifier.padding(bottom = 16.dp)
//            )
//
//            LazyColumn {
//                items(
//                    listOf(
//                        "Hector" to "hace 5 horas",
//                        "David" to "hace 8 horas",
//                        "Daniel" to "hace 9 horas",
//                        "Hugo" to "hace 13 horas"
//                    )
//                ) { (name, time) ->
//                    FriendRequestItem(
//                        name = name,
//                        time = time,
//                        onAccept = { /* TODO: Implementar aceptar solicitud */ }
//                    )
//                }
//            }
//        }
//    }
//}

@Composable
fun FriendRequestsScreen(navController: NavController) {
    // Estado para almacenar las solicitudes de amistad
    var friendRequests by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Cargar las solicitudes al iniciar la pantalla
    LaunchedEffect(Unit) {
        try {
            // Obtener credenciales
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val authPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val userId = sharedPreferences.getString("userId", null)
            val token = authPreferences.getString("access_token", null)

            if (userId == null || token == null) {
                Log.e("FriendRequests", "Error: Sin credenciales")
                loading = false
                return@LaunchedEffect
            }

            // Preparar headers
            val headers = mapOf(
                "Content-Type" to "application/json",
                "Auth" to token
            )
            Log.d("FriendRequests", "Headers: $headers")
            Log.d("FriendRequests", "User ID: $userId")
            // Obtener solicitudes de amistad pendientes
            val response = Functions.getWithHeaders(
                "friends/get_solicitudes/$userId",
                headers
            )

            Log.d("FriendRequests", "Respuesta: $response")

            if (response != null) {
                val jsonArray = org.json.JSONArray(response)
                val fetchedRequests = mutableListOf<Pair<String, String>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)

                    // Determinar quién es el remitente (User1) y quién es el receptor (User2)
                    val id1 = jsonObject.getString("id_friend_1")
                    val currentUserId = userId ?: ""

                    // User1 es siempre el remitente de la solicitud según la API
                    val senderUser = jsonObject.getJSONObject("User1")
                    val senderName = senderUser.getString("username")

                    // Formatear el tiempo de la solicitud
                    val requestTime = jsonObject.getString("createdAt")
                    val formattedTime = formatRequestTime(requestTime)

                    fetchedRequests.add(senderName to formattedTime)
                }

                friendRequests = fetchedRequests
            }

        } catch (e: Exception) {
            Log.e("FriendRequests", "Error al cargar solicitudes: ${e.message}")
            e.printStackTrace()
        } finally {
            loading = false
        }
    }

    // Layout actual de la pantalla
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna izquierda para el título (mantener como está)
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SOLICITUDES\nDE AMISTAD",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                lineHeight = 45.sp,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        // Columna derecha para la lista de solicitudes
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Lista de solicitudes",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = TextWhite)
                }
            } else if (friendRequests.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes solicitudes pendientes",
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                LazyColumn {
                    items(friendRequests) { (name, time) ->
                        FriendRequestItem(
                            name = name,
                            time = time,
                            onAccept = {
                                coroutineScope.launch {
                                    try {
                                        // Obtener credenciales
                                        val sharedPreferences = context.getSharedPreferences(
                                            "user_prefs",
                                            Context.MODE_PRIVATE
                                        )
                                        val authPreferences = context.getSharedPreferences(
                                            "auth_prefs",
                                            Context.MODE_PRIVATE
                                        )
                                        val userId = sharedPreferences.getString("userId", null)
                                        val token = authPreferences.getString("access_token", null)

                                        if (userId == null || token == null) {
                                            Log.e("FriendRequests", "Error: Sin credenciales")
                                            return@launch
                                        }

                                        // Preparar headers
                                        val headers = mapOf(
                                            "Content-Type" to "application/json",
                                            "Auth" to token
                                        )

                                        // Obtener ID del remitente a partir del nombre
                                        val friendIdResponse = Functions.getWithHeaders(
                                            "main-screen/get-id/$name",
                                            headers
                                        )

                                        val friendId = try {
                                            val jsonObject = org.json.JSONObject(friendIdResponse)
                                            jsonObject.getString("id")
                                        } catch (e: Exception) {
                                            Log.e("FriendRequests", "Error al obtener ID: ${e.message}")
                                            return@launch
                                        }

                                        // Aceptar solicitud
                                        val body = """{"id": "$friendId"}"""
                                        val response = Functions.postWithBody(
                                            "friends/add_friend/$userId",
                                            body,
                                            headers
                                        )

                                        Log.d("FriendRequests", "Respuesta aceptar: $response")

                                        // Actualizar la lista quitando la solicitud aceptada
                                        friendRequests = friendRequests.filter { it.first != name }
                                    } catch (e: Exception) {
                                        Log.e("FriendRequests", "Error al aceptar solicitud: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

// Función para formatear el tiempo de la solicitud
fun formatRequestTime(isoDate: String): String {
    try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate)
        val now = System.currentTimeMillis()

        val diff = now - date.time
        return when {
            diff < 60000 -> "hace un momento"
            diff < 3600000 -> "hace ${diff / 60000} min"
            diff < 86400000 -> "hace ${diff / 3600000} h"
            else -> "hace ${diff / 86400000} días"
        }
    } catch (e: Exception) {
        return "fecha desconocida"
    }
}