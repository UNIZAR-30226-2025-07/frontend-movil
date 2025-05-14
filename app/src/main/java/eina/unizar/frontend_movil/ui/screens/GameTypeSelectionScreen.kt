package eina.unizar.frontend_movil.ui.navigation

import MainMenuScreen
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eina.unizar.frontend_movil.ui.screens.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.protobuf.ByteString
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.viewmodel.*
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import eina.unizar.frontend_movil.BuildConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTypeSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Diálogo para mostrar errores
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("Aceptar")
                }
            }
        )
    }

    // Diálogo de carga
    if (isLoading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Creando partida") },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(color = GreenMessage)
                    Text("Espere por favor...")
                }
            },
            confirmButton = { }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "NUEVA PARTIDA",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PurpleBackground
                )
            )
        },
        containerColor = PurpleBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenedor de botones ajustado
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Partida Pública
                val context = LocalContext.current
                CompactGameButton(
                    text = "PÚBLICA",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        val sharedPreferencesSkin = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                        val userId = sharedPreferences.getString("PlayerID", null) ?: UUID.randomUUID().toString()
                        val userName = sharedPreferences.getString("username", "noname")
                        val rawSkinName = sharedPreferencesSkin.getString("skin", null) ?: "real_zaragoza"
                        val skinName = rawSkinName
                            .lowercase()
                            .replace(Regex("[áäàâã]"), "a")
                            .replace(Regex("[éëèê]"), "e")
                            .replace(Regex("[íïìî]"), "i")
                            .replace(Regex("[óöòôõ]"), "o")
                            .replace(Regex("[úüùû]"), "u")
                            .replace(Regex("[ñ]"), "gn")
                            .replace(" ", "_")
                        val serverUrl = "ws://10.0.2.2:4040/ws"//"ws://galaxy.t2dc.es:4440/ws"
                        val gameId = 0 // partida pública
                        val intent = android.content.Intent(context, eina.unizar.frontend_movil.cliente_movil.ui.GameActivity::class.java).apply {
                            putExtra("serverUrl", serverUrl)
                            putExtra("userName", userName)
                            putExtra("userId", userId)
                            putExtra("skinName", skinName)
                            putExtra("gameId", gameId)
                            putExtra("isLeader", false)
                            if (context !is android.app.Activity) {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        }
                        context.startActivity(intent)},
                    color = GreenMessage.copy(alpha = 0.8f)
                )

                // Botón Unirse a Sala
                CompactGameButton(
                    text = "UNIRSE A SALA",
                    icon = Icons.Default.PlayArrow,
                    onClick = { navController.navigate("join-private-room") },
                    color = CardGray.copy(alpha = 0.4f)
                )

                // Botón Crear Sala
                CompactGameButton(
                    text = "CREAR SALA",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        isLoading = true
                        createPrivateGame(context) { success, error, gameId ->
                            isLoading = false
                            if (success) {
                                navController.navigate("create-private-room")
                            } else {
                                errorMessage = error
                            }
                        }
                    },
                    color = CardGray.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
private fun CompactGameButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp, max = 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = TextWhite,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


private fun createPrivateGame(
    context: Context,
    callback: (Boolean, String?, String?) -> Unit
) {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            // Obtener token y userId
            val token = FunctionsUserId.getToken(context)
            val userId = FunctionsUserId.extractUserId(token)

            if (userId == null) {
                callback(false, "No se pudo obtener el ID del usuario", null)
                return@launch
            }

            // Generar valores aleatorios
            val passwd = generateRandomString(8)
            val name = "Partida-${generateRandomString(4)}"

            // Guardar contraseña en preferencias
            context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("gamePasswd", passwd)
                .apply()

            // Crear cuerpo de la petición
            val createBody = JSONObject().apply {
                put("passwd", passwd)
                put("maxPlayers", 8)
                put("name", name)
                put("idLeader", userId)
            }

            // Headers para las peticiones
            val headers = mapOf(
                "Content-Type" to "application/json",
                "Auth" to (token ?: "")
            )

            // Crear la partida privada
            val createResponse = Functions.postWithHeaders(
                "private/create",
                headers,
                createBody
            )

            if (createResponse == null) {
                callback(false, "Error al crear la partida", null)
                return@launch
            }

            val createJson = JSONObject(createResponse)
            //Mensaje de log
            Log.d("CreatePrivateGame", "Respuesta de creación: $createJson")
            val gameId = createJson.getString("id")

            // Guardar gameId en preferencias
            context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("gameId", gameId)
                .apply()

            Log.d("CreatePrivateGame", "Partida creada exitosamente: $createJson")

            // Cuerpo para unirse a la partida
            val joinBody = JSONObject().apply {
                put("gameId", gameId)
                put("passwd", passwd)
                put("idUser", userId)
            }

            // Unirse a la partida privada
            val joinResponse = Functions.postWithHeaders(
                "private/join",
                headers,
                joinBody
            )

            if (joinResponse == null) {
                callback(false, "Error al unirse a la partida", null)
                return@launch
            }

            Log.d("CreatePrivateGame", "Unido a la partida exitosamente: $joinResponse")
            callback(true, null, gameId)

        } catch (e: Exception) {
            Log.e("CreatePrivateGame", "Error: ${e.message}", e)
            callback(false, "Error: ${e.message}", null)
        }
    }
}

// Función para generar cadenas aleatorias
private fun generateRandomString(length: Int): String {
    val charset = ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { charset.random() }
        .joinToString("")
}