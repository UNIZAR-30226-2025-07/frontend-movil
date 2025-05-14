package eina.unizar.frontend_movil.cliente_movil.networking

import android.util.Log
import eina.unizar.frontend_movil.cliente_movil.utils.Constants
import okhttp3.*
import galaxy.Galaxy.Operation
import galaxy.Galaxy.OperationType
import galaxy.Galaxy.MoveOperation
import galaxy.Galaxy.Vector2D
import okio.ByteString
import java.nio.ByteBuffer
import java.util.UUID

class WebSocketClient(private val serverUrl: String, private val listener: WebSocketListener) {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    fun connect() {
        val request = Request.Builder()
            .url(serverUrl)
            .build()

        webSocket = client.newWebSocket(request, listener)
        // Nota: OkHttp lanza los callbacks en hilos del dispatcher interno
        Log.d(TAG, "WebSocket: conexión iniciada a $serverUrl")
    }

    /** Envía el movimiento (x,y) serializado en JSON */
    fun sendMovement(x: Float, y: Float) {
        if (webSocket == null) {
            Log.e(TAG, "WebSocket no conectado. No se puede enviar movimiento.")
            return
        }

        val scaledX = ((x + 1) / 2 * Constants.DEFAULT_WORLD_WIDTH).toInt()
        val scaledY = ((y + 1) / 2 * Constants.DEFAULT_WORLD_HEIGHT).toInt()

        Log.d(TAG, "Enviando movimiento escalado: x=$x, y=$y ; X=$scaledX, Y=$scaledY")

        val moveOperation = Operation.newBuilder()
            .setOperationType(OperationType.OpMove)
            .setMoveOperation(
                MoveOperation.newBuilder()
                    .setPosition(
                        Vector2D.newBuilder()
                            .setX(x.toInt())
                            .setY(y.toInt())
                            .build()
                    )
                    .build()
            )
            .build()

        val messageBytes = moveOperation.toByteArray()
        val ok = webSocket?.send(ByteString.of(*messageBytes)) ?: false
        if (!ok) Log.e(TAG, "Error al enviar movimiento")
    }

    fun sendEatFood(x: Float, y: Float, radius: Double) {
        val eatFoodOperation = Operation.newBuilder()
            .setOperationType(OperationType.OpEatFood)
            .setEatFoodOperation(
                galaxy.Galaxy.EatFoodOperation.newBuilder()
                    .setFoodPosition(
                        Vector2D.newBuilder()
                            .setX(x.toInt())
                            .setY(y.toInt())
                            .build()
                    )
                    .setNewRadius(radius.toInt()) // Siempre envía un valor
                    .build()
            )
            .build()

        val messageBytes = eatFoodOperation.toByteArray()
        val ok = webSocket?.send(ByteString.of(*messageBytes)) ?: false
        if (!ok) Log.e(TAG, "Error al enviar operación de comer comida")
    }

    fun sendEatPlayer(playerEatenId: String, newRadius: Double) {
        val uuid = UUID.fromString(playerEatenId)
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(uuid.mostSignificantBits)
        buffer.putLong(uuid.leastSignificantBits)
        val playerEatenBytes = com.google.protobuf.ByteString.copyFrom(buffer.array())


        val eatPlayerOperation = Operation.newBuilder()
            .setOperationType(OperationType.OpEatPlayer)
            .setEatPlayerOperation(
                galaxy.Galaxy.EatPlayerOperation.newBuilder()
                    .setPlayerEaten(playerEatenBytes)
                    .setNewRadius(newRadius.toInt())
                    .build()
            )
            .build()

        val messageBytes = eatPlayerOperation.toByteArray()
        val ok = webSocket?.send(ByteString.of(*messageBytes)) ?: false
        if (!ok) Log.e(TAG, "Error al enviar operación de comer jugador")
    }

    fun sendLeaveGame(){
        Log.d("GameActivity", "ADIOS ME VOY")
        val leaveOperation = Operation.newBuilder()
            .setOperationType(OperationType.OpLeave)
            .setLeaveOperation(
                galaxy.Galaxy.LeaveOperation.newBuilder()
            )
            .build()

        val messageBytes = leaveOperation.toByteArray()
        val ok = webSocket?.send(ByteString.of(*messageBytes)) ?: false
        if (!ok) Log.e(TAG, "Error al enviar operación de abandonar partida")

    }

    fun sendPauseGame(){
        val pauseOperation = Operation.newBuilder()
            .setOperationType(OperationType.OpPause)
            .setPauseOperation(
                galaxy.Galaxy.PauseOperation.newBuilder()
            )
            .build()

        val messageBytes = pauseOperation.toByteArray()
        val ok = webSocket?.send(ByteString.of(*messageBytes)) ?: false
        if (!ok) Log.e(TAG, "Error al enviar operación de pausar partida")
    }


    /** Emite petición de unirse al juego */
    fun joinGame(playerId: UUID, userName: String, skinName: String?, gameId: Int) {
        Log.d(TAG, "JOIN GAME: $gameId")
        val randomColor = (0xFFFFFF and (Math.random() * 0xFFFFFF).toInt()) // Genera un color aleatorio
        val buffer = ByteBuffer.wrap(ByteArray(16))
        buffer.putLong(playerId.mostSignificantBits)
        buffer.putLong(playerId.leastSignificantBits)
        val playerIdBytes = com.google.protobuf.ByteString.copyFrom(buffer.array())
        val skin = skinName
            ?.replace("basico", "básico")
            ?.replace(Regex("gn"), "ñ")
            ?.replace("_", " ")
            ?.lowercase()
            ?.replace(Regex("(^|\\s)([a-záéíóúñ])")) { matchResult ->
                matchResult.value.uppercase()}
        val joinOperation = Operation.newBuilder()
            .setOperationType(OperationType.OpJoin)
            .setJoinOperation(
                galaxy.Galaxy.JoinOperation.newBuilder()
                    .setPlayerID(playerIdBytes)
                    .setUsername(userName)
                    .setColor(randomColor) // Asigna el color generado
                    .apply {
                        if (!skin.isNullOrEmpty()) setSkin(skin+".png")
                    }
                    .setGameID(gameId)
                    .build()
            )
            .build()

        val messageBytes = joinOperation.toByteArray()
        val ok = webSocket?.send(ByteString.of(*messageBytes)) ?: false
        if (!ok) Log.e(TAG, "Failed to send join operation")
    }

    /** Cierra la conexión limpiamente */
    fun close() {
        webSocket?.close(1000, "Client closing")
        client.dispatcher.executorService.shutdown()
        Log.d(TAG, "WebSocket: conexión cerrada")
    }


    companion object {
        private const val TAG = "WebSocketClient"
    }
}

