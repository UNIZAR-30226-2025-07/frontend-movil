package eina.unizar.frontend_movil.ui.screens

import MainMenuScreen
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.models.Message
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivateRoomScreen(navController: NavController) {
    // estados
    var roomCode by remember { mutableStateOf("") }
    val generatedCode = remember { generateRandomCode() }
    val context = LocalContext.current
    // scroll
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SALA PRIVADA", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = TextWhite
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = PurpleBackground
                )
            )
        },
        containerColor = PurpleBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)    // respeta statusBar + navBar
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            JoinRoomSection(
                roomCode = roomCode,
                onCodeChange = { roomCode = it },
                onJoin = { joinPrivateRoom(roomCode, navController) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "─ O ─",
                color = TextWhite.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            CreateRoomSection(
                generatedCode = generatedCode,
                onCreate = { createPrivateRoom(generatedCode, navController) },
                onCopyCode = { copyToClipboard(generatedCode, context) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun JoinRoomSection(
    roomCode: String,
    onCodeChange: (String) -> Unit,
    onJoin: () -> Unit
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
            Text(
                text = "UNIRSE A SALA",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = roomCode,
                onValueChange = onCodeChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Código de sala", color = TextWhite.copy(0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = TextWhite.copy(0.5f),
                    unfocusedIndicatorColor = TextWhite.copy(0.3f),
                    cursorColor = TextWhite
                ),
                textStyle = LocalTextStyle.current.copy(color = TextWhite),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    autoCorrect = false
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onJoin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenMessage.copy(alpha = 0.8f)
                )
            ) {
                Text("UNIRSE", color = TextWhite)
            }
        }
    }
}

@Composable
private fun CreateRoomSection(
    generatedCode: String,
    onCreate: () -> Unit,
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
            Text(
                text = "CREAR NUEVA SALA",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = generatedCode,
                    color = TextWhite,
                    fontSize = 28.sp,
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
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Comparte este código con otros jugadores",
                color = TextWhite.copy(0.7f),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onCreate,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardGray.copy(alpha = 0.4f)
                )
            ) {
                Text("CREAR SALA", color = TextWhite)
            }
        }
    }
}

// ————— Funciones auxiliares —————

private fun generateRandomCode(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    return (1..6).map { chars.random() }.joinToString("")
}

private fun copyToClipboard(text: String, context: Context) {
    val clipboard = ContextCompat.getSystemService(context, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("Código sala", text))
    Toast.makeText(context, "Código copiado", Toast.LENGTH_SHORT).show()
}

private fun joinPrivateRoom(code: String, navController: NavController) {
    if (code.length == 6) {
        navController.navigate("waiting-room/$code")
    } else {
        // Mostrar error
    }
}

private fun createPrivateRoom(code: String, navController: NavController) {
    navController.navigate("waiting-room/$code?host=true")
}
