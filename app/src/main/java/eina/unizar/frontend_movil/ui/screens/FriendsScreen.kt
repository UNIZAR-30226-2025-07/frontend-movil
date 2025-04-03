package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll




@Composable
fun FriendItem(
    name: String,
    lastSeen: String,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = name,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = lastSeen,
                    color = TextWhite.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar amigo",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun FriendsScreen(navController: NavController) {
    var showFriendRequests by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna izquierda para el título y los botones
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "AMIGOS",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Fila de botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de Solicitudes
                Button(
                    onClick = { navController.navigate("friend_requests") },
                    colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Solicitudes")
                        Badge {
                            Text("3")
                        }
                    }
                }

                // Botón de Añadir amigos
                Button(
                    onClick = { navController.navigate("add_friend") },
                    colors = ButtonDefaults.buttonColors(containerColor = CardGray.copy(alpha = 0.3f))
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Añadir amigo",
                            modifier = Modifier.size(20.dp)
                        )
                        Text("AÑADIR AMIGO")
                    }
                }
            }
        }

        // Columna derecha para la lista de amigos
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Título de la lista
            Text(
                text = "Lista de amigos",
                color = TextWhite,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista de amigos
            LazyColumn {
                items(
                    listOf(
                        "Hector" to "en línea hace 5 horas",
                        "David" to "en línea hace 8 horas",
                        "Daniel" to "en línea hace 9 horas",
                        "Hugo" to "en línea hace 13 horas"
                    )
                ) { (name, lastSeen) ->
                    FriendItem(
                        name = name,
                        lastSeen = lastSeen,
                        onRemove = { /* TODO: Implementar eliminar amigo */ }
                    )
                }
            }
        }
    }
}