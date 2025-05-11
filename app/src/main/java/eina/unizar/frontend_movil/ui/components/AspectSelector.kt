package eina.unizar.frontend_movil.ui.components

import FunctionsUserId
import android.content.Context
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import eina.unizar.frontend_movil.R
import eina.unizar.frontend_movil.ui.functions.Functions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.math.abs


data class AspectItem(
    val id: Int,
    val name: String
)

@Composable
fun AspectSelector(navController: NavController) {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    var aspects by remember { mutableStateOf(listOf<AspectItem>()) }
    var selectedIndex by remember { mutableStateOf(0) }

    val token = sharedPreferences.getString("access_token", null)

    // Cargar aspectos al iniciar el componente
    LaunchedEffect(token) {
        if (token != null) {
            loadUserAspects(context, token) { loadedAspects ->
                aspects = if (loadedAspects.isNotEmpty()) {
                    loadedAspects
                } else {
                    listOf(AspectItem(1, "Aspecto Básico"))
                }
            }
        } else {
            aspects = listOf(AspectItem(1, "Aspecto Básico"))
        }
    }

    // Guardar el aspecto seleccionado en SharedPreferences
    fun saveSkinPreference(skinName: String) {
        with(sharedPreferences.edit()) {
            putString("skin", "$skinName")
            apply()
        }
    }

    // Obtener el aspecto actual
    val currentAspect = aspects.getOrNull(selectedIndex) ?: AspectItem(1, "Aspecto Básico")

    // Actualizar la preferencia cuando cambia el índice
    LaunchedEffect(selectedIndex) {
        if (aspects.isNotEmpty()) {
            saveSkinPreference(aspects[selectedIndex].name)
        }
    }

    var animationInProgress by remember { mutableStateOf(false) }
    var animationDirection by remember { mutableStateOf(0) }
    val animatedOffset by animateFloatAsState(
        targetValue = if (animationInProgress) {
            if (animationDirection > 0) 300f else -300f
        } else 0f,
        finishedListener = {
            // Cambiar el índice solo cuando la animación termina
            if (animationInProgress) {
                selectedIndex = (selectedIndex + animationDirection + aspects.size) % aspects.size
                animationInProgress = false
            }
        }
    )

    Box(
        modifier = Modifier
            .width(220.dp)
            .height(170.dp)
            .background(Color(0xFFe1c2f1), shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .padding(20.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        // No hacemos nada aquí, la animación ya está en progreso
                    },
                    onDragStart = { /* No requerido */ },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Solo iniciar la animación si no hay una en curso
                        if (!animationInProgress && aspects.size > 1) {
                            val horizontalDrag = dragAmount.x
                            // Verificar que el deslizamiento sea suficientemente horizontal
                            if (abs(horizontalDrag) > abs(dragAmount.y) && abs(horizontalDrag) > 10f) {
                                animationDirection = if (horizontalDrag < 0) 1 else -1
                                animationInProgress = true
                            }
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Botón Anterior
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(40.dp)
                        .align(Alignment.CenterStart),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "<",
                        fontSize = 30.sp,
                        modifier = Modifier.clickable {
                            if (!animationInProgress && aspects.isNotEmpty()) {
                                animationDirection = -1
                                animationInProgress = true
                            }
                        }
                    )
                }

                // Imagen del aspecto con animación
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .offset(x = animatedOffset.dp)
                ) {
                    val imageRes = getAspectResourceId(context, currentAspect.name)
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = currentAspect.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Botón Siguiente
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(40.dp)
                        .align(Alignment.CenterEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ">",
                        fontSize = 30.sp,
                        modifier = Modifier.clickable {
                            if (!animationInProgress && aspects.isNotEmpty()) {
                                animationDirection = 1
                                animationInProgress = true
                            }
                        }
                    )
                }
            }

            // Nombre del aspecto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentAspect.name,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// Función para cargar los aspectos del usuario
private fun loadUserAspects(context: Context, token: String, onAspectsLoaded: (List<AspectItem>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val userId = FunctionsUserId.extractUserId(token)

            if (userId != null) {
                val headers = mapOf(
                    "Content-Type" to "application/json",
                    "Auth" to token
                )

                val response = Functions.getWithHeaders("items/get-all-items/$userId", headers)

                if (response != null) {
                    val userAspects = mutableListOf<AspectItem>()

                    // Procesar la respuesta JSON
                    val jsonObject = JSONObject(response)
                    val itemsArray = jsonObject.getJSONArray("items")

                    for (i in 0 until itemsArray.length()) {
                        val item = itemsArray.getJSONObject(i)
                        userAspects.add(AspectItem(
                            id = item.getInt("id"),
                            name = item.getString("name")
                        ))
                    }

                    // Asegurarse de que siempre esté el aspecto básico
                    if (!userAspects.any { it.name == "Aspecto Básico" }) {
                        userAspects.add(0, AspectItem(1, "Aspecto Básico"))
                    }

                    withContext(Dispatchers.Main) {
                        onAspectsLoaded(userAspects)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onAspectsLoaded(listOf(AspectItem(1, "Aspecto Básico")))
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onAspectsLoaded(listOf(AspectItem(1, "Aspecto Básico")))
                }
            }
        } catch (e: Exception) {
            Log.e("AspectSelector", "Error al cargar aspectos: ${e.message}")
            withContext(Dispatchers.Main) {
                onAspectsLoaded(listOf(AspectItem(1, "Aspecto Básico")))
            }
        }
    }
}

// Función para obtener el ID del recurso de imagen para un aspecto
private fun getAspectResourceId(context: Context, aspectName: String): Int {
    val normalizedName = aspectName
        .lowercase()
        .replace(Regex("[áäàâã]"), "a")
        .replace(Regex("[éëèê]"), "e")
        .replace(Regex("[íïìî]"), "i")
        .replace(Regex("[óöòôõ]"), "o")
        .replace(Regex("[úüùû]"), "u")
        .replace(Regex("[ñ]"), "gn")
        .replace(Regex("[^a-z0-9_]"), "_")

    // Intentar varias combinaciones para encontrar el recurso
    val resourceName = "aspectos_$normalizedName"
    val resourceId = context.resources.getIdentifier(resourceName, "drawable", context.packageName)

    // Si no se encuentra, intentar sin prefijo
    if (resourceId == 0) {
        val alternativeId = context.resources.getIdentifier(normalizedName, "drawable", context.packageName)
        if (alternativeId != 0) {
            return alternativeId
        }
    } else {
        return resourceId
    }

    return R.drawable.default_skin
}