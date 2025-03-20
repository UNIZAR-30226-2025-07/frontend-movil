package eina.unizar.frontend_movil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eina.unizar.frontend_movil.ui.theme.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

data class AchievementData(
    val title: String,
    val isCompleted: Boolean,
    val progress: String = "",
    val xpReward: String = ""
)

@Composable
fun AchievementItem(
    title: String,
    isCompleted: Boolean = false,
    progress: String = "",
    xpReward: String = ""
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) LightGreenCard else DarkGreenCard
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    color = TextWhite,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (progress.isNotEmpty()) {
                    Text(
                        text = progress,
                        color = TextWhite.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (xpReward.isNotEmpty()) {
                    Text(
                        text = xpReward,
                        color = TextWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint = TextWhite,
                        modifier = Modifier
                            .size(24.dp)
                            .background(SuccessGreen, shape = RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Columna izquierda para el título
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "LOGROS",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.padding(vertical = 32.dp)
            )
        }

        // Columna derecha para los logros
        val achievements = listOf(
            AchievementData("Elimina a 50 jugadores", true, "+ 50 Xp"),
            AchievementData("Juega 100 partidas", false, "(23/100)"),
            AchievementData("Llega hasta el nivel 10", false, "(7/10)"),
            AchievementData("Gana 10 partidas consecutivas", false, "(5/10)"),
            AchievementData("Completa el tutorial", true, "+ 20 Xp"),
            AchievementData("Desbloquea todos los personajes", false, "(3/5)"),
            AchievementData("Alcanza el nivel 20", false, "(15/20)"),
            AchievementData("Juega durante 10 horas", false, "(7/10)")
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(achievements) { achievement ->
                AchievementItem(
                    title = achievement.title,
                    isCompleted = achievement.isCompleted,
                    progress = achievement.progress,
                    xpReward = achievement.xpReward
                )
            }
        }
    }
}
