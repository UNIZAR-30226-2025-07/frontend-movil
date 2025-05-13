package eina.unizar.frontend_movil.ui.screens

import MainMenuScreen
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import eina.unizar.frontend_movil.ui.models.Player
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.viewmodel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import androidx.activity.compose.BackHandler
import kotlinx.coroutines.Job


// ViewModel para la sala privada
class PrivateRoomViewModel : ViewModel() {
    private val _gameCode = MutableStateFlow("")
    val gameCode: StateFlow<String> = _gameCode.asStateFlow()

    private val _maxPlayers = MutableStateFlow(8)
    val maxPlayers: StateFlow<Int> = _maxPlayers.asStateFlow()

    private val _leaderId = MutableStateFlow("")
    val leaderId: StateFlow<String> = _leaderId.asStateFlow()

    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: StateFlow<List<Player>> = _players.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _gameId = MutableStateFlow("")
    val gameId: StateFlow<String> = _gameId.asStateFlow()

    private val _gamePasswd = MutableStateFlow("")
    val gamePasswd: StateFlow<String> = _gamePasswd.asStateFlow()

    fun loadGameCredentials(context: Context) {
        val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        _gameId.value = prefs.getString("gameId", "") ?: ""
        _gamePasswd.value = prefs.getString("gamePasswd", "") ?: ""

        if (_gameId.value.isEmpty() || _gamePasswd.value.isEmpty()) {
            _error.value = "No se encontraron datos de la partida"
            _loading.value = false
        }
    }

    private var pollingJob: Job? = null

    // Estado para saber si está activo el polling
    private val _isPolling = MutableStateFlow(false)
    val isPolling: StateFlow<Boolean> = _isPolling.asStateFlow()

    fun startPolling(context: Context) {
        viewModelScope.launch {
            // Cargar datos iniciales
            loadGameData(context)

            // Actualizar cada 2 segundos
            while (true) {
                delay(2000)
                loadGameData(context)
            }
        }
    }

    fun stopPolling() {
        _isPolling.value = false
        pollingJob?.cancel()
        pollingJob = null
        Log.d("PrivateRoom", "Polling detenido")
    }

    // Asegurar que se cancele el polling cuando el ViewModel se limpie
    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }

    private suspend fun loadGameData(context: Context) {
        try {
            val token = FunctionsUserId.getToken(context)
            val headers = mapOf("Auth" to (token ?: ""))

            // Obtener información de la partida
            val gameBody = JSONObject().apply {
                put("gameId", _gameId.value)
                put("passwd", _gamePasswd.value)
            }

            val gameResponse = Functions.postWithHeaders(
                "private/getPrivateGame",
                headers,
                gameBody
            )

            if (gameResponse == null) {
                _error.value = "Error al obtener los datos de la partida"
                _loading.value = false
                return
            }

            val gameData = JSONObject(gameResponse)
            Log.d("PrivateRoom", "Datos del juego: $gameData")

            if (!gameData.has("privateGame")) {
                // Limpiar datos si la partida no existe
                context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .remove("gameId")
                    .remove("gamePasswd")
                    .apply()

                _error.value = "La partida ya no existe"
                _loading.value = false
                return
            }

            val privateGame = gameData.getJSONObject("privateGame")
            _gameCode.value = privateGame.optString("unique_code", _gameId.value)
            _maxPlayers.value = privateGame.optInt("maxPlayers", 8)
            _leaderId.value = privateGame.optString("leader", "")

            // Guardar el Id del lider en SharedPreferences
            context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("LeaderID", _leaderId.value)
                .apply()

            // Obtener jugadores en la partida
            val playersResponse = Functions.getWithHeaders(
                "private/allPlayers/${_gameId.value}",
                headers
            )

            if (playersResponse == null) {
                _error.value = "Error al obtener la lista de jugadores"
                _loading.value = false
                return
            }

            val playersData = JSONObject(playersResponse)
            Log.d("PrivateRoom", "Datos de jugadores: $playersData")

            val playersList = mutableListOf<Player>()
            if (playersData.has("players") && playersData.getJSONObject("players").has("players")) {
                val playersArray = playersData.getJSONObject("players").getJSONArray("players")

                for (i in 0 until playersArray.length()) {
                    val playerObj = playersArray.getJSONObject(i)
                    val playerId = playerObj.optString("id", "")
                    // Usar "name" como primera opción y "username" como respaldo
                    val playerName = playerObj.optString("name",
                        playerObj.optString("username", "Jugador"))
                    // Comprobar si el estado es "Ready"
                    val status = playerObj.optString("status", "")
                    val isReady = status == "Ready"

                    playersList.add(Player(id = playerId, name = playerName, isReady = isReady))
                }
            }

            // Actualizar estado de jugadores
            _players.value = playersList
            _loading.value = false

        } catch (e: Exception) {
            Log.e("PrivateRoom", "Error al cargar datos: ${e.message}", e)
            _error.value = "Error: ${e.message}"
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}

@Composable
fun PlayerListItem(player: Player, isLeader: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = player.name,
                    color = TextWhite,
                    fontSize = 16.sp
                )

                // Mostrar corona si es líder
                if (isLeader) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "👑",
                        fontSize = 16.sp
                    )
                }
            }

            if (player.isReady) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Listo",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

