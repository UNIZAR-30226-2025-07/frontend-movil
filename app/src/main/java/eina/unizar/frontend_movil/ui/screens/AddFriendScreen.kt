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

@Composable
fun AddFriendScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }

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
                onClick = { /* TODO: Implementar envío de solicitud */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardGray.copy(alpha = 0.3f)
                )
            ) {
                Text("ENVIAR SOLICITUD")
            }
        }
    }
} 