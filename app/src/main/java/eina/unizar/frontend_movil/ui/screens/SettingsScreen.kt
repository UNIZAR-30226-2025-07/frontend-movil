package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.*

@Composable
fun SettingsScreen() {
    var gameVolume by remember { mutableStateOf(0.82f) }
    var musicVolume by remember { mutableStateOf(0.38f) }
    var colorBlindMode by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CONFIGURACION",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Volumen del juego
                Text(
                    text = "Volumen del juego",
                    color = TextWhite,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = gameVolume,
                        onValueChange = { gameVolume = it },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SliderBlue,
                            activeTrackColor = SliderBlue
                        )
                    )
                    Text(
                        text = "${(gameVolume * 100).toInt()}%",
                        color = TextWhite,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Volumen de la música
                Text(
                    text = "Volumen de la música",
                    color = TextWhite,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Slider(
                        value = musicVolume,
                        onValueChange = { musicVolume = it },
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = SliderBlue,
                            activeTrackColor = SliderBlue
                        )
                    )
                    Text(
                        text = "${(musicVolume * 100).toInt()}%",
                        color = TextWhite,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Modo daltónico
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modo daltónico (alternativo)",
                        color = TextWhite,
                        fontSize = 20.sp
                    )
                    Switch(
                        checked = colorBlindMode,
                        onCheckedChange = { colorBlindMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = SuccessGreen,
                            checkedTrackColor = SuccessGreen.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}
