package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.ui.theme.*

@Composable
fun MenuButton(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 32.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = CardGray.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MainMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Galaxy",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite
        )

        Spacer(modifier = Modifier.height(32.dp))

        MenuButton(
            text = "JUGAR",
            icon = {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Jugar",
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            navController.navigate("game")
        }

        MenuButton(
            text = "AMIGOS",
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Amigos",
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            navController.navigate("friends")
        }

        MenuButton(
            text = "LOGROS",
            icon = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Logros",
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            navController.navigate("achievements")
        }

        MenuButton(
            text = "CONFIGURACIÓN",
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            navController.navigate("settings")
        }

        MenuButton(
            text = "TIENDA",
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Tienda",
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            navController.navigate("store")
        }

        MenuButton(
            text = "INICIAR SESIÓN",
            icon = {
                Icon(
                    imageVector = Icons.Default.Face,
                    contentDescription = "Iniciar sesión",
                    modifier = Modifier.size(24.dp)
                )
            }
        ) {
            navController.navigate("login")
        }
    }
}
