package eina.unizar.frontend_movil.ui.screens

import FunctionsUserId.getToken
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.lifecycle.viewModelScope
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class Achievement(
    val id: String,
    val name: String,
    val currentValue: Int,
    val objectiveValue: Int,
    val achieved: Boolean,
    val xpReward: Int = 50,
    val type: String
)

class AchievementsViewModel : ViewModel() {
    private val _achievements = mutableStateOf<List<Achievement>>(emptyList())
    val achievements: State<List<Achievement>> = _achievements

    private val _isLoading = mutableStateOf(true)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun loadAchievements(context: Context) {
        _isLoading.value = true
        _error.value = null

        val userId = FunctionsUserId.extractUserId(FunctionsUserId.getToken(context))
        if (userId == null) {
            _error.value = "No se pudo obtener el ID de usuario"
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            try {
                val achievementsList = fetchAchievements(userId, context)
                _achievements.value = achievementsList
            } catch (e: Exception) {
                Log.e("Achievements", "Error cargando logros: ${e.message}", e)
                _error.value = "Error al cargar los logros: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun claimAchievement(achievementId: String, context: Context): Boolean {
        return try {
            val userId =
                FunctionsUserId.extractUserId(FunctionsUserId.getToken(context)) ?: return false
            val result = unlockAchievement(userId, achievementId, context)

            if (result) {
                // Actualizar la lista de logros tras reclamar
                /*
                _achievements.value = _achievements.value.map {
                    if (it.id == achievementId) it.copy(achieved = true) else it
                }
                */
                loadAchievements(context)
            }

            result
        } catch (e: Exception) {
            Log.e("Achievements", "Error reclamando logro: ${e.message}", e)
            false
        }
    }

    private suspend fun fetchAchievements(userId: String, context: Context): List<Achievement> =
        withContext(Dispatchers.IO) {
            try {
                val token = getToken(context)
                val headers = mapOf("Auth" to (token ?: ""))

                Log.d("Achievements", "Obteniendo logros para usuario: $userId")

                val response = Functions.getWithHeaders(
                    "achievements/achievements/$userId",
                    headers
                )

                if (response != null) {
                    Log.d("Achievements", "Respuesta logros: $response")

                    val jsonObject = JSONObject(response)
                    val achievementsArray = jsonObject.getJSONArray("achievements")

                    val achievements = mutableListOf<Achievement>()
                    for (i in 0 until achievementsArray.length()) {
                        val achievement = achievementsArray.getJSONObject(i)
                        achievements.add(
                            Achievement(
                                id = achievement.getString("id"),
                                name = achievement.getString("name"),
                                currentValue = achievement.getInt("current_value"),
                                objectiveValue = achievement.getInt("objective_value"),
                                achieved = achievement.getBoolean("achieved"),
                                xpReward = achievement.optInt("experience_otorgued", 50),
                                type = achievement.getString("type")
                            )
                        )
                    }
                    achievements
                } else {
                    throw Exception("No se recibió respuesta del servidor")
                }
            } catch (e: Exception) {
                Log.e("Achievements", "Error al obtener logros", e)
                throw e
            }
        }

    private suspend fun unlockAchievement(
        userId: String,
        achievementId: String,
        context: Context
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = getToken(context)
            val headers = mapOf("Auth" to (token ?: ""))

            val jsonBody = JSONObject().apply {
                put("user_id", userId)
                put("achievement_id", achievementId)
            }

            Log.d("Achievements", "Reclamando logro: $achievementId para usuario: $userId")

            val response = Functions.postWithHeaders(
                "achievements/unlock-achievement",
                headers,
                jsonBody
            )

            if (response != null) {
                Log.d("Achievements", "Respuesta reclamo: $response")
                true
            } else {
                Log.e("Achievements", "Error al reclamar logro: sin respuesta")
                false
            }
        } catch (e: Exception) {
            Log.e("Achievements", "Error al reclamar logro", e)
            false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    navController: NavController,
    viewModel: AchievementsViewModel = viewModel()
) {
    val context = LocalContext.current
    val achievements by viewModel.achievements
    val isLoading by viewModel.isLoading
    val error by viewModel.error
    val coroutineScope = rememberCoroutineScope()

    // Cargar logros al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.loadAchievements(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "LOGROS",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PurpleBackground
                )
            )
        },
        containerColor = PurpleBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = GreenMessage
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            color = TextWhite,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadAchievements(context) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenMessage)
                        ) {
                            Text("Reintentar")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(achievements) { achievement ->
                            AchievementCard(
                                achievement = achievement,
                                onClaimClick = {
                                    coroutineScope.launch {
                                        val success = viewModel.claimAchievement(achievement.id, context)
                                        if (success) {
                                            // Mostrar toast o snackbar indicando éxito
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: Achievement,
    onClaimClick: () -> Unit
) {
    val progress: Float

    if (achievement.type == "timePlayed") {
        var progreso =
            (Math.min(achievement.currentValue, achievement.objectiveValue).toFloat()) / 3600.0
        progress = (progreso / (achievement.objectiveValue.toFloat())/3600.0).toFloat()
    } else {
        var progreso = Math.min(achievement.currentValue, achievement.objectiveValue).toFloat()
        progress = progreso / achievement.objectiveValue.toFloat()
    }
    val isCompleted = achievement.currentValue >= achievement.objectiveValue

    // Definir el color amarillo para usar en el botón y texto
    val amarillo = Color(0xFFFFD700)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.achieved) LightGreenCard else DarkGreenCard
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = achievement.name,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "+${achievement.xpReward} XP",
                        color = TextWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    if (achievement.achieved) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Reclamado",
                            tint = TextWhite,
                            modifier = Modifier
                                .size(24.dp)
                                .background(SuccessGreen, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Barra de progreso
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = if (isCompleted) SuccessGreen else GreenMessage,
                    trackColor = CardGray
                )
                Text(
                    text = if (achievement.type == "timePlayed") {
                        String.format("%.1f/%.1f",
                            Math.min(achievement.currentValue / 3600.0, achievement.objectiveValue / 3600.0),
                            achievement.objectiveValue / 3600.0
                        )
                    } else {
                        "${Math.min(achievement.currentValue, achievement.objectiveValue)}/${achievement.objectiveValue}"
                    },
                    color = TextWhite.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // Botón para reclamar cuando está completo pero no reclamado
            if (isCompleted && !achievement.achieved) {
                Button(
                    onClick = onClaimClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = amarillo)
                ) {
                    Text("RECLAMAR RECOMPENSA", color = Color.Black)
                }
            } else if (achievement.achieved) {
                Text(
                    text = "Recompensa reclamada",
                    color = amarillo,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
