package eina.unizar.frontend_movil.ui.screens

import FunctionsUserId.extractUserId
import FunctionsUserId.getToken
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.functions.Functions.getWithHeaders
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.math.min
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import org.json.JSONArray
import androidx.compose.material3.TextButton
import androidx.core.graphics.toColorInt

@Composable
fun BattlePassScreen(navController: NavHostController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var userCurrentLevel by remember { mutableStateOf(1) }
    val userHasPremium = remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Estados para guardar los ítems del pase
    var freePassItems by remember { mutableStateOf<List<Reward>>(emptyList()) }
    var premiumPassItems by remember { mutableStateOf<List<Reward>>(emptyList()) }

    val token = FunctionsUserId.getToken(context)
    val userId = FunctionsUserId.extractUserId(token)

    var isClaimingReward by remember { mutableStateOf(false) }
    var claimMessage by remember { mutableStateOf<String?>(null) }

    var userExperience by remember { mutableStateOf(0) }
    var experienceToNextLevel by remember { mutableStateOf(1000) }

    fun claimReward(itemId: Int) {
        if (isClaimingReward) return // Evitar múltiples solicitudes

        coroutineScope.launch {
            isClaimingReward = true
            try {
                val headers = mapOf(
                    "Content-Type" to "application/json",
                    "Auth" to (token ?: "")
                )

                val bodyJson = JSONObject().apply {
                    put("itemId", itemId)
                    put("userId", userId)
                }
                Log.d("Claim", "ItemId:" + itemId)

                val response = Functions.postWithHeaders(
                    endpoint = "items/assign-item",  // Primer parámetro: endpoint
                    headers = headers,               // Tercer parámetro: cabeceras
                    jsonBody = bodyJson                // Segundo parámetro: cuerpo JSON
                )

                if (response != null) {
                    // Actualizar listas de recompensas
                    val freeItemsResponse = Functions.getWithHeaders(
                        "season-pass/season-pass/getItemsFromLevels/$userId",
                        headers
                    )
                    if (freeItemsResponse != null) {
                        freePassItems = parseFreeRewards(freeItemsResponse)
                    }

                    val premiumItemsResponse = Functions.getWithHeaders(
                        "season-pass/season-pass/getItemsFromSeasonPass/$userId/1",
                        headers
                    )
                    if (premiumItemsResponse != null) {
                        premiumPassItems = parsePremiumRewards(premiumItemsResponse)
                    }

                    claimMessage = "¡Recompensa reclamada con éxito!"
                } else {
                    claimMessage = "Error al reclamar la recompensa"
                }
            } catch (e: Exception) {
                Log.e("BattlePassScreen", "Error al reclamar recompensa: ${e.message}")
                claimMessage = "Error: ${e.message}"
            } finally {
                isClaimingReward = false
            }
        }
    }

    // Efecto para cargar todos los datos necesarios
    LaunchedEffect(userId) {
        coroutineScope.launch {
            try {
                val headers = mapOf(
                    "Content-Type" to "application/json",
                    "Auth" to (token ?: "")
                )

                // Obtener experiencia actual del usuario
                val experienceResponse = Functions.getWithHeaders(
                    "season-pass/season-pass/getUserExperience/$userId",
                    headers
                )
                if (experienceResponse != null) {
                    val jsonObject = JSONObject(experienceResponse)
                    if (jsonObject.has("experience")) {
                        userExperience = jsonObject.getInt("experience")
                    }
                }

                // 1. Obtener nivel del usuario
                val levelResponse = Functions.getWithHeaders("season-pass/season-pass/getUserLevel/$userId", headers)
                if (levelResponse != null) {
                    val jsonObject = JSONObject(levelResponse)
                    if (jsonObject.has("level")) {
                        val level = jsonObject.getInt("level")
                        userCurrentLevel = min(level, 20)
                    }
                }

                // Obtener experiencia necesaria para el siguiente nivel
                val nextLevelResponse = Functions.getWithHeaders(
                    "season-pass/season-pass/getExperienceToNextLevel/$userCurrentLevel",
                    headers
                )
                if (nextLevelResponse != null) {
                    val jsonObject = JSONObject(nextLevelResponse)
                    if (jsonObject.has("experience")) {
                        experienceToNextLevel = jsonObject.getInt("experience")
                    }
                }

                // 2. Verificar si tiene pase premium
                val hasPremiumResponse = Functions.getWithHeaders("season-pass/season-pass/hasUserSP/$userId/1", headers)
                if (hasPremiumResponse != null) {
                    val jsonObject = JSONObject(hasPremiumResponse)
                    userHasPremium.value = jsonObject.optBoolean("unlocked", false)
                }

                // 3. Obtener ítems del pase gratuito
                val freeItemsResponse = Functions.getWithHeaders("season-pass/season-pass/getItemsFromLevels/$userId", headers)
                if (freeItemsResponse != null) {
                    freePassItems = parseFreeRewards(freeItemsResponse)
                    Log.d("BattlePassScreen", "Ítems parseados del pase gratuito: ${freePassItems.size}")
                }

                // 4. Obtener ítems del pase premium
                val premiumItemsResponse = Functions.getWithHeaders("season-pass/season-pass/getItemsFromSeasonPass/$userId/1", headers)
                if (premiumItemsResponse != null) {
                    premiumPassItems = parsePremiumRewards(premiumItemsResponse)
                }

            } catch (e: Exception) {
                Log.e("BattlePassScreen", "Error al cargar datos: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    // Crear el battlePassLevels basado en los ítems cargados
    val battlePassLevels = remember(freePassItems, premiumPassItems) {
        List(20) { level ->
            val currentLevel = level + 1
            val freeItem = freePassItems.find { it.id == currentLevel } ?:
            Reward(
                name = when (currentLevel) {
                    1, 5, 10, 15, 20 -> "Monedas"
                    3, 8, 13, 18 -> "Aspecto"
                    else -> "Gemas"
                },
                iconName = "monedas",
                type = when (currentLevel) {
                    3, 8, 13, 18 -> RewardType.SKIN
                    1, 5, 10, 15, 20 -> RewardType.COINS
                    else -> RewardType.GEMS
                },
                amount = when (currentLevel) {
                    1, 5, 10, 15, 20 -> currentLevel * 50
                    else -> currentLevel * 5
                }
            )

            val premiumItem = premiumPassItems.find { it.id == currentLevel } ?:
            Reward(
                name = "Aspecto Premium",
                iconName = "aspecto_premium_$currentLevel",
                type = RewardType.SKIN
            )

            BattlePassLevel(
                level = currentLevel,
                requiredPoints = currentLevel * 100,
                freeReward = freeItem,
                premiumReward = premiumItem
            )
        }
    }

    val scrollState = rememberScrollState()
    val itemWidth = 100.dp
    val itemSpacing = 4.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF7209B7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    // ponerla de color 0xFF282032
                    tint = Color(0xFF7209B7)
                )
            }

            Text(
                text = "Pase de Temporada 1",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
                // alinear centradamente el titulo
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (isLoading) {
                Spacer(modifier = Modifier.weight(1f))
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
                .background(Color(0xFF7209B7))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
            ) {
                // 1. FILA DE ITEMS DEL PASE GRATIS
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    battlePassLevels.forEach { level ->
                        FreeRewardItem(
                            itemId = level.freeReward.itemId,
                            level = level,
                            isAvailable = level.level <= userCurrentLevel,
                            currentUserLevel = userCurrentLevel,
                            showCheckmark = level.freeReward.reclaimed,
                            modifier = Modifier
                                .width(itemWidth)
                                .padding(end = itemSpacing),
                            onClaimReward = { itemId -> claimReward(itemId) }
                        )
                    }
                }

                // Espaciado superior equilibrado
                Spacer(modifier = Modifier.height(24.dp))

                // 2. FILA DE PROGRESO Y CÍRCULOS CON NIVELES (MODIFICADA)
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    // Barra de fondo (gris)
                    Box(
                        modifier = Modifier
                            .width(itemWidth * 20 + itemSpacing * 19)
                            .height(5.dp)
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    // Barra de progreso completada
                    val totalWidth = (itemWidth * 20 + itemSpacing * 19).value
                    val progressWidth = if (userCurrentLevel > 1) {
                        // Si ya completó al menos un nivel
                        val completedSegments = userCurrentLevel - 1
                        Log.d("BattlePassScreen", "Experiencia del usuario: $userExperience, Experiencia para el siguiente nivel: $experienceToNextLevel")
                        val extraProgress = userExperience.toFloat() / experienceToNextLevel.toFloat()
                        val totalSegments = if (completedSegments >= 20) 20f
                        else completedSegments.toFloat() + extraProgress

                        (totalSegments / 20f) * totalWidth
                    } else {
                        // Si aún está en nivel 1, solo mostrar progreso parcial del nivel actual
                        (userExperience.toFloat() / experienceToNextLevel.toFloat()) *
                                ((itemWidth + itemSpacing).value)
                    }

                    Box(
                        modifier = Modifier
                            .width(progressWidth.dp)
                            .height(5.dp)
                            .align(Alignment.CenterStart)
                            .background(Color(0xFF4CAF50))
                    )

                    // Círculos sobre la línea (ahora encima de la barra de progreso)
                    Row {
                        battlePassLevels.forEachIndexed { index, level ->
                            Box(
                                modifier = Modifier
                                    .width(itemWidth)
                                    .padding(end = itemSpacing)
                                    .height(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            when {
                                                level.level == userCurrentLevel -> Color(0xFF4CAF50)
                                                level.level < userCurrentLevel -> Color(0xFF2196F3)
                                                else -> Color.Gray
                                            },
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level.level.toString(),
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Espaciado inferior equilibrado
                Spacer(modifier = Modifier.height(20.dp))

                // 3. FILA DE ITEMS DEL PASE PREMIUM
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    battlePassLevels.forEach { level ->
                        PremiumRewardItem(
                            itemId = level.premiumReward.itemId,
                            level = level,
                            isAvailable = userHasPremium.value && level.level <= userCurrentLevel,
                            currentUserLevel = userCurrentLevel,
                            hasPremium = userHasPremium.value,
                            showCheckmark = level.premiumReward.reclaimed,
                            modifier = Modifier
                                .width(itemWidth)
                                .padding(end = itemSpacing),
                            onClaimReward = { itemId -> claimReward(itemId) }
                        )
                    }
                }

                // Mostrar mensaje de reclamación
                if (claimMessage != null) {
                    AlertDialog(
                        onDismissRequest = { claimMessage = null },
                        title = { Text("Reclamación de recompensa") },
                        text = { Text(claimMessage!!) },
                        confirmButton = {
                            TextButton(onClick = { claimMessage = null }) {
                                Text("Aceptar")
                            }
                        }
                    )
                }

                // Mostrar indicador de carga durante la reclamación
                if (isClaimingReward) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}

private fun parseFreeRewards(jsonResponse: String): List<Reward> {
    val items = mutableListOf<Reward>()
    try {
        val jsonArray = JSONArray(jsonResponse)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            items.add(
                Reward(
                    itemId = item.optInt("id", 0),  // Usar id_item en lugar de id_level
                    id = item.optInt("id_level", 0),  // Usar id_level en lugar de id_item
                    name = item.optString("name", ""),
                    iconName = getNormalizedName(item.optString("name", "")),
                    type = RewardType.SKIN,  // Los items del pase gratuito listados son skins
                    unlocked = item.optBoolean("unlocked", false),
                    reclaimed = item.optBoolean("reclaimed", false)
                )
            )
        }
    } catch (e: Exception) {
        Log.e("BattlePassScreen", "Error al parsear recompensas gratuitas: ${e.message}")
    }
    return items
}

// Función correcta para parsear las recompensas premium
private fun parsePremiumRewards(jsonResponse: String): List<Reward> {
    val items = mutableListOf<Reward>()
    try {
        val jsonArray = JSONArray(jsonResponse)
        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            Log.d("BattlePassScreen", "Item premium: $item")
            items.add(
                Reward(
                    itemId = item.optInt("id", 0),  // Usar id_item en lugar de id_level
                    id = item.optInt("level_required", 0),
                    name = item.optString("name", ""),
                    iconName = getNormalizedName(item.optString("name", "")),
                    type = RewardType.SKIN,  // Los items premium son todos skins
                    unlocked = item.optBoolean("unlocked", false),
                    reclaimed = item.optBoolean("reclaimed", false)
                )
            )
        }
    } catch (e: Exception) {
        Log.e("BattlePassScreen", "Error al parsear recompensas premium: ${e.message}")
    }
    return items
}

@Composable
fun PremiumRewardItem(
    itemId: Int,
    level: BattlePassLevel,
    isAvailable: Boolean,
    currentUserLevel: Int,
    hasPremium: Boolean,
    showCheckmark: Boolean = false,
    modifier: Modifier = Modifier,
    onClaimReward: (Int) -> Unit = {}  // Nueva función para reclamar
) {
    val context = LocalContext.current

    // Determinar el color de fondo según el estado
    val backgroundColor = when {
        level.premiumReward.unlocked && !level.premiumReward.reclaimed -> Color(0xFFFFD700) // Amarillo para reclamable
        showCheckmark -> Color(0xFF2196F3) // Azul claro para reclamado
        else -> Color(0xFF37474F) // Color normal
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .size(80.dp)
            // Añadir clickable solo si se puede reclamar
            .let { mod ->
                if (level.premiumReward.unlocked && !level.premiumReward.reclaimed && hasPremium)
                    mod.clickable { onClaimReward(itemId) }
                else mod
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val resourceName = getNormalizedName(level.premiumReward.name)
            val resourceId = context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )

            if (resourceId != 0) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = resourceId),
                    contentDescription = level.premiumReward.name,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = if (isAvailable && hasPremium) Color.White else Color.Gray,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = level.premiumReward.name,
                color = if (isAvailable && hasPremium) Color.White else Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Candado para usuarios sin pase premium
        if (!hasPremium) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Bloqueado",
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
            )
        }

        // Checkmark para ítems reclamados
        if (showCheckmark) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Reclamado",
                tint = Color.Green,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
fun FreeRewardItem(
    itemId: Int,
    level: BattlePassLevel,
    isAvailable: Boolean,
    currentUserLevel: Int,
    showCheckmark: Boolean = false,
    modifier: Modifier = Modifier,
    onClaimReward: (Int) -> Unit = {}  // Nueva función para reclamar
) {
    val context = LocalContext.current

    // Determinar el color de fondo según el estado
    val backgroundColor = when {
        level.freeReward.unlocked && !level.freeReward.reclaimed -> Color(0xFFFFD700) // Amarillo para reclamable
        showCheckmark -> Color(0xFF2196F3) // Azul claro para reclamado
        else -> Color(0xFF263238) // Color normal
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .size(80.dp)
            // Añadir clickable solo si se puede reclamar
            .let { mod ->
                if (level.freeReward.unlocked && !level.freeReward.reclaimed &&
                    level.freeReward.type == RewardType.SKIN && level.freeReward.name.isNotEmpty())
                    mod.clickable { onClaimReward(itemId) }
                else mod
            },
        contentAlignment = Alignment.Center
    ) {
        // Si no hay recompensa para este nivel o no es skin, mostrar caja vacía
        if (level.freeReward.type != RewardType.SKIN || level.freeReward.name.isEmpty()) {
            // Hacer invisible el box
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .alpha(0f)
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val resourceName = getNormalizedName(level.freeReward.name)
                val resourceId = context.resources.getIdentifier(
                    resourceName,
                    "drawable",
                    context.packageName
                )

                if (resourceId != 0) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = resourceId),
                        contentDescription = level.freeReward.name,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (isAvailable) Color.White else Color.Gray,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = level.freeReward.name,
                    color = if (isAvailable) Color.White else Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Checkmark para ítems reclamados
        if (showCheckmark) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Reclamado",
                tint = Color.Green,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}

// Función de utilidad para normalizar nombres
private fun getNormalizedName(name: String): String {
    return name
        .lowercase()
        .replace(Regex("[áäàâã]"), "a")
        .replace(Regex("[éëèê]"), "e")
        .replace(Regex("[íïìî]"), "i")
        .replace(Regex("[óöòôõ]"), "o")
        .replace(Regex("[úüùû]"), "u")
        .replace(Regex("[ñ]"), "gn")
        .replace(Regex("[^a-z0-9_]"), "_")
}

// Función para determinar el tipo de recompensa según el nombre
private fun getRewardType(name: String): RewardType {
    return when {
        name.contains("moneda", ignoreCase = true) -> RewardType.COINS
        name.contains("gema", ignoreCase = true) -> RewardType.GEMS
        else -> RewardType.SKIN
    }
}

data class BattlePassLevel(
    val level: Int,
    val requiredPoints: Int,
    val freeReward: Reward,
    val premiumReward: Reward
)

data class Reward(
    val itemId: Int = 0,
    val id: Int = 0,
    val name: String,
    val iconName: String,
    val type: RewardType,
    val amount: Int = 0,
    val unlocked: Boolean = false,
    val reclaimed: Boolean = false
)

enum class RewardType {
    SKIN, COINS, GEMS
}