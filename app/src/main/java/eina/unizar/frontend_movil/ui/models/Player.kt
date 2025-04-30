package eina.unizar.frontend_movil.ui.models

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val name: String,
    val isReady: Boolean
)
