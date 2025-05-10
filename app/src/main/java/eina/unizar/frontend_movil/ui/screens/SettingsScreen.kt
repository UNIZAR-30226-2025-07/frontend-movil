package eina.unizar.frontend_movil.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import eina.unizar.frontend_movil.ui.utils.MusicManager
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.compose.foundation.verticalScroll

@Composable
fun SettingsScreen(navController: NavController) {
    var gameVolume by remember { mutableStateOf(0.82f) }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Text(
                text = "CONFIGURACION",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(vertical = 32.dp)
            )

            // Tarjeta principal que contiene los ajustes
            Card(
                modifier = Modifier
                    .width(400.dp)
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
                            value = MusicManager.volume.value,
                            onValueChange = { newValue ->
                                MusicManager.volume.value = newValue
                                MusicManager.mediaPlayer?.setVolume(newValue, newValue)
                            },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = SliderBlue,
                                activeTrackColor = SliderBlue
                            )
                        )
                        Text(
                            text = "${(MusicManager.volume.value * 100).toInt()}%",
                            color = TextWhite,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Enlaces legales (footer) con línea divisoria
            Divider(
                color = TextWhite.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Text(
                text = "Información legal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Enlaces en una fila horizontal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(onClick = {
                    openWebLink(context, "http://galaxy.t2dc.es:4321/cookiePolicy")
                }) {
                    Text(
                        "Política de cookies",
                        fontSize = 14.sp,
                        color = SliderBlue
                    )
                }

                TextButton(onClick = {
                    openWebLink(context, "http://galaxy.t2dc.es:4321/privacyPolicy")
                }) {
                    Text(
                        "Privacidad",
                        fontSize = 14.sp,
                        color = SliderBlue
                    )
                }

                TextButton(onClick = {
                    openWebLink(context, "http://galaxy.t2dc.es:4321/termsOfService")
                }) {
                    Text(
                        "Términos",
                        fontSize = 14.sp,
                        color = SliderBlue
                    )
                }

                TextButton(onClick = {
                    navController.navigate("support")
                }) {
                    Text(
                        "Soporte",
                        fontSize = 14.sp,
                        color = SliderBlue
                    )
                }
            }
        }
    }
}

fun openWebLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    context.startActivity(intent)
}