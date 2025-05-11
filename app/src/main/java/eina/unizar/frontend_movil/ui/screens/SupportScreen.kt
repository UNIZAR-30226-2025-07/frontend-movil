package eina.unizar.frontend_movil.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.ui.theme.*
import android.widget.Toast
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import androidx.compose.ui.platform.LocalContext
import java.text.Normalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var tipoConsulta by remember { mutableStateOf("Seleccione una opción") }
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val tiposConsulta = listOf("Seleccione una opción", "Soporte técnico", "Denunciar jugador", "Problemas cuenta", "Sugerencias", "Otros")
    var expandedDropdown by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = PurpleBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Barra superior con botón de regreso
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = TextWhite
                    )
                }

                Text(
                    text = "Contacto de soporte",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Formulario
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email", color = TextWhite.copy(alpha = 0.7f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SliderBlue,
                            unfocusedBorderColor = TextWhite.copy(alpha = 0.5f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre", color = TextWhite.copy(alpha = 0.7f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SliderBlue,
                            unfocusedBorderColor = TextWhite.copy(alpha = 0.5f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    // Tipo de consulta (dropdown)
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        OutlinedTextField(
                            value = tipoConsulta,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de consulta", color = TextWhite.copy(alpha = 0.7f)) },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SliderBlue,
                                unfocusedBorderColor = TextWhite.copy(alpha = 0.5f),
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(CardGray)
                        ) {
                            tiposConsulta.forEach { tipo ->
                                if (tipo != "Seleccione una opción") {
                                    DropdownMenuItem(
                                        text = { Text(tipo, color = TextWhite) },
                                        onClick = {
                                            tipoConsulta = tipo
                                            expandedDropdown = false
                                        },
                                        modifier = Modifier.background(CardGray)
                                    )
                                }
                            }
                        }
                    }

                    // Título
                    OutlinedTextField(
                        value = titulo,
                        onValueChange = { titulo = it },
                        label = { Text("Título de la consulta", color = TextWhite.copy(alpha = 0.7f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SliderBlue,
                            unfocusedBorderColor = TextWhite.copy(alpha = 0.5f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción", color = TextWhite.copy(alpha = 0.7f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(bottom = 16.dp),
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SliderBlue,
                            unfocusedBorderColor = TextWhite.copy(alpha = 0.5f),
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        )
                    )

                    // Botón enviar
                    Button(
                        onClick = {
                            // Validar campos
                            when {
                                email.isEmpty() -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor, introduce tu email")
                                    }
                                }
                                !isValidEmail(email) -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor, introduce un email válido")
                                    }
                                }
                                nombre.isEmpty() -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor, introduce tu nombre")
                                    }
                                }
                                tipoConsulta == "Seleccione una opción" -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor, selecciona un tipo de consulta")
                                    }
                                }
                                titulo.isEmpty() -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor, introduce un título para la consulta")
                                    }
                                }
                                descripcion.isEmpty() -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Por favor, describe tu consulta")
                                    }
                                }
                                else -> {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val tipoFormateado = Normalizer.normalize(tipoConsulta, Normalizer.Form.NFD)
                                            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
                                            .lowercase()
                                            .replace(" ", "-")

                                        val result = sendSupportRequest(
                                            titulo,
                                            email,
                                            nombre,
                                            tipoFormateado,
                                            descripcion
                                        )

                                        isLoading = false

                                        if (result == null) {
                                            // Éxito - limpiar formulario
                                            email = ""
                                            nombre = ""
                                            tipoConsulta = "Seleccione una opción"
                                            titulo = ""
                                            descripcion = ""

                                            snackbarHostState.showSnackbar("Tu consulta ha sido enviada correctamente")
                                        } else {
                                            snackbarHostState.showSnackbar("Error: $result")
                                        }
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SliderBlue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = TextWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text("Enviar consulta", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// Función para validar formato de email
private fun isValidEmail(email: String): Boolean {
    val pattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
    return email.matches(pattern.toRegex())
}

// Función de envío con timeout
suspend fun sendSupportRequest(
    title: String,
    email: String,
    name: String,
    type: String,
    description: String
): String? = withContext(Dispatchers.IO) {
    try {
        println("Enviando solicitud de soporte:")
        println("title: $title")
        println("email: $email")
        println("name: $name")
        println("type: $type")
        println("description: $description")

        val url = URL("http://galaxy.t2dc.es:3000/contact-support/new")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.connectTimeout = 15000 // 15 segundos de timeout
        conn.readTimeout = 15000
        conn.doOutput = true

        val json = JSONObject().apply {
            put("title", title)
            put("email", email)
            put("name", name)
            put("type", type)
            put("description", description)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(json.toString()) }

        val responseCode = conn.responseCode
        println("Código de respuesta: $responseCode")

        val response = try {
            conn.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            conn.errorStream?.bufferedReader()?.readText() ?: "Sin respuesta"
        }

        println("Respuesta del servidor: $response")

        if (responseCode in 200..299) {
            null // Éxito
        } else {
            "Error en la solicitud: $responseCode - $response"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error de conexión: ${e.message}"
    }
}