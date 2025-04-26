package eina.unizar.frontend_movil.ui.navigation

import MainMenuScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import eina.unizar.frontend_movil.ui.screens.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import eina.unizar.frontend_movil.ui.theme.*
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.viewmodel.*
import eina.unizar.frontend_movil.ui.functions.SharedPrefsUtil
import eina.unizar.frontend_movil.ui.models.Message




@Composable
fun GameTypeSelectionScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Título compacto
        Text(
            text = "NUEVA PARTIDA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Contenedor de botones ajustado
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Botón Partida Pública
            CompactGameButton(
                text = "PÚBLICA",
                icon = Icons.Default.PlayArrow,
                onClick = { navController.navigate("game") },
                color = GreenMessage.copy(alpha = 0.8f)
            )

            // Botón Unirse a Sala
            CompactGameButton(
                text = "UNIRSE A SALA",
                icon = Icons.Default.PlayArrow,
                onClick = { navController.navigate("join-private-room") },
                color = CardGray.copy(alpha = 0.4f)
            )

            // Botón Crear Sala
            CompactGameButton(
                text = "CREAR SALA",
                icon = Icons.Default.PlayArrow,
                onClick = { navController.navigate("create-private-room") },
                color = CardGray.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun CompactGameButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp, max = 56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextWhite,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = TextWhite,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}