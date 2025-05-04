package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eina.unizar.frontend_movil.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.graphics.Color
import eina.unizar.frontend_movil.ui.functions.Functions
import android.util.Log
import kotlinx.coroutines.launch



@Composable
fun AddFriendScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
    ) {
        // Botón de volver
        IconButton(
            onClick = { navController.navigateUp() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = TextWhite
            )
        }

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Título
            Text(
                text = "AÑADIR AMIGOS",
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 48.dp)
            )

            // Campo de texto para el username
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                modifier = Modifier.fillMaxWidth()
            )

            // Botón de enviar solicitud
            Button(
                onClick = {  if (username.isBlank()) {
                    message = "Por favor, introduce un nombre de usuario"
                    return@Button
                }

                    loading = true
                    message = null

                    // Usar corrutina para la operación asíncrona
                    kotlinx.coroutines.MainScope().launch {
                        try {
                            // Obtener credenciales
                            val sharedPreferences = context.getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
                            val authPreferences = context.getSharedPreferences("auth_prefs", android.content.Context.MODE_PRIVATE)
                            val userId = sharedPreferences.getString("userId", null)
                            val token = authPreferences.getString("access_token", null)

                            if (userId == null || token == null) {
                                message = "Error de sesión. Por favor, inicia sesión de nuevo"
                                loading = false
                                return@launch
                            }

                            // Preparar headers
                            val headers = mapOf(
                                "Content-Type" to "application/json",
                                "Auth" to token
                            )

                            val response1 = Functions.getWithHeaders(
                                "main-screen/get-id/$username",
                                headers
                            )

                            val friendId = try {

                                val jsonObject = org.json.JSONObject(response1)
                                jsonObject.getString("id")
                            } catch (e: Exception) {
                                Log.e("AddFriendScreen", "Error al parsear ID: ${e.message}")
                                throw Exception("El usuario solicitado no existe")
                            }


                            val requestBody = """{"id": "$friendId"}"""
                            Log.d("AddFriendScreen", "Request Body: $requestBody")

                            val response = Functions.postWithBody(
                                "friends/add_solicitud/$userId",
                                requestBody,
                                headers
                            )

                            Log.d("AddFriendScreen", "Response: $response")

                            // Procesar respuesta
                            message = "Solicitud enviada correctamente"
                            username = ""  // Limpiar el campo
                        } catch (e: Exception) {
                            message = "Error: ${e.message}"
                            e.printStackTrace()
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardGray.copy(alpha = 0.3f)
                )
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextWhite
                    )
                } else {
                    Text("ENVIAR SOLICITUD")
                }
            }
            message?.let {
                Text(
                    text = it,
                    color = if (it.startsWith("Error")) Color.Red else Color(0xFF4CAF50),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
} 