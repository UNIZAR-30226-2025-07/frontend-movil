package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.ui.functions.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll


@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF282032)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 40.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recuperar Contraseña",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                if (!emailSent) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Correo Electrónico") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = Color.White,
                            focusedBorderColor = Color(0xFF7209B7),
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color(0xFF7209B7),
                            unfocusedLabelColor = Color.Gray
                        )
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (email.isEmpty()) {
                                errorMessage = "Por favor, introduce tu correo electrónico"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = ""

                            coroutineScope.launch {
                                val result = requestPasswordReset(email)
                                isLoading = false

                                if (result) {
                                    emailSent = true
                                } else {
                                    errorMessage = "No se pudo enviar el correo. Verifica tu dirección"
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7209B7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(vertical = 8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Enviar Correo", color = Color.White)
                        }
                    }
                    // En la parte del else (cuando emailSent es true)
                } else {
                    // Mensaje de confirmación con scroll
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .verticalScroll(rememberScrollState()) // Añadir scroll vertical
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = "¡Correo enviado!",
                            color = Color.Green,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Hemos enviado instrucciones para recuperar tu contraseña a:",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = email,
                            color = Color(0xFF7209B7),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Por favor, revisa tu bandeja de entrada y sigue las instrucciones del correo.",
                            color = Color.White,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                navController.navigate("login_screen") {
                                    popUpTo("login_screen") { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF7209B7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Volver al Login", color = Color.White)
                        }
                    }
                }

                if (!emailSent) {
                    TextButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(
                            text = "Volver",
                            color = Color(0xFF7209B7),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

suspend fun requestPasswordReset(email: String): Boolean {
    return try {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)

        val response = Functions.postWithBody(
            endpoint = "auth/forgot-password",
            body = jsonObject.toString()
        )

        android.util.Log.d("ForgotPassword", "Respuesta: $response")

        response != null
    } catch (e: Exception) {
        android.util.Log.e("ForgotPassword", "Error solicitando reset: ${e.message}")
        e.printStackTrace()
        false
    }
}

suspend fun resetPassword(email: String, code: String, newPassword: String): Boolean {
    return try {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)
        jsonObject.put("code", code)
        jsonObject.put("newPassword", newPassword)

        // Ajustar el endpoint si es necesario según la API
        val response = Functions.postWithBody(
            endpoint = "auth/reset-password",
            body = jsonObject.toString()
        )

        android.util.Log.d("ForgotPassword", "Respuesta reset: $response")

        response != null
    } catch (e: Exception) {
        android.util.Log.e("ForgotPassword", "Error restableciendo contraseña: ${e.message}")
        e.printStackTrace()
        false
    }
}