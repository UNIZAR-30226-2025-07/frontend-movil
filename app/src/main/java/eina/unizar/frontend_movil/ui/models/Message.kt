package eina.unizar.frontend_movil.ui.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class Message(
    val id: String,
    val text: String,
    val timestamp: String,
    val sentByUser: Boolean
)