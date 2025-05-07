package eina.unizar.frontend_movil.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.SliderBlue
import eina.unizar.frontend_movil.ui.theme.PurpleBackground
import eina.unizar.frontend_movil.ui.theme.TextWhite
import eina.unizar.frontend_movil.ui.functions.Functions
import kotlinx.coroutines.launch
import org.json.JSONObject
import android.util.Base64
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.Icon

@Composable
fun BattlePassBar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val token = FunctionsUserId.getToken(context)
    val isLoggedIn = token != null

    var userLevel by remember { mutableStateOf(1) }
    var userExperience by remember { mutableStateOf(0) }
    var experienceToNextLevel by remember { mutableStateOf(100) }

    val coroutineScope = rememberCoroutineScope()

    // Cargar datos solo si el usuario está autenticado
    if (isLoggedIn) {
        val userId = FunctionsUserId.extractUserId(token)

        LaunchedEffect(userId) {
            coroutineScope.launch {
                try {
                    val headers = mapOf(
                        "Content-Type" to "application/json",
                        "Auth" to (token ?: "")
                    )

                    // Obtener datos del pase de batalla
                    val levelResponse = Functions.getWithHeaders(
                        "season-pass/season-pass/getUserLevel/$userId",
                        headers
                    )
                    if (levelResponse != null) {
                        val jsonObject = JSONObject(levelResponse)
                        if (jsonObject.has("level")) {
                            userLevel = jsonObject.getInt("level")
                        }
                    }

                    val expResponse = Functions.getWithHeaders(
                        "season-pass/season-pass/getUserExperience/$userId",
                        headers
                    )
                    if (expResponse != null) {
                        val jsonObject = JSONObject(expResponse)
                        if (jsonObject.has("experience")) {
                            userExperience = jsonObject.getInt("experience")
                        }
                    }

                    if (userLevel > 0) {
                        val nextLevelResponse = Functions.getWithHeaders(
                            "season-pass/season-pass/getExperienceToNextLevel/$userLevel",
                            headers
                        )
                        if (nextLevelResponse != null) {
                            val jsonObject = JSONObject(nextLevelResponse)
                            if (jsonObject.has("experience")) {
                                experienceToNextLevel = jsonObject.getInt("experience")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    val finalExperienceToNextLevel = if (userLevel == 21) 4500 else experienceToNextLevel
    val progress = if (isLoggedIn) userExperience.toFloat() / finalExperienceToNextLevel else 0f

    // Efecto de desbloqueo con animación
    val animatedProgress = animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
    )

    Card(
        modifier = modifier
            .width(280.dp)
            .height(90.dp)
            .clickable { onClick() },
        backgroundColor = PurpleBackground,
        elevation = 8.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Contenido normal cuando está desbloqueado
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Indicadores de nivel actual y siguiente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isLoggedIn) "Nivel ${if (userLevel == 21) 20 else userLevel}" else "Pase de batalla",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (isLoggedIn) {
                        Text(
                            text = "Nivel ${if (userLevel >= 20) "Max" else (userLevel + 1)}",
                            color = TextWhite.copy(alpha = 0.7f),
                            fontSize = 16.sp
                        )
                    }
                }

                // Barra de progreso
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(Color.DarkGray.copy(alpha = 0.5f))
                ) {
                    if (isLoggedIn) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.value.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            SliderBlue.copy(alpha = 0.8f),
                                            SliderBlue
                                        )
                                    )
                                )
                        )

                        // Texto de progreso
                        Text(
                            text = "$userExperience/$finalExperienceToNextLevel XP",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    } else {
                        // Texto para usuarios no autenticados
                        Text(
                            text = "Iniciar sesión para desbloquear",
                            color = TextWhite,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
            }

            // Efecto de bloqueado cuando no hay sesión
            if (!isLoggedIn) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF282032).copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(32.dp)
                            .align(Alignment.Center)
                    )
                }
            }
        }
    }
}