package eina.unizar.frontend_movil.ui.screens

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.ui.theme.PurpleBackground
import eina.unizar.frontend_movil.ui.theme.TextWhite
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import eina.unizar.frontend_movil.ui.functions.Functions

@Composable
fun ProfileSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNameField by remember { mutableStateOf(false) }
    var showEmailField by remember { mutableStateOf(false) }
    var showPasswordField by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Verificar token y obtener datos del usuario al inicio
    LaunchedEffect(Unit) {
        val userId = getUserIdFromAccessToken(context)
        logTokenInfo(context)

        userId?.let {
            val username = fetchUsername(it, context)
            name = username
            Log.d("ProfileSettings", "Nombre de usuario cargado: $username")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Ajustes de Usuario",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Mostrar mensajes de éxito o error
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            successMessage?.let {
                Text(
                    text = it,
                    color = Color.Green,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // Cambiar Nombre
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { showNameField = !showNameField },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F37C9)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cambiar nombre de usuario",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    if (showNameField) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Nuevo nombre de usuario") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Cambiar contraseña
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { showPasswordField = !showPasswordField },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF3F37C9))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cambiar contraseña",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (showPasswordField) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Nueva contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirmar contraseña") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    // Botón Guardar Cambios
                    Button(
                        onClick = {
                            // Validar y mostrar diálogo de confirmación
                            if (showPasswordField && (password.isEmpty() || password != confirmPassword)) {
                                errorMessage = "Las contraseñas no coinciden"
                                return@Button
                            }

                            if (showPasswordField && password.isNotEmpty() && !isPasswordValid(password)) {
                                errorMessage = "La contraseña debe tener al menos 7 caracteres, una mayúscula, un número y un símbolo"
                                return@Button
                            }

                            showConfirmDialog = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(
                            text = "Guardar Cambios",
                            color = TextWhite,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cerrar Sesión
                    Button(
                        onClick = {
                            // Eliminar el token de acceso
                            context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .remove("access_token")
                                .remove("refresh_token")
                                .apply()

                            // Navegar a la pantalla principal y limpiar la pila de navegación
                            navController.navigate("main_menu") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Diálogo de confirmación
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = { Text("Confirmar cambios") },
                text = {
                    Column {
                        Text("Por favor, confirma tu contraseña actual para guardar los cambios")
                        Spacer(modifier = Modifier.height(8.dp))
                        var currentPassword by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = currentPassword,
                            onValueChange = { currentPassword = it },
                            label = { Text("Contraseña actual") },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        coroutineScope.launch {
                            val userId = getUserIdFromAccessToken(context)
                            if (userId != null) {
                                // Aquí implementaríamos la lógica de verificación y actualización
                                val success = updateUserData(context, userId, name, password)
                                if (success) {
                                    successMessage = "Datos actualizados correctamente"
                                    errorMessage = null
                                } else {
                                    errorMessage = "Error al actualizar los datos"
                                }
                            }
                            showConfirmDialog = false
                        }
                    }) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    Button(onClick = { showConfirmDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

// Funciones auxiliares
fun isPasswordValid(password: String): Boolean {
    val regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{7,}$".toRegex()
    return regex.matches(password)
}

fun getUserIdFromAccessToken(context: Context): String? {
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = sharedPreferences.getString("access_token", null)

    return try {
        if (token == null) return null

        val parts = token.split(".")
        if (parts.size != 3) return null

        val payload = parts[1].padEnd((parts[1].length + 3) / 4 * 4, '=')
        val decoded = Base64.decode(payload, Base64.URL_SAFE)
        val jsonString = String(decoded)

        Log.d("TokenAuth", "Token payload: $jsonString")
        val jsonObject = JSONObject(jsonString)
        jsonObject.optString("id")
    } catch (e: Exception) {
        Log.e("TokenAuth", "Error decodificando token: ${e.message}", e)
        null
    }
}

fun logTokenInfo(context: Context) {
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val accessToken = sharedPreferences.getString("access_token", null)

    try {
        if (accessToken == null) {
            Log.w("TokenAuth", "No hay token almacenado")
            return
        }

        val parts = accessToken.split(".")
        val payload = parts[1].padEnd((parts[1].length + 3) / 4 * 4, '=')
        val decoded = Base64.decode(payload, Base64.URL_SAFE)
        val jsonString = String(decoded)
        val jsonObject = JSONObject(jsonString)

        val exp = jsonObject.optLong("exp", 0) * 1000
        val iat = jsonObject.optLong("iat", 0) * 1000
        val now = System.currentTimeMillis()

        Log.d("TokenAuth", """
            Info del Token:
            - Creado: ${Date(iat)}
            - Expira: ${Date(exp)}
            - Tiempo actual: ${Date(now)}
            - Minutos restantes: ${(exp - now) / 1000 / 60}
            - Duración total: ${(exp - iat) / 1000 / 60} minutos
        """.trimIndent())

    } catch (e: Exception) {
        Log.e("TokenAuth", "Error al leer info del token: ${e.message}")
    }
}

suspend fun updateUserData(context: Context, userId: String, username: String, password: String?): Boolean {
    return try {
        // Crear directamente un JSONObject en lugar de un mapa
        val jsonObject = JSONObject()
        jsonObject.put("username", username)

        if (!password.isNullOrBlank()) {
            val hashedPassword = hashPassword(password)
            jsonObject.put("password", hashedPassword)
        }

        val headers = mapOf(
            "Content-Type" to "application/json",
            "Auth" to (context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE).getString("access_token", "") ?: "")
        )

        val response = Functions.put(
            endpoint = "main-screen/update-user/$userId",
            body = jsonObject.toString(),
            headers = headers
        )

        response != null
    } catch (e: Exception) {
        Log.e("ProfileSettings", "Error actualizando datos: ${e.message}", e)
        false
    }
}

suspend fun fetchUsername(userId: String, context: Context): String {
    return try {
        Log.d("ProfileSettings", "Intentando obtener username para userId: $userId")

        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("access_token", null)

        val headers = mapOf(
            "Content-Type" to "application/json",
            "Auth" to (token ?: "")
        )

        val response = Functions.getWithHeaders("main-screen/get-user/$userId", headers)
        Log.d("ProfileSettings", "Respuesta: $response")

        if (response != null) {
            val jsonObject = org.json.JSONObject(response)
            jsonObject.optString("username", "Guest").also {
                Log.d("ProfileSettings", "Username obtenido: $it")
            }
        } else {
            Log.e("ProfileSettings", "Respuesta nula del servidor")
            "Guest"
        }
    } catch (e: Exception) {
        Log.e("ProfileSettings", "Error obteniendo username: ${e.message}", e)
        "Guest"
    }
}

