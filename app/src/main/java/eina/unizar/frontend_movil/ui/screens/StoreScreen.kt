package eina.unizar.frontend_movil.ui.screens

import FunctionsUserId.getToken
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.R
import eina.unizar.frontend_movil.ui.theme.*
import androidx.navigation.NavController
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import java.util.TimeZone
import eina.unizar.frontend_movil.ui.functions.Functions
import eina.unizar.frontend_movil.ui.utils.GoldColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class SkinItem(
    val id: Int,
    val name: String,
    val price: Double,
    val imageRes: Int,
    val alreadyOwned: Boolean = false
)

@Composable
fun ConfirmPurchaseDialog(
    skinName: String,
    skinImageRes: Int,
    skinPrice: Double,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text(text = "Confirmar compra", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Imagen de la skin
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(TextWhite)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = skinImageRes),
                        contentDescription = skinName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nombre de la skin
                Text(
                    text = skinName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Precio con formato
                Text(
                    text = "Precio: ${String.format("%.2f €", skinPrice)}",
                    fontSize = 16.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¿Estás seguro de que quieres realizar esta compra?",
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Pagar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onCancel
            ) {
                Text("Cancelar")
            }
        }
    )
}

// Modificar la función StoreItem para mostrar "Ya adquirido" en vez del precio
@Composable
fun StoreItem(
    skin: SkinItem,
    onBuy: (SkinItem) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth().height(240.dp),
        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth().fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(TextWhite)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = skin.imageRes),
                    contentDescription = skin.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre de la skin
            Text(
                text = skin.name,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de compra o estado de "Ya adquirido"
            if (skin.alreadyOwned) {
                // Si ya tiene la skin, mostrar texto de "Ya adquirido"
                Text(
                    text = "Ya adquirido",
                    color = SuccessGreen,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                // Si no tiene la skin, mostrar botón de compra
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "Comprar",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.2f €", skin.price),
                            fontSize = 16.sp,
                            color = TextWhite
                        )
                    }
                }
            }

            // Popup de confirmación mejorado
            if (showDialog) {
                ConfirmPurchaseDialog(
                    skinName = skin.name,
                    skinImageRes = skin.imageRes,
                    skinPrice = skin.price,
                    onConfirm = {
                        onBuy(skin)
                        showDialog = false
                    },
                    onCancel = { showDialog = false }
                )
            }
        }
    }
}

@Composable
fun SeasonPassItem(alreadyOwned: Boolean, onBuy: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .height(150.dp),
        colors = CardDefaults.cardColors(containerColor = GoldColor.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del pase de temporada
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(TextWhite),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pase_de_temporada),
                    contentDescription = "Pase de Temporada",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Información y botón
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "PASE DE TEMPORADA",
                    color = GoldColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar "Ya adquirido" o el botón de compra según corresponda
                if (alreadyOwned) {
                    Text(
                        text = "Ya adquirido",
                        color = SuccessGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                } else {
                    Button(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldColor),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Comprar",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "20,00 €",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                        }
                    }
                }
            }
        }

        // Diálogo de confirmación (sólo se muestra si no tiene el pase)
        if (showDialog && !alreadyOwned) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Confirmar compra", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Imagen del pase de temporada
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(TextWhite)
                                .align(Alignment.CenterHorizontally),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pase_de_temporada),
                                contentDescription = "Pase de Temporada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Pase de Temporada",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Precio: 20,00 €",
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "¿Estás seguro de que quieres comprar el Pase de Temporada?",
                            fontSize = 14.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onBuy()
                            showDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldColor)
                    ) {
                        Text("Pagar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showDialog = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

fun calculateTimeUntilMidnight(): Long {
    // Configurar calendario con zona horaria española (Europe/Madrid)
    val timeZone = TimeZone.getTimeZone("Europe/Madrid")
    val now = Calendar.getInstance(timeZone)
    val tomorrow = Calendar.getInstance(timeZone)

    tomorrow.add(Calendar.DATE, 1)
    tomorrow.set(Calendar.HOUR_OF_DAY, 0)
    tomorrow.set(Calendar.MINUTE, 0)
    tomorrow.set(Calendar.SECOND, 0)
    tomorrow.set(Calendar.MILLISECOND, 0)

    return tomorrow.timeInMillis - now.timeInMillis
}

fun updateShopId(context: Context): Int {
    val now = Calendar.getInstance()
    val shopId = now.get(Calendar.DAY_OF_MONTH) // Usar el día del mes como id de la tienda
    val sharedPreferences = context.getSharedPreferences("ShopPrefs", Context.MODE_PRIVATE)
    with(sharedPreferences.edit()) {
        putInt("shopId", shopId)
        putString("lastShopUpdate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(
            Date()
        ))
        apply()
    }
    return shopId
}

fun loadShopData(context: Context, onItemsLoaded: (List<SkinItem>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val shopId = updateShopId(context)
            val token = getToken(context)

            val headers = mapOf(
                "Content-Type" to "application/json",
                "Auth" to (token ?: "")
            )

            Log.d("Shop", "Headers: $headers")
            val response = Functions.getWithHeaders("shop/getItems/$shopId", headers)
            Log.d("Shop", "Respuesta: $response")

            if (response != null) {
                val items = parseShopItems(response, context)
                withContext(Dispatchers.Main) {
                    onItemsLoaded(items)
                }
            } else {
                Log.e("Shp", "Respuesta nula del servidor")
            }
        } catch (e: Exception) {
            android.util.Log.e("Shop", "Error al cargar tienda: ${e.message}", e)
        }
    }
}

