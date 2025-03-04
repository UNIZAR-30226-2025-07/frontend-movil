package eina.unizar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.Modifier


@Composable
fun MainMenuScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Agar.io Clone", fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Button(onClick = { navController.navigate("game") }) {
            Text("Jugar")
        }

        Button(onClick = { navController.navigate("friends") }) {
            Text("Amigos")
        }

        Button(onClick = { navController.navigate("achievements") }) {
            Text("Logros")
        }

        Button(onClick = { navController.navigate("settings") }) {
            Text("Configuración")
        }
    }
}
