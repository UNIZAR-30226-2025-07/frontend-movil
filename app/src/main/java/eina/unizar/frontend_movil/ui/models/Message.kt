package eina.unizar.frontend_movil.ui.models

import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val text: String,
    val timestamp: String,
    val sentByUser: Boolean
)