package eina.unizar.frontend_movil.ui.screens

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
import androidx.compose.foundation.lazy.items

data class SkinItem(
    val id: Int,
    val name: String,
    val price: String,
    val imageRes: Int
)

@Composable
fun StoreItem(
    skin: SkinItem,
    onBuy: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de la skin
            Image(
                painter = painterResource(id = skin.imageRes),
                contentDescription = skin.name,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(TextWhite),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nombre de la skin
            Text(
                text = skin.name,
                color = TextWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Botón de compra
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Comprar",
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = skin.price)
                }
            }

            // Popup de confirmación
            if (showDialog) {
                ConfirmPurchaseDialog(
                    skinName = skin.name,
                    onConfirm = {
                        onBuy() // Lógica de compra
                        showDialog = false
                    },
                    onCancel = { showDialog = false }
                )
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
    val skins = listOf(
        SkinItem(1, "Nebula Purple", "2.99 €", R.drawable.skin_nebula),
        SkinItem(2, "Galaxy Swirl", "3.99 €", R.drawable.skin_galaxy),
        SkinItem(3, "Cosmic Rain", "2.99 €", R.drawable.skin_cosmic),
        SkinItem(4, "Star Dust", "4.99 €", R.drawable.skin_stardust),
        SkinItem(5, "Aurora", "3.99 €", R.drawable.skin_aurora),
        SkinItem(6, "Black Hole", "5.99 €", R.drawable.skin_blackhole)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
    ) {
        // Título centrado en la mitad izquierda
        Text(
            text = "TIENDA",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        )

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
                    onBuy = { /* Implementar lógica de compra */ }
                )
            }
        }
    }
}