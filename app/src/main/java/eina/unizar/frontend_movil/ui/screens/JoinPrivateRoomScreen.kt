package eina.unizar.frontend_movil.ui.screens

import MainMenuScreen
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.models.Message
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinPrivateRoomScreen(navController: NavController) {
    var roomCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var savedGames by remember { mutableStateOf<List<PrivateGame>>(emptyList()) }
    var isLoadingGames by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Cargar partidas guardadas al iniciar la pantalla
    LaunchedEffect(key1 = Unit) {
        fetchSavedGames(context) { games ->
            savedGames = games
            isLoadingGames = false
        }
    }

    // Diálogo de carga
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Buscando sala") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = GreenMessage)
                    Text("Comprobando código de sala...")
                }
            },
            confirmButton = { }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UNIRSE A SALA", color = TextWhite) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = PurpleBackground
                )
            )
        },
        containerColor = PurpleBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = roomCode,
                        onValueChange = { roomCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Código de sala", color = TextWhite.copy(0.5f)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = TextWhite.copy(0.5f),
                            unfocusedIndicatorColor = TextWhite.copy(0.3f),
                            cursorColor = TextWhite
                        ),
                        textStyle = LocalTextStyle.current.copy(color = TextWhite),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            autoCorrect = false
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (roomCode.isNotEmpty()) {
                                joinPrivateGame(context, roomCode, navController) { success, message ->
                                    isLoading = false
                                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                }
                                isLoading = true
                            } else {
                                Toast.makeText(context, "Ingresa un código de sala", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenMessage.copy(alpha = 0.8f)
                        )
                    ) {
                        Text("UNIRSE A LA SALA", color = TextWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sección de partidas guardadas
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        "TUS PARTIDAS PRIVADAS",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (isLoadingGames) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = GreenMessage)
                        }
                    } else if (savedGames.isEmpty()) {
                        Text(
                            "No tienes partidas privadas guardadas",
                            color = TextWhite.copy(alpha = 0.7f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    } else {
                        // Lista de partidas guardadas
                        savedGames.forEach { game ->
                            SavedGameItem(
                                game = game,
                                onPlayClick = {
                                    playGame(context, game, navController)
                                },
                                onDeleteClick = {
                                    removeGame(context, game) {
                                        // Actualizar la lista después de eliminar
                                        savedGames = savedGames.filter { it.id != game.id }
                                    }
                                }
                            )
                            Divider(
                                color = Color.Gray.copy(alpha = 0.3f),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Función para unirse a una partida privada con código
private fun joinPrivateGame(
    context: Context,
    gameCode: String,
    navController: NavController,
    callback: (Boolean, String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = FunctionsUserId.getToken(context)
            val userId = FunctionsUserId.extractUserId(token)
            val userName = SharedPrefsUtil.getUserName(context)
            val skinName = SharedPrefsUtil.getSkinName(context)
            val headers = mapOf("Auth" to (token ?: ""))
            //val urlServer = SharedPrefsUtil.getServerUrl(context)

            Log.d("JoinPrivateGame", "Buscando partida con código: $gameCode")

            // Paso 1: Obtener datos de la partida con el código
            val gameResponse = Functions.getWithHeaders(
                "private/getGameWithCode/$gameCode",
                headers
            )

            if (gameResponse == null) {
                withContext(Dispatchers.Main) {
                    callback(false, "No se encontró ninguna partida con ese código")
                }
                return@launch
            }

            val gameData = JSONObject(gameResponse)
            Log.d("JoinPrivateGame", "Partida encontrada: $gameData")

            if (!gameData.has("game")) {
                withContext(Dispatchers.Main) {
                    callback(false, "Datos de partida incorrectos")
                }
                return@launch
            }

            val game = gameData.getJSONObject("game")
            val gameId = game.getString("id")
            val gamePasswd = game.getString("passwd")

            // Paso 2: Unirse a la partida con los datos obtenidos
            val joinBody = JSONObject().apply {
                put("gameId", gameId)
                put("passwd", gamePasswd)
                put("idUser", userId)
            }

            Log.d("JoinPrivateGame", "Intentando unirse a la partida: $joinBody")

            val joinResponse = Functions.postWithHeaders(
                "private/join",
                headers,
                joinBody
            )

            if (joinResponse == null) {
                withContext(Dispatchers.Main) {
                    callback(false, "Error al unirse a la partida")
                }
                return@launch
            }

            Log.d("JoinPrivateGame", "Unido exitosamente a la partida: $joinResponse")

            withContext(Dispatchers.Main) {
                callback(true, "Unido correctamente")
            }

            // Guardar datos de la partida en preferencias
            context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("gameId", gameId)
                .putString("gamePasswd", gamePasswd)
                .apply()

            withContext(Dispatchers.Main) {
                callback(true, "Te has unido a la sala correctamente")
                navController.navigate("create-private-room")
            }

        } catch (e: Exception) {
            Log.e("JoinPrivateGame", "Error: ${e.message}", e)
            withContext(Dispatchers.Main) {
                callback(false, "Error: ${e.message}")
            }
        }
    }
}

@Composable
fun SavedGameItem(
    game: PrivateGame,
    onPlayClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Información del juego
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono circular
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)),
                            startX = 0f,
                            endX = 40f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Detalles del juego
            Column {
                Text(
                    text = game.name,
                    color = TextWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextWhite.copy(alpha = 0.5f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${game.currentPlayers}/${game.maxPlayers} • Código: ${game.uniqueCode}",
                        color = TextWhite.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Botones de acción
        Row {
            IconButton(onClick = onPlayClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Jugar",
                    tint = Color(0xFF00CC00)
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFFFF0000)
                )
            }
        }
    }
}

// Clase de datos para representar una partida privada
data class PrivateGame(
    val id: String,
    val name: String,
    val uniqueCode: String,
    val leader: String,
    val passwd: String,
    val updatedAt: String,
    val currentPlayers: Int,
    val maxPlayers: Int
)

// Función para obtener las partidas guardadas
private fun fetchSavedGames(context: Context, callback: (List<PrivateGame>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = FunctionsUserId.getToken(context)
            val userId = FunctionsUserId.extractUserId(token)
            val headers = mapOf("Auth" to (token ?: ""))

            Log.d("SavedGames", "Obteniendo partidas privadas para el usuario: $userId")

            val response = Functions.getWithHeaders(
                "private/unfinished/$userId",
                headers
            )

            if (response == null) {
                Log.d("SavedGames", "No se encontraron partidas privadas")
                withContext(Dispatchers.Main) {
                    callback(emptyList())
                }
                return@launch
            }

            val games = parseGamesResponse(response)
            Log.d("SavedGames", "Partidas obtenidas: ${games.size}")

            withContext(Dispatchers.Main) {
                callback(games)
            }

        } catch (e: Exception) {
            Log.e("SavedGames", "Error al obtener partidas guardadas: ${e.message}", e)
            withContext(Dispatchers.Main) {
                callback(emptyList())
            }
        }
    }
}

// Función para analizar la respuesta JSON de partidas
private fun parseGamesResponse(response: String): List<PrivateGame> {
    try {
        val games = mutableListOf<PrivateGame>()
        val jsonArray = JSONArray(response)

        for (i in 0 until jsonArray.length()) {
            val gameJson = jsonArray.getJSONObject(i)
            games.add(
                PrivateGame(
                    id = gameJson.getString("id"),
                    name = gameJson.optString("name", "Partida Privada"),
                    uniqueCode = gameJson.optString("unique_code", ""),
                    leader = gameJson.optString("leader", ""),
                    passwd = gameJson.optString("passwd", ""),
                    updatedAt = gameJson.optString("updatedAt", ""),
                    currentPlayers = gameJson.optInt("currentPlayers", 0),
                    maxPlayers = gameJson.optInt("maxPlayers", 0)
                )
            )
        }

        return games
    } catch (e: Exception) {
        Log.e("SavedGames", "Error al parsear respuesta: ${e.message}", e)
        return emptyList()
    }
}

// Función para jugar una partida guardada
private fun playGame(context: Context, game: PrivateGame, navController: NavController) {
    try {
        // Guardar datos de la partida en preferencias
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("gameId", game.id)
            .putString("gamePasswd", game.passwd)
            .apply()

        Log.d("SavedGames", "Iniciando partida guardada: ${game.id}")
        Toast.makeText(context, "Entrando a la sala...", Toast.LENGTH_SHORT).show()

        // Navegar a la pantalla de sala privada
        navController.navigate("create-private-room")

    } catch (e: Exception) {
        Log.e("SavedGames", "Error al iniciar partida: ${e.message}", e)
        Toast.makeText(context, "Error al iniciar la partida", Toast.LENGTH_SHORT).show()
    }
}

// Función para eliminar una partida guardada
private fun removeGame(context: Context, game: PrivateGame, onRemoved: () -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = FunctionsUserId.getToken(context)
            val userId = FunctionsUserId.extractUserId(token) ?: ""
            val headers = mapOf("Auth" to (token ?: ""))

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Eliminando partida...", Toast.LENGTH_SHORT).show()
            }

            if (game.leader == userId) {
                // El usuario es líder, eliminar la partida completa
                Log.d("SavedGames", "Eliminando partida como líder: ${game.id}")

                val response = Functions.delete(
                    "private/delete/${game.id}",
                    "", // JSON vacío como cuerpo
                    headers
                )

                if (response != null) {
                    Log.d("SavedGames", "Partida eliminada correctamente")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Partida eliminada correctamente", Toast.LENGTH_SHORT).show()
                        onRemoved()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al eliminar la partida", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // El usuario no es líder, eliminar solo al usuario de la partida
                Log.d("SavedGames", "Eliminando usuario de la partida: ${game.id}")

                val response = Functions.delete(
                    "private/deleteUserFromPrivate/${game.id}/$userId",
                    "",
                    headers
                )

                if (response != null) {
                    Log.d("SavedGames", "Usuario eliminado de la partida correctamente")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Has salido de la partida", Toast.LENGTH_SHORT).show()
                        onRemoved()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error al salir de la partida", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        } catch (e: Exception) {
            Log.e("SavedGames", "Error al eliminar partida: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}