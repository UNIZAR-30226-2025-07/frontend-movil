package eina.unizar.frontend_movil.ui.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Serializable
data class Player(
    val name: String,
    val isReady: Boolean,
    val id: String
) {

}
