package eina.unizar.frontend_movil.cliente_movil.model

data class Player(
    val id: String,
    var x: Float,
    var y: Float,
    var radius: Float,
    var color: Int,
    var skinName: String? = null,
    var username: String,
    var score: Int,
    var targetX: Float = x,
    var targetY: Float = y
)
