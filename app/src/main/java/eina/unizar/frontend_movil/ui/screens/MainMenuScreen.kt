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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import eina.unizar.frontend_movil.R

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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
    ) {
        // Contenedor para el logo y el botón de jugar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.galaxy),
                contentDescription = "Galaxy Logo",
                modifier = Modifier.size(200.dp)
            )

            // Botón de jugar
            Button(
                onClick = { navController.navigate("game") },
                modifier = Modifier
                    .width(200.dp)
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Jugar",
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "JUGAR",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Configuración en la esquina superior izquierda
        Button(
            onClick = { navController.navigate("settings") },
            colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f)),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    modifier = Modifier.size(20.dp)
                )
                Text("CONFIGURACIÓN")
            }
        }

        // Logros y amigos en la esquina superior derecha
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp)
        ) {
            Button(
                onClick = { navController.navigate("achievements") },
                colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Logros",
                        modifier = Modifier.size(20.dp)
                    )
                    Text("LOGROS")
                }
            }
            Button(
                onClick = { navController.navigate("friends") },
                colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f))
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Amigos",
                        modifier = Modifier.size(20.dp)
                    )
                    Text("AMIGOS")
                }
            }
        }

        // Tienda en la esquina inferior derecha
        Button(
            onClick = { navController.navigate("store") },
            colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Tienda",
                    modifier = Modifier.size(20.dp)
                )
                Text("TIENDA")
            }
        }
    }
}