object ToastManager {
    private var currentToast: Toast? = null

    fun show(context: Context, message: String, duration: Int) {
        // Cancelar el Toast actual si existe
        currentToast?.cancel()

        // Crear y mostrar el nuevo Toast
        currentToast = Toast.makeText(context, message, duration).apply {
            show()
        }
    }

    fun cancelAll() {
        currentToast?.cancel()
        currentToast = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateRoomScreen(
    navController: NavController,
    viewModel: PrivateRoomViewModel = viewModel()
) {
    val context = LocalContext.current
    val token = FunctionsUserId.getToken(context)
    val scrollState = rememberScrollState()

    // Obtener userId actual
    val userId = FunctionsUserId.extractUserId(token) ?: ""

    // Observar estados del ViewModel
    val gameCode by viewModel.gameCode.collectAsState()
    val gameId by viewModel.gameId.collectAsState()
    val maxPlayers by viewModel.maxPlayers.collectAsState()
    val players by viewModel.players.collectAsState()
    val leaderId by viewModel.leaderId.collectAsState()
    val isLoading by viewModel.loading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    // Comprobar si el usuario actual es el líder
    val isUserLeader = userId == leaderId

    // Iniciar la carga de datos
    LaunchedEffect(Unit) {
        viewModel.loadGameCredentials(context)
        viewModel.startPolling(context)
    }

    // Detener polling cuando la composición se desmonte
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPolling()
            ToastManager.cancelAll()
            Log.d("PrivateRoom", "DisposableEffect: Deteniendo polling")
        }
    }

    BackHandler {
        // Lanzar una coroutina para llamar a la función suspendida
        viewModel.stopPolling()
        ToastManager.cancelAll()
        CoroutineScope(Dispatchers.Main).launch {
            handleScreenExit(context, gameId, userId, leaderId, token)
        }
        navController.navigateUp()
    }

    /*
// Efecto para manejar la salida de la pantalla (se ejecutará si el usuario sale con gesto)
    DisposableEffect(gameId, userId, leaderId) {
        onDispose {
            ToastManager.cancelAll()
            // Lanzar una coroutina para llamar a la función suspendida
            CoroutineScope(Dispatchers.Main).launch {
                handleScreenExit(context, gameId, userId, leaderId, token)
            }
        }
    }
*/
    // Diálogo de error
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearError()
                    navController.navigateUp()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SALA PRIVADA", color = TextWhite) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = PurpleBackground
                )
            )
        },
        containerColor = PurpleBackground
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenMessage)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CreateRoomSection(
                    generatedCode = gameCode.ifEmpty { gameId },
                    players = players,
                    maxPlayers = maxPlayers,
                    leaderId = leaderId,
                    isUserLeader = isUserLeader,
                    onMarkReady = {
                        markPlayerReady(context, gameId, viewModel.gamePasswd.value, userId, navController)
                    },
                    onStartGame = {
                        startGame(context, gameId, navController)
                    },
                    onCopyCode = {
                        copyToClipboard(gameCode.ifEmpty { gameId }, context)
                    }
                )
            }
        }
    }
}

