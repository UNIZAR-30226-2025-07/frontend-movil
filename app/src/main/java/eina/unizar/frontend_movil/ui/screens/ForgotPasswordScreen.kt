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
import eina.unizar.frontend_movil.ui.functions.functions
import kotlinx.coroutines.launch
import org.json.JSONObject
import eina.unizar.frontend_movil.ui.screens.validatePassword
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var showCodeInput by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }

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

                // Formulario inicial para solicitar código
                if (!showCodeInput) {
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

                    if (successMessage.isNotEmpty()) {
                        Text(
                            text = successMessage,
                            color = Color.Green,
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
                            successMessage = ""

                            coroutineScope.launch {
                                val result = requestPasswordReset(email)
                                isLoading = false

                                if (result) {
                                    successMessage = "Código enviado a tu correo electrónico"
                                    showCodeInput = true
                                } else {
                                    errorMessage = "No se pudo enviar el código. Verifica tu correo"
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
                            Text("Enviar Código", color = Color.White)
                        }
                    }

                } else {
                    // Formulario para verificar código y cambiar contraseña
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it.trim() },
                        label = { Text("Código de Verificación") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva Contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
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

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirmar Contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
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

                    if (successMessage.isNotEmpty()) {
                        Text(
                            text = successMessage,
                            color = Color.Green,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (verificationCode.isEmpty()) {
                                errorMessage = "Por favor, introduce el código de verificación"
                                return@Button
                            }

                            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                                errorMessage = "Por favor, completa todos los campos"
                                return@Button
                            }

                            if (newPassword != confirmPassword) {
                                errorMessage = "Las contraseñas no coinciden"
                                return@Button
                            }

                            if (!validatePassword(newPassword)) {
                                errorMessage = "La contraseña debe tener al menos 7 caracteres, una mayúscula, un número y un símbolo"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = ""
                            successMessage = ""

                            coroutineScope.launch {
                                val result = resetPassword(email, verificationCode, newPassword)
                                isLoading = false

                                if (result) {
                                    successMessage = "Contraseña actualizada correctamente"
                                    // Esperar un momento y navegar a login
                                    kotlinx.coroutines.delay(2000)
                                    navController.navigate("login_screen") {
                                        popUpTo("login_screen") { inclusive = true }
                                    }
                                } else {
                                    errorMessage = "No se pudo restablecer la contraseña. Verifica el código"
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
                            Text("Restablecer Contraseña", color = Color.White)
                        }
                    }
                }

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

suspend fun requestPasswordReset(email: String): Boolean {
    return try {
        val jsonObject = JSONObject()
        jsonObject.put("email", email)

        // Usamos el endpoint correcto según el script web
        val response = functions.postWithBody(
            endpoint = "auth/forgot-password",
            body = jsonObject.toString()
        )

        // Registrar la respuesta para depuración
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
        val response = functions.postWithBody(
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