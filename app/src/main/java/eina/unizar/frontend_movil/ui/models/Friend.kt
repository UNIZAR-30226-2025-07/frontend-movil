package eina.unizar.frontend_movil.ui.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class Friend(
    val id: String,   // Usamos String, ya que la API lo describe como "id" (puede ser número o string)
    val nombre: String
)
