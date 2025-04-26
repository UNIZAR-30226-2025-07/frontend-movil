import android.content.Context
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.R

@Composable
fun PlayerProgress(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.user),
            contentDescription = "Icono",
            modifier = Modifier
                .width(40.dp)
                .height(40.dp)
                .clickable {
                    val accessToken = sharedPreferences.getString("access_token", null)
                    Log.d("AccessToken", "Access Token: $accessToken")
                    if (accessToken != null) {
                        navController.navigate("profile_settings")
                    } else {
                        navController.navigate("login_screen")
                    }
                }
        )
        Spacer(modifier = Modifier.width(16.dp))
        LinearProgressIndicator(
            progress = 0.7f,
            modifier = Modifier.height(8.dp)
        )
    }
}


@Composable
fun MainMenuScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun checkTokenValidity(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val accessToken = sharedPreferences.getString("access_token", null)

        if (accessToken == null) {
            return false
        }

        try {
            val parts = accessToken.split(".")
            val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
            val jsonObject = org.json.JSONObject(payload)
            val exp = jsonObject.optLong("exp", 0) * 1000

            if (exp < System.currentTimeMillis()) {
                sharedPreferences.edit().remove("access_token").apply()
                return false
            }
            return true
        } catch (e: Exception) {
            sharedPreferences.edit().remove("access_token").apply()
            return false
        }
    }

    fun navigateWithAuth(route: String) {
        if (checkTokenValidity(context)) {
            navController.navigate(route)
        } else {
            navController.navigate("login_screen")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF282032)
    ) {
        // Animación infinita para interpolar el color de fondo del botón "JUGAR"
        val infiniteTransition = rememberInfiniteTransition()
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6000, easing = LinearEasing)
            )
        )

        // Definimos los tres colores con un toque espacial y galáctico
        val cosmicPurple = Color(0xFF7209B7)  // Púrpura intenso
        val cosmicBlue = Color(0xFF3F37C9)    // Azul profundo
        val cosmicPink = Color(0xFFF72585)    // Rosa/Magenta vibrante

        // Interpolación de colores según el valor de progress
        val animatedColor = when {
            progress < 0.33f -> lerp(cosmicPurple, cosmicBlue, progress / 0.33f)
            progress < 0.66f -> lerp(cosmicBlue, cosmicPink, (progress - 0.33f) / 0.33f)
            else -> lerp(cosmicPink, cosmicPurple, (progress - 0.66f) / 0.34f)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Columna con los demás elementos (posición superior)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PlayerProgress(navController) // Llamada a la función PlayerProgress
                // Columna que contiene dos filas con dos iconos cada una
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fila de botones (Configuración, Amigos, etc.)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de Configuración (Ajustes) usando un ícono PNG
                        Button(
                            onClick = { navController.navigate("settings") },  // Navegar a la pantalla de configuración
                            modifier = Modifier
                                .height(60.dp)
                                .width(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ajustes),
                                contentDescription = "Ajustes",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Botón de Amigos usando un ícono PNG
                        Button(
                            onClick = { navigateWithAuth("friends") },  // Navegar a la pantalla de amigos
                            modifier = Modifier
                                .height(60.dp)
                                .width(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.personas),
                                contentDescription = "Amigos",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    // Segunda fila: Tienda (izquierda) y Logros (derecha)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Botón de Tienda usando un ícono PNG
                        Button(
                            onClick = { navigateWithAuth("store") },  // Navegar a la pantalla de tienda
                            modifier = Modifier
                                .height(60.dp)
                                .width(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.carrito),
                                contentDescription = "Tienda",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        // Botón de Logros usando un ícono PNG
                        Button(
                            onClick = { navigateWithAuth("achievements") },  // Navegar a la pantalla de logros
                            modifier = Modifier
                                .height(60.dp)
                                .width(90.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.medalla),
                                contentDescription = "Logros",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // Botón de JUGAR con animación de fondo, posicionado a la esquina inferior derecha
            Button(
                onClick = { navController.navigate("play") },  // Navegar a la pantalla de juego
                colors = ButtonDefaults.buttonColors(backgroundColor = animatedColor),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .height(80.dp)
                    .width(240.dp)
            ) {
                Text("JUGAR", color = Color.White)
            }
        }
    }
}