private fun handlePayment(context: Context, skinItem: SkinItem, onComplete: (Boolean, String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = getToken(context) ?: ""
            val userId = FunctionsUserId.extractUserId(token)

            // Primera petición: procesar el pago
            val paymentData = JSONObject().apply {
                put("amount", skinItem.price * 100)  // Convertir a centavos
                put("currency", "EUR")
                put("paymentMethodId", "pm_card_visa")  // Método de pago predeterminado
            }

            val paymentHeaders = mapOf(
                "Content-Type" to "application/json",
                "Auth" to token
            )

            Log.d("Payment", "Procesando pago: $paymentData")
            val paymentResponse = Functions.postWithHeaders(
                "payment/pay",
                paymentHeaders,
                paymentData
            )

            if (paymentResponse != null) {
                val paymentJsonResponse = JSONObject(paymentResponse)
                val success = paymentJsonResponse.optBoolean("success", false)

                if (success) {
                    // Verificar si es el pase de temporada (ID especial 9999)
                    if (skinItem.id == 9999) {
                        // Para el pase de temporada, usar endpoint específico
                        val assignBattlePassData = JSONObject().apply {
                            put("user_id", userId)
                        }

                        Log.d("Payment", "Asignando pase de temporada: $assignBattlePassData")
                        val assignResponse = Functions.postWithHeaders(
                            "shop/purchasedSP",
                            paymentHeaders,
                            assignBattlePassData
                        )

                        if (assignResponse != null) {
                            withContext(Dispatchers.Main) {
                                onComplete(true, "¡Has adquirido el Pase de Temporada!")
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onComplete(false, "Error al asignar el pase de temporada")
                            }
                        }
                    } else {
                        // Para elementos normales, usar el endpoint habitual
                        val assignItemData = JSONObject().apply {
                            put("itemId", skinItem.id)
                            put("userId", userId)
                        }

                        Log.d("Payment", "Asignando ítem: $assignItemData")
                        val assignResponse = Functions.postWithHeaders(
                            "items/assign-item",
                            paymentHeaders,
                            assignItemData
                        )

                        if (assignResponse != null) {
                            val assignJsonResponse = JSONObject(assignResponse)
                            val assignSuccess = !assignJsonResponse.has("error")

                            withContext(Dispatchers.Main) {
                                if (assignSuccess) {
                                    onComplete(true, "¡Compra completada con éxito!")
                                } else {
                                    onComplete(false, "Error al asignar el ítem: ${assignJsonResponse.optString("error")}")
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                onComplete(false, "Error en la comunicación con el servidor")
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onComplete(false, "Error al procesar el pago: ${paymentJsonResponse.optString("error")}")
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    onComplete(false, "Error en la comunicación con el servidor")
                }
            }
        } catch (e: Exception) {
            Log.e("Payment", "Error en el proceso de pago: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onComplete(false, "Error: ${e.message}")
            }
        }
    }
}

// Función para verificar si el usuario tiene el pase de temporada
private fun checkSeasonPass(context: Context, onResult: (Boolean) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = getToken(context) ?: ""
            val userId = FunctionsUserId.extractUserId(token)

            if (userId != null) {
                val headers = mapOf(
                    "Content-Type" to "application/json",
                    "Auth" to token
                )

                Log.d("SeasonPass", "Verificando pase de temporada para usuario $userId")

                // Construir la URL completa para depurar
                val fullUrl = "http://galaxy.t2dc.es:3000/shop/hasSP/$userId"
                Log.d("SeasonPass", "URL completa: $fullUrl")

                val response = Functions.getWithHeaders("shop/hasSP/$userId", headers)

                if (response != null) {
                    // Registrar la respuesta completa para depuración
                    Log.d("SeasonPass", "Respuesta cruda: $response")

                    val jsonResponse = JSONObject(response)
                    val hasSP = jsonResponse.optBoolean("hasSeasonPass", false)

                    Log.d("SeasonPass", "Usuario tiene pase de temporada: $hasSP")

                    withContext(Dispatchers.Main) {
                        onResult(hasSP)
                    }
                } else {
                    Log.e("SeasonPass", "Respuesta nula al verificar pase de temporada")

                    // Intentar una petición directa para debug
                    try {
                        val directUrl = URL(fullUrl)
                        val connection = directUrl.openConnection() as HttpURLConnection
                        connection.setRequestProperty("Auth", token)
                        connection.setRequestProperty("Content-Type", "application/json")
                        connection.requestMethod = "GET"

                        val responseCode = connection.responseCode
                        Log.d("SeasonPass", "Intento directo: código $responseCode")

                        if (responseCode == 200) {
                            val directResponse = connection.inputStream.bufferedReader().readText()
                            Log.d("SeasonPass", "Respuesta directa: $directResponse")
                        }
                    } catch (e: Exception) {
                        Log.e("SeasonPass", "Error en intento directo: ${e.message}")
                    }

                    withContext(Dispatchers.Main) {
                        onResult(false)
                    }
                }
            } else {
                Log.e("SeasonPass", "ID de usuario nulo")
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        } catch (e: Exception) {
            Log.e("SeasonPass", "Error al verificar pase de temporada: ${e.message}", e)
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult(false)
            }
        }
    }
}


private fun parseShopItems(response: String, context: Context): List<SkinItem> {
    val items = mutableListOf<SkinItem>()
    val jsonArray = JSONArray(response)

    for (i in 0 until jsonArray.length()) {
        val jsonObject = jsonArray.getJSONObject(i)
        val name = jsonObject.getString("name_item")
        val normalizedName = name
            .lowercase()
            .replace(Regex("[áäàâã]"), "a")
            .replace(Regex("[éëèê]"), "e")
            .replace(Regex("[íïìî]"), "i")
            .replace(Regex("[óöòôõ]"), "o")
            .replace(Regex("[úüùû]"), "u")
            .replace(Regex("[ñ]"), "gn")
            .replace(Regex("[^a-z0-9_]"), "_")


        // Intenta varias combinaciones para encontrar el recurso
        var resourceId = 0

        // Primera opción: con prefijo "aspectos_"
        val resourceName1 = "aspectos_$normalizedName"
        resourceId = context.resources.getIdentifier(resourceName1, "drawable", context.packageName)
        Log.d("Shop", "Buscando imagen: $resourceName1, resultado: $resourceId")

        // Segunda opción: sin prefijo
        if (resourceId == 0) {
            resourceId = context.resources.getIdentifier(normalizedName, "drawable", context.packageName)
            Log.d("Shop", "Buscando imagen: $normalizedName, resultado: $resourceId")
        }

        // Si todo falla, usa la imagen por defecto
        if (resourceId == 0) {
            Log.d("Shop", "No se encontró imagen para: $name, usando default_skin")
            resourceId = R.drawable.default_skin
        }

        items.add(
            SkinItem(
                id = jsonObject.getInt("id_item"),
                name = name,
                price = jsonObject.getDouble("item_price"),
                imageRes = if (resourceId != 0) resourceId else R.drawable.default_skin
            )
        )
    }
    return items
}

// Función para cargar las skins que ya posee el usuario
private fun loadUserItems(context: Context, onUserItemsLoaded: (List<Int>) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = getToken(context) ?: ""
            val userId = FunctionsUserId.extractUserId(token)

            if (userId != null) {
                val headers = mapOf(
                    "Content-Type" to "application/json",
                    "Auth" to token
                )

                Log.d("UserItems", "Obteniendo items del usuario $userId")
                val response = Functions.getWithHeaders("items/get-all-items/$userId", headers)

                if (response != null) {
                    val userItemIds = mutableListOf<Int>()

                    // Corregido: La respuesta es un objeto JSON con un array "items"
                    val jsonObject = JSONObject(response)
                    val itemsArray = jsonObject.getJSONArray("items")

                    for (i in 0 until itemsArray.length()) {
                        val item = itemsArray.getJSONObject(i)
                        // Usar el campo "id" en lugar de "id_item" que es null según el log
                        userItemIds.add(item.getInt("id"))
                    }

                    Log.d("UserItems", "Items encontrados: ${userItemIds.size}")
                    withContext(Dispatchers.Main) {
                        onUserItemsLoaded(userItemIds)
                    }
                } else {
                    Log.e("UserItems", "Respuesta nula al obtener items del usuario")
                    withContext(Dispatchers.Main) {
                        onUserItemsLoaded(emptyList())
                    }
                }
            } else {
                Log.e("UserItems", "ID de usuario nulo")
                withContext(Dispatchers.Main) {
                    onUserItemsLoaded(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e("UserItems", "Error al cargar items del usuario: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onUserItemsLoaded(emptyList())
            }
        }
    }
}



@Composable
fun ConfirmPurchaseDialog(skinName: String, onConfirm: () -> Unit, onCancel: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onCancel() },
        title = { Text(text = "Confirmar compra") },
        text = { Text("¿Estás seguro de que quieres comprar la skin '$skinName'?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Sí, comprar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun StoreScreen(navController: NavController) {
    var skins by remember { mutableStateOf<List<SkinItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var message by remember { mutableStateOf<String?>(null) }
    var showMessage by remember { mutableStateOf(false) }
    var hasSeasonPass by remember { mutableStateOf(false) }

    // Estados para el contador hasta medianoche
    var timeUntilMidnight by remember { mutableStateOf(calculateTimeUntilMidnight()) }
    var hours by remember { mutableStateOf(0) }
    var minutes by remember { mutableStateOf(0) }
    var seconds by remember { mutableStateOf(0) }

    val context = LocalContext.current

    // Efecto para actualizar el contador cada segundo
    LaunchedEffect(Unit) {
        while(true) {
            timeUntilMidnight = calculateTimeUntilMidnight()

            // Convertir milisegundos a formato HH:MM:SS
            hours = (timeUntilMidnight / (1000 * 60 * 60)).toInt()
            minutes = ((timeUntilMidnight % (1000 * 60 * 60)) / (1000 * 60)).toInt()
            seconds = ((timeUntilMidnight % (1000 * 60)) / 1000).toInt()

            kotlinx.coroutines.delay(1000) // Actualizar cada segundo
        }
    }

    // Cargar los datos de la tienda y los items del usuario al iniciar
    LaunchedEffect(Unit) {
        isLoading = true

        // Verificar si el usuario tiene el pase de temporada
        checkSeasonPass(context) { hasSP ->
            hasSeasonPass = hasSP
            Log.d("SeasonPass", "Resultado verificación: $hasSP")

            // Cargar los demás elementos de la tienda
            loadUserItems(context) { userItemIds ->
                loadShopData(context) { shopItems ->
                    val updatedItems = shopItems.map { shopItem ->
                        shopItem.copy(alreadyOwned = userItemIds.contains(shopItem.id))
                    }
                    skins = updatedItems
                    isLoading = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
    ) {
        // Columna para el título y el contador
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "TIENDA",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Contador hasta medianoche
            Text(
                text = "Nueva tienda en:",
                fontSize = 16.sp,
                color = TextWhite.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Formato HH:MM:SS con ceros a la izquierda
            Text(
                text = String.format("%02d:%02d:%02d", hours, minutes, seconds),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SuccessGreen
            )
        }

        // Grid de objetos a la derecha
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxHeight()
                .width(600.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(skins) { skin ->
                StoreItem(
                    skin = skin,
                    onBuy = { selectedSkin ->
                        if (!selectedSkin.alreadyOwned) {
                            isLoading = true
                            handlePayment(context, selectedSkin) { success, msg ->
                                isLoading = false
                                message = msg
                                showMessage = true

                                if (success) {
                                    // Recargar datos después de una compra exitosa
                                    loadUserItems(context) { userItemIds ->
                                        loadShopData(context) { shopItems ->
                                            val updatedItems = shopItems.map { shopItem ->
                                                shopItem.copy(alreadyOwned = userItemIds.contains(shopItem.id))
                                            }
                                            skins = updatedItems
                                        }
                                    }
                                }
                            }
                        }
                    }
                )
            }
            // Elemento a ancho completo para el pase de temporada
            item(span = { GridItemSpan(2) }) {
                SeasonPassItem(
                    alreadyOwned = hasSeasonPass,
                    onBuy = {
                        isLoading = true
                        val seasonPassItem = SkinItem(
                            id = 9999, // ID especial para el pase de temporada
                            name = "Pase de Temporada",
                            price = 20.00,
                            imageRes = R.drawable.pase_de_temporada
                        )

                        handlePayment(context, seasonPassItem) { success, msg ->
                            isLoading = false
                            message = msg
                            showMessage = true

                            if (success) {
                                // Actualizar el estado del pase de temporada directamente
                                hasSeasonPass = true

                                // También recargar los otros datos de la tienda
                                loadUserItems(context) { userItemIds ->
                                    loadShopData(context) { shopItems ->
                                        val updatedItems = shopItems.map { shopItem ->
                                            shopItem.copy(alreadyOwned = userItemIds.contains(shopItem.id))
                                        }
                                        skins = updatedItems
                                    }
                                }
                            }
                        }
                    }
                )
            }
        }

        // Indicador de carga y mensajes (sin cambios)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PurpleBackground.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TextWhite)
            }
        }

        if (showMessage && message != null) {
            AlertDialog(
                onDismissRequest = {
                    showMessage = false
                    message = null
                },
                title = { Text("Resultado de la compra") },
                text = { Text(message!!) },
                confirmButton = {
                    Button(onClick = {
                        showMessage = false
                        message = null
                    }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}