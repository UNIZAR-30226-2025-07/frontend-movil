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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "LOGROS",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(vertical = 32.dp)
        )

        AchievementItem(
            title = "Elimina a 50 jugadores",
            isCompleted = true,
            xpReward = "+ 50 Xp"
        )

        AchievementItem(
            title = "Juega 100 partidas",
            progress = "(23/100)",
            isCompleted = false
        )

        AchievementItem(
            title = "Llega hasta el nivel 10",
            progress = "(7/10)",
            isCompleted = false
        )
    }
}
