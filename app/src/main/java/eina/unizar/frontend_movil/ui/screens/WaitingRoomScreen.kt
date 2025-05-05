package eina.unizar.frontend_movil.ui.screens

import MainMenuScreen
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import eina.unizar.frontend_movil.ui.models.Player
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingRoomScreen(
    navController: NavController,
    roomCode: String?,
    isHost: Boolean = false
) {
    var isReady by remember { mutableStateOf(false) }
    val players = remember {
        mutableStateListOf(
            Player("Tú", isReady, "hola"),  // Jugador actual
            Player("Daniel", true, "hola"),
            Player("Hector", false, "hola"),
            Player("David", true, "hola"),
            Player("Hugo", false, "hola")
        )
    }

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
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado con estadísticas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${players.size}/8 JUGADORES",
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Text(
                    text = "${players.count { it.isReady }}/8 LISTOS",
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de jugadores
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(
                    items = players,
                    key = { player -> player.hashCode() }
                ) { player ->
                    PlayerListItem(
                        player = player,
                        isLeader = player.name == "Tú"  // Aquí corregimos el parámetro
                    )
                }
            }

            // Botón de estado listo
            Button(
                onClick = {
                    isReady = !isReady
                    // Actualizar estado del jugador actual
                    players[0] = players[0].copy(isReady = isReady)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) GreenMessage.copy(alpha = 0.8f)
                    else CardGray.copy(alpha = 0.4f)
                )
            ) {
                Text(
                    text = if (isReady) "LISTO ✓" else "MARCAR COMO LISTO",
                    color = TextWhite
                )
            }
        }
    }
}

@Composable
fun PlayerListItemB(player: Player, isLeader: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isLeader) GreenMessage else TextWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = player.name,
                    color = if (isLeader) GreenMessage else TextWhite,
                    fontSize = 16.sp
                )
            }

            if (player.isReady) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Listo",
                    tint = TextWhite,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
