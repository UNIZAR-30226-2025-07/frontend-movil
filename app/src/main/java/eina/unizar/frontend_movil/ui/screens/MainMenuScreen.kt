import android.content.Context
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.R
import android.content.SharedPreferences
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.components.AspectSelector
import eina.unizar.frontend_movil.ui.components.BattlePassBar
import eina.unizar.frontend_movil.ui.theme.CardGray
import eina.unizar.frontend_movil.ui.theme.SliderBlue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import kotlin.apply
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight


// Define los colores si no están definidos en el archivo de tema
private val SliderBlue = Color(0xFF3F37C9) // Color azul para elementos de UI
private val CardGray = Color(0xFF333333)   // Color gris para tarjetas y fondos


@Composable
fun PlayerProgress(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var username by remember { mutableStateOf("Guest") }
    var token by remember { mutableStateOf(sharedPreferences.getString("access_token", null)) }


    suspend fun fetchUsername(userId: String): String {
        return try {
            Log.d("PlayerProgress", "Iniciando fetchUsername para userId: $userId")
            val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
            val token = sharedPreferences.getString("access_token", null)

            val headers = mapOf(
                "Content-Type" to "application/json",
                "Auth" to (token ?: "")
            )

            Log.d("PlayerProgress", "Headers: $headers")
            val response = Functions.getWithHeaders("main-screen/get-user/$userId", headers)
            Log.d("PlayerProgress", "Respuesta: $response")

            if (response != null) {
                val jsonObject = org.json.JSONObject(response)
                jsonObject.optString("username", "Guest").also {
                    Log.d("PlayerProgress", "Username obtenido: $it")
                }
            } else {
                Log.e("PlayerProgress", "Respuesta nula del servidor")
                "Guest"
            }
        } catch (e: Exception) {
            Log.e("PlayerProgress", "Error obteniendo username: ${e.message}", e)
            "Guest"
        }
    }

    fun extractUserId(token: String?): String? {
        if (token == null) {
            Log.d("PlayerProgress", "Token es nulo")
            return null
        }

        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("PlayerProgress", "Token malformado")
                return null
            }

            // Ajustar el padding del Base64
            val payload = parts[1].padEnd((parts[1].length + 3) / 4 * 4, '=')
            val decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val jsonString = String(decoded)
            Log.d("PlayerProgress", "Payload decodificado: $jsonString")

            val jsonObject = org.json.JSONObject(jsonString)
            jsonObject.optString("id", null).also {
                Log.d("PlayerProgress", "UserId extraído: $it")
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) //Guardamos en user_prefs el user_id obtenido
                val editor = sharedPreferences.edit()
                editor.putString("userId", it)
                editor.apply()
            }
        } catch (e: Exception) {
            Log.e("PlayerProgress", "Error decodificando token: ${e.message}", e)
            null
        }
    }

    LaunchedEffect(token) {
        val userId = extractUserId(token)
        username = if (userId != null) {
            fetchUsername(userId)
        } else {
            "Guest"
        }
        val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val editor = userPrefs.edit()
        editor.putString("username", username)
        if (userId == null) {
            // Crear un nuevo uuid aleatorio
            val randomUUID = java.util.UUID.randomUUID().toString()
            editor.putString("PlayerID", randomUUID)
        } else {
            editor.putString("PlayerID", userId)
        }
        editor.apply()
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
            .edit()
            .remove("gameId")
            .apply()

    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "access_token") {
                token = sharedPreferences.getString("access_token", null)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Columna para la imagen de usuario
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Icono",
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        if (sharedPreferences.getString("access_token", null) != null) {
                            navController.navigate("profile_settings")
                        } else {
                            navController.navigate("login_screen")
                        }
                    }
            )
        }

        // Espacio entre el icono y el recuadro
        Spacer(modifier = Modifier.width(12.dp))

        // Recuadro para el nombre de usuario
        Box(
            modifier = Modifier
                .height(30.dp)
                .width(100.dp)
                .background(
                    color = Color(0xFF3F37C9).copy(alpha = 0.7f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                )
                .clickable {
                    if (sharedPreferences.getString("access_token", null) != null) {
                        navController.navigate("profile_settings")
                    } else {
                        navController.navigate("login_screen")
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Calcular tamaño de fuente dinámico basado en longitud
            val fontSize = when {
                username.length <= 7 -> 16.sp
                username.length <= 10 -> 14.sp
                username.length <= 13 -> 13.sp
                else -> 8.sp
            }

            Text(
                text = username,
                color = Color.White,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        // Espacio que separa el componente de usuario del resto
        Spacer(modifier = Modifier.width(16.dp))
    }
}


@Composable
fun MainMenuScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    var token by remember { mutableStateOf(sharedPreferences.getString("access_token", null)) }
    var showDialog by remember { mutableStateOf(false) }
    var guestUsername by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun checkTokenValidity(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)

        if (accessToken == null) {
            return false
        }

        try {
            val parts = accessToken.split(".")
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val jsonObject = org.json.JSONObject(payload)
            val exp = jsonObject.optLong("exp", 0) * 1000

            if (exp < System.currentTimeMillis()) {
                sharedPreferences.edit().remove("access_token").apply()
                return false
            }
            return true
        } catch (e: Exception) {
            sharedPreferences.edit().remove("access_token").apply()
            return false
        }
    }

    fun navigateWithAuth(route: String) {
        if (checkTokenValidity(context)) {
            navController.navigate(route)
        } else {
            navController.navigate("login_screen")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF282032)
    ) {
        // Animación infinita para interpolar el color de fondo del botón "JUGAR"
        val infiniteTransition = rememberInfiniteTransition()
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing)
            )
        )

        // Definimos los tres colores con un toque espacial y galáctico
        val cosmicPurple = Color(0xFF7209B7)  // Púrpura intenso
        val cosmicBlue = Color(0xFF3F37C9)    // Azul profundo
        val cosmicPink = Color(0xFFF72585)    // Rosa/Magenta vibrante

        // Interpolación de colores según el valor de progress
        val animatedColor = when {
            progress < 0.33f -> lerp(cosmicPurple, cosmicBlue, progress / 0.33f)
            progress < 0.66f -> lerp(cosmicBlue, cosmicPink, (progress - 0.33f) / 0.33f)
            else -> lerp(cosmicPink, cosmicPurple, (progress - 0.66f) / 0.34f)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Columna con los demás elementos (posición superior)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerProgress(navController) // Llamada a la función PlayerProgress

                // Columna que contiene dos filas con dos iconos cada una
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fila de botones (Configuración, Amigos, etc.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de Configuración (Ajustes) usando un ícono PNG
                        Button(
                            onClick = { navController.navigate("settings") },
                            modifier = Modifier
                                .height(60.dp)
                                .width(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ajustes),
                                contentDescription = "Ajustes",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Botón de Amigos usando un ícono PNG
                        Button(
                            onClick = { navigateWithAuth("friends") },
                            modifier = Modifier
                                .height(60.dp)
                                .width(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.personas),
                                contentDescription = "Amigos",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // AspectSelector en el centro de la pantalla
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                AspectSelector(navController)
            }

            // Segunda fila de iconos - Ahora en la parte inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 120.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Tienda usando un ícono PNG
                Button(
                    onClick = { navigateWithAuth("store") },
                    modifier = Modifier
                        .height(60.dp)
                        .width(90.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.carrito),
                        contentDescription = "Tienda",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Botón de Logros usando un ícono PNG
                Button(
                    onClick = { navigateWithAuth("achievements") },
                    modifier = Modifier
                        .height(60.dp)
                        .width(90.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.medalla),
                        contentDescription = "Logros",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            // Barra de progreso de nivel en la esquina inferior izquierda
            BattlePassBar(
                onClick = {
                    if (token != null) {
                        navController.navigate("battle_pass")
                    } else {
                        navController.navigate("login_screen")
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            )
            // Botón de JUGAR con animación de fondo, posicionado a la esquina inferior derecha
            Button(
                onClick = {
                    if (token == null) {
                        showDialog = true
                    } else {
                        navController.navigate("game")
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = animatedColor),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .height(80.dp)
                    .width(240.dp)
            ) {
                Text("JUGAR", color = Color.White, fontSize = 24.sp, letterSpacing = 4.sp)
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    backgroundColor = Color(0xFF282032),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    title = {
                        Text(
                            text = "JUGAR COMO INVITADO",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Mensaje de error con animación si existe
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    color = Color(0xFFF72585),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            // Campo de texto personalizado
                            OutlinedTextField(
                                value = guestUsername,
                                onValueChange = {
                                    guestUsername = it
                                    errorMessage = null  // Limpiar mensaje al escribir
                                },
                                label = { Text("Nombre de usuario") },
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.White,
                                    cursorColor = Color(0xFF3F37C9),
                                    focusedBorderColor = Color(0xFF3F37C9),
                                    unfocusedBorderColor = Color(0xFF3F37C9).copy(alpha = 0.5f),
                                    focusedLabelColor = Color(0xFF3F37C9),
                                    unfocusedLabelColor = Color(0xFF3F37C9).copy(alpha = 0.5f),
                                    backgroundColor = Color(0xFF1A1721)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    buttons = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Botón Cancelar
                            Button(
                                onClick = { showDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF282032)
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFF3F37C9).copy(alpha = 0.7f)
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(end = 8.dp)
                            ) {
                                Text("CANCELAR", color = Color.White, fontSize = 14.sp)
                            }

                            // Botón Continuar
                            Button(
                                onClick = {
                                    if (guestUsername.trim().isEmpty()) {
                                        errorMessage = "Por favor, introduce un nombre"
                                    } else if (guestUsername.length < 3) {
                                        errorMessage = "El nombre debe tener al menos 3 caracteres"
                                    } else {
                                        val userPrefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                        val editor = userPrefs.edit()
                                        editor.putString("username", guestUsername.trim())
                                        editor.apply()
                                        showDialog = false
                                        navController.navigate("play")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF3F37C9)
                                ),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .padding(start = 8.dp)
                            ) {
                                Text("CONTINUAR", color = Color.White, fontSize = 14.sp)
                            }
                        }
                    }
                )
            }
        }
    }
}

