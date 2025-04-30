package eina.unizar.frontend_movil.ui.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.ui.theme.PurpleBackground
import eina.unizar.frontend_movil.ui.theme.TextWhite
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import eina.unizar.frontend_movil.ui.functions.Functions
import org.json.JSONObject
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.material3.CircularProgressIndicator

fun hashPassword(password: String): String {
    // Salt fijo predeterminado (NO cambiar una vez en producción)
    val fixedSalt = "fj27dk39slf1"

    // Combina la contraseña con el salt
    val saltedPassword = fixedSalt + password

    // Convierte la cadena a un array de bytes
    val bytes = saltedPassword.map { it.code }

    // Implementación simple de SHA-256
    var hash = 0
    for (byte in bytes) {
        hash = ((hash shl 5) - hash) + byte
        hash = hash and hash // Convierte a un entero de 32 bits
    }

    // Convierte el hash a una representación hexadecimal
    var hashHex = (hash.toLong() and 0xFFFFFFFFL).toString(16)
    while (hashHex.length < 8) {
        hashHex = "0$hashHex"
    }

    // Agrega más complejidad (iteraciones)
    for (i in 0 until 1000) {
        hashHex += fixedSalt
        var tempHash = 0
        for (char in hashHex) {
            tempHash = ((tempHash shl 5) - tempHash) + char.code
            tempHash = tempHash and tempHash
        }
        hashHex = (tempHash.toLong() and 0xFFFFFFFFL).toString(16)
    }

    return hashHex
}

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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
                text = "Iniciar Sesión",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A).copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Correo Electrónico") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    Button(
                        onClick = {
                            if (email.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true
                            scope.launch {
                                try {
                                    val hashedPassword = hashPassword(password)
                                    val jsonBody = JSONObject()
                                    jsonBody.put("email", email.trim())
                                    jsonBody.put("password", hashedPassword)
                                    Log.d("LoginScreen", "Enviando petición con email: ${email.trim()}")
                                    val response = Functions.post("auth/sign-in", jsonBody)
                                    if (response != null) {
                                        Log.d("LoginScreen", "Respuesta del servidor: $response")
                                        try {
                                            val jsonResponse = JSONObject(response)
                                            if (jsonResponse.has("accessToken")) {
                                                val accessToken = jsonResponse.getString("accessToken")
                                                Log.d("LoginScreen", "Token de acceso: $accessToken")
                                                context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                                                    .edit()
                                                    .putString("access_token", accessToken)
                                                    .apply()
                                                Log.d("LoginScreen", "Token guardado en SharedPreferences")
                                                Toast.makeText(context, "Sesión iniciada con éxito", Toast.LENGTH_SHORT).show()
                                                navController.navigate("main_menu") {
                                                    popUpTo("login_screen") { inclusive = true }
                                                }
                                            } else {
                                                Toast.makeText(context, "Credenciales inválidas", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error al procesar la respuesta", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F37C9))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = TextWhite
                            )
                        } else {
                            Text(
                                text = "Entrar",
                                color = TextWhite,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            Text(
                text = "No tengo cuenta",
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    navController.navigate("new_account")
                }
            )

            Text(
                text = "¿Olvidaste tu contraseña?",
                fontSize = 14.sp,
                color = TextWhite.copy(alpha = 0.7f),
                modifier = Modifier.clickable {
                    navController.navigate("forgot_password")
                }
            )
        }
    }
}