private suspend fun handleScreenExit(
    context: Context,
    gameId: String,
    userId: String,
    leaderId: String,
    token: String?
) {
    // Cancelar cualquier Toast activo
    withContext(Dispatchers.Main) {
        ToastManager.cancelAll()
    }

    // Al salir de la composición, comprobamos el estado de salida controlada
    val prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
    val salidaControlada = prefs.getString("salidaControlada", "false") == "true"

    Log.d("PrivateRoom", "Saliendo de la pantalla. Salida controlada: $salidaControlada")

    if (salidaControlada) {
        // Si la salida es controlada, limpiamos las preferencias en el hilo principal
        withContext(Dispatchers.Main) {
            prefs.edit().putString("salidaControlada", "false").apply()

            // Limpiamos también los datos de la partida
            context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .edit()
                //.remove("gameId")
                .remove("gamePasswd")
                .apply()
        }

        Log.d("PrivateRoom", "Salida controlada, no se eliminará la partida")
        return
    }

    // Si no es una salida controlada, procedemos según el rol del usuario
    if (gameId.isNotBlank()) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val headers = mapOf("Auth" to (token ?: ""))

                if (userId == leaderId) {
                    // Si el usuario es líder, eliminamos la partida
                    Log.d("PrivateRoom", "Eliminando partida como líder: $gameId")
                    val response = Functions.delete(
                        "private/delete/$gameId",
                        "",
                        headers
                    )

                    Log.d("PrivateRoom", "Respuesta de eliminar partida: ${response != null}")
                } else {
                    // Si el usuario no es líder, lo eliminamos de la partida
                    Log.d("PrivateRoom", "Eliminando usuario de la partida: $gameId")
                    val response = Functions.delete(
                        "private/deleteUserFromPrivate/$gameId/$userId",
                        "",
                        headers
                    )

                    Log.d("PrivateRoom", "Respuesta de eliminar usuario: ${response != null}")
                }

                // Limpiar los datos de la partida
                withContext(Dispatchers.Main) {
                    context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .remove("gameId")
                        .remove("gamePasswd")
                        .remove("LeaderID")
                        .apply()
                }
            } catch (e: Exception) {
                Log.e("PrivateRoom", "Error al procesar salida: ${e.message}", e)
            }
        }
    }
}

@Composable
private fun CreateRoomSection(
    generatedCode: String,
    players: List<Player>,
    maxPlayers: Int,
    leaderId: String,
    isUserLeader: Boolean,
    onMarkReady: () -> Unit,
    onStartGame: () -> Unit,
    onCopyCode: () -> Unit
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
            // Fila con contador de jugadores y código de sala
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Contador de jugadores con fondo coloreado
                Card(
                    colors = CardDefaults.cardColors(containerColor = GreenMessage.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${players.size}/$maxPlayers JUGADORES",
                        color = TextWhite,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Código de sala
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = generatedCode,
                        color = TextWhite,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )
                    IconButton(onClick = onCopyCode) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Copiar código",
                            tint = TextWhite.copy(0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de jugadores
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
            ) {
                items(
                    items = players,
                    key = { player -> player.id }
                ) { player ->
                    PlayerListItem(
                        player = player,
                        isLeader = player.id == leaderId
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Agregar un log para verificar que se está haciendo clic
                    Log.d("PrivateRoom", "Botón de iniciar partida pulsado")

                    // Llamar a la función directamente si eres el líder
                    if (isUserLeader) {
                        onStartGame()
                    } else {
                        onMarkReady()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isUserLeader) CardGray.copy(alpha = 0.4f) else GreenMessage.copy(alpha = 0.4f))
            ) {
                Text(
                    if (isUserLeader) "INICIAR PARTIDA" else "LISTO",
                    color = TextWhite
                )
            }
        }
    }
}

// Función para marcar al jugador como listo y esperar enlace
private fun markPlayerReady(
    context: Context,
    gameId: String,
    gamePasswd: String,
    userId: String,
    navController: NavController
) {
    // Crear una variable para controlar la actividad del bucle
    var isActive = true

    // Variable para almacenar la referencia del job
    var job: kotlinx.coroutines.Job? = null

    // Configurar listener del ciclo de vida
    val activity = context as? androidx.activity.ComponentActivity
    activity?.lifecycle?.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onPause(owner: androidx.lifecycle.LifecycleOwner) {
            Log.d("PrivateRoom", "Actividad pausada, deteniendo bucle de consultas")
            isActive = false
            job?.cancel() // Cancelar la coroutine explícitamente
            ToastManager.cancelAll() // Cancelar cualquier Toast activo
            super.onPause(owner)
        }
    })
    job = CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = FunctionsUserId.getToken(context)
            val headers = mapOf("Auth" to (token ?: ""))

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Marcándote como listo...", Toast.LENGTH_SHORT).show()
            }

            // Primera petición: Ponerse en listo
            val readyBody = JSONObject().apply {
                put("gameId", gameId)
                put("userId", userId)
            }

            val response = Functions.postWithHeaders(
                "private/ready",
                headers,
                readyBody
            )

            if (response == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al marcarte como listo", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            Log.d("PrivateRoom", "Usuario marcado como listo correctamente")

            // Segunda petición: Esperar por el enlace
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Esperando a que el líder inicie la partida...", Toast.LENGTH_SHORT).show()
            }

            // Variables para mostrar mensajes de espera progresivos
            var waitCount = 0
            val waitMessages = listOf(
                "Esperando al resto de jugadores...",
                "Preparando la partida...",
                "Cargando el juego...",
                "Casi listo, un momento..."
            )

            // Empezar a consultar regularmente por el enlace
            var gameStarted = false
            while (!gameStarted && isActive) {
                if (waitCount % 2 == 0) {
                    val messageIndex = (waitCount / 2) % waitMessages.size
                    withContext(Dispatchers.Main) {
                        ToastManager.show(context, waitMessages[messageIndex], Toast.LENGTH_SHORT)
                    }
                }
                waitCount++

                delay(2000)
                if (!isActive) break
                try {
                    val linkResponse = Functions.getWithHeaders(
                        "private/link/$gameId",
                        headers
                    )

                    if (linkResponse != null) {
                        // La respuesta es directamente la URL, no un objeto JSON
                        val gameLink = linkResponse.trim()
                        Log.d("PrivateRoom", "Link recibido: $gameLink")

                        // Guardar el estado de salida controlada
                        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("salidaControlada", "true")
                            .apply()

                        // Redirigir a la pantalla de juego
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "¡La partida está iniciando!", Toast.LENGTH_SHORT).show()
                            Log.d("PrivateRoom", "Enlace de juego: $gameLink")

                            //val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gameLink))
                            //context.startActivity(intent)
                            navController.navigate("game")
                        }

                        gameStarted = true
                    }
                } catch (e: Exception) {
                    Log.e("PrivateRoom", "Error al consultar enlace: ${e.message}", e)
                    // Continuar consultando a pesar del error
                    delay(2000)
                }
            }

        } catch (e: Exception) {
            Log.e("PrivateRoom", "Error al marcar como listo: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private fun startGame(
    context: Context,
    gameId: String,
    navController: NavController
) {
    // Crear una variable para controlar la actividad del bucle
    var isActive = true

    // Variable para almacenar la referencia del job
    var job: kotlinx.coroutines.Job? = null

    // Configurar listener del ciclo de vida
    val activity = context as? androidx.activity.ComponentActivity
    activity?.lifecycle?.addObserver(object : androidx.lifecycle.DefaultLifecycleObserver {
        override fun onPause(owner: androidx.lifecycle.LifecycleOwner) {
            Log.d("PrivateRoom", "Actividad pausada, deteniendo bucle de consultas")
            isActive = false
            job?.cancel() // Cancelar la coroutine explícitamente
            ToastManager.cancelAll() // Cancelar cualquier Toast activo
            super.onPause(owner)
        }
    })
    Log.d("PrivateRoom", "Iniciando startGame con gameId: $gameId")
    job = CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = FunctionsUserId.getToken(context)
            val headers = mapOf("Auth" to (token ?: ""))
            val userId = FunctionsUserId.extractUserId(token) ?: ""

            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Iniciando partida...", Toast.LENGTH_SHORT).show()
            }

            // Primera petición: Iniciar la partida
            val startBody = JSONObject().apply {
                put("gameId", gameId)
                put("userId", userId)
            }

            val response = Functions.postWithHeaders(
                "private/ready",
                headers,
                startBody
            )

            if (response == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al iniciar la partida", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            Log.d("PrivateRoom", "Leader en listo correctamente")

            var waitCount = 0
            val waitMessages = listOf(
                "Preparando partida...",
                "Conectando a los jugadores...",
                "Configurando el escenario...",
                "Cargando datos de juego..."
            )

            Log.d("PrivateRoom", "Partida iniciada correctamente")

            // Consultar por el enlace en bucle hasta recibirlo
            var gameStarted = false
            while (!gameStarted && isActive) { // Añadir condición de salida
                // Mostrar mensaje de espera rotativo cada 4 segundos (cada 2 consultas)
                if (waitCount % 2 == 0 && waitCount > 0) {
                    val messageIndex = (waitCount / 2) % waitMessages.size
                    withContext(Dispatchers.Main) {
                        ToastManager.show(context, waitMessages[messageIndex], Toast.LENGTH_SHORT)
                    }
                }
                waitCount++

                delay(2000) // Esperar 2 segundos entre consultas

                if (!isActive) break

                try {
                    val linkResponse = Functions.getWithHeaders(
                        "private/link/$gameId",
                        headers
                    )

                    if (linkResponse != null) {
                        // La respuesta es directamente la URL, no un objeto JSON
                        val gameLink = linkResponse.trim()
                        Log.d("PrivateRoom", "Link recibido: $gameLink")

                        // Guardar el estado de salida controlada
                        context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("salidaControlada", "true")
                            .apply()

                        // Redirigir a la pantalla de juego
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "¡La partida está iniciando!", Toast.LENGTH_SHORT).show()
                            Log.d("PrivateRoom", "Enlace de juego: $gameLink")

                            // En lugar de lanzar la URL directamente, usar la función que maneja los enlaces de localhost
                            //openGameLink(context, gameLink)
                            navController.navigate("game")
                        }

                        gameStarted = true
                    }
                } catch (e: Exception) {
                    Log.e("PrivateRoom", "Error al consultar enlace: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("PrivateRoom", "Error al marcar como listo: ${e.message}", e)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

// Función para manejar enlaces, considerando el caso de localhost
private fun openGameLink(context: Context, gameLink: String) {
    try {
        Log.d("PrivateRoom", "Intentando abrir enlace: $gameLink")

        // Comprobar si el enlace es localhost
        if (gameLink.contains("localhost") || gameLink.contains("127.0.0.1")) {
            // Para entorno de pruebas/desarrollo, mostrar mensaje
            Toast.makeText(
                context,
                "Enlace de desarrollo detectado. En producción, este enlace abriría el juego.",
                Toast.LENGTH_LONG
            ).show()

            // También mostramos la URL para depuración
            Toast.makeText(
                context,
                "URL: $gameLink",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Para enlaces normales, intentar abrir con Intent
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(gameLink))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Verificar si hay alguna app que pueda manejar el intent
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                // Si no hay app, mostrar mensaje
                Toast.makeText(
                    context,
                    "No se encontró ninguna aplicación para abrir el enlace",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    } catch (e: Exception) {
        Log.e("PrivateRoom", "Error al abrir enlace: ${e.message}", e)
        Toast.makeText(
            context,
            "Error al abrir enlace: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    }
}

// ————— Funciones auxiliares —————

private fun copyToClipboard(text: String, context: Context) {
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("Código sala", text))
    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
}