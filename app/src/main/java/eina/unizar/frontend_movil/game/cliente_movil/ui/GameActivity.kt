package eina.unizar.frontend_movil.cliente_movil.ui

import MainMenuScreen
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import eina.unizar.frontend_movil.R
import eina.unizar.frontend_movil.cliente_movil.game.GameView
import eina.unizar.frontend_movil.cliente_movil.networking.WebSocketClient
import org.json.JSONObject
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import galaxy.Galaxy.Event
import galaxy.Galaxy.EventType
import galaxy.Galaxy.NewPlayerEvent
import galaxy.Galaxy.NewFoodEvent
import galaxy.Galaxy.PlayerMoveEvent
import galaxy.Galaxy.PlayerGrowEvent
import galaxy.Galaxy.DestroyFoodEvent
import galaxy.Galaxy.DestroyPlayerEvent
import galaxy.Galaxy.JoinEvent
import okio.ByteString
import eina.unizar.frontend_movil.cliente_movil.model.Food
import eina.unizar.frontend_movil.cliente_movil.model.Player
import eina.unizar.frontend_movil.cliente_movil.utils.ColorUtils
import eina.unizar.frontend_movil.cliente_movil.utils.Constants
import galaxy.Galaxy.PauseEvent
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.jvm.java

const val FOOD_RADIUS = 20f

class GameActivity : AppCompatActivity(), GameView.MoveListener {

    private lateinit var gameView: GameView
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var btnReconnect: Button
    private lateinit var btnPause: Button  // Botón de pausa para el líder

    private var userId: String = ""
    private var userName: String = ""
    private var serverUrl: String = ""
    private var skinName: String? = null
    private var gameId: Int = 0

    private var isLeader: Boolean = false
    private var isPrivateGame: Boolean = false

    private var isJoinned: Boolean = false


    private val deadPlayers = mutableListOf<UUID>()

    // Último estado completo recibido
    private var gameState: JSONObject? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Leer parámetros de la Activity
        intent.extras?.let { b ->
            userId    = b.getString("userId", "")
            userName  = b.getString("userName", "")
            serverUrl = b.getString("serverUrl", null)
            skinName = b.getString("skinName", null)
            gameId = b.getInt("gameId", 0)
            isLeader = b.getBoolean("isLeader", false)
        }

        if (userId.isEmpty() || userName.isEmpty() || serverUrl.isEmpty()) {
            Toast.makeText(this, "Faltan parámetros de conexión", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        isPrivateGame = gameId != 0

        gameView     = findViewById(R.id.GameView)
        btnReconnect = findViewById(R.id.btnReconnect)
        btnPause = findViewById(R.id.btnPause)  // Botón de pausa
        gameView.setMoveListener(this)

        btnPause.visibility = if (isPrivateGame && isLeader) View.VISIBLE else View.GONE
        // Configuramos la acción del botón de pausa
        if (isPrivateGame && isLeader){
            btnPause.setOnClickListener {
                pauseGame()
            }
        }

        // Preparamos el WebSocketListener inline
        val listener = object : WebSocketListener() {

            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "onOpen: Conectado al servidor")
                runOnUiThread {
                    Toast.makeText(this@GameActivity,
                        "¡Conectado!",
                        Toast.LENGTH_SHORT).show()
                }
                // Nada más abrir, enviamos el joinGame
                val uuid = UUID.fromString(userId)  // Convierte String a UUID
                webSocketClient.joinGame(uuid, userName, skinName, gameId)
            }

            override fun onMessage(ws: WebSocket, bytes: ByteString) {
                try {
                    val event = Event.parseFrom(bytes.toByteArray())
                    Log.d(TAG, "Evento recibido: $event")
                    if (!isJoinned && event.eventType != EventType.EvJoin) {
                        Log.d(TAG, "Esperando a unirse al juego...")
                        return
                    }
                    when (event.eventType) {
                        EventType.EvNewPlayer -> handleNewPlayer(event.newPlayerEvent)
                        EventType.EvNewFood -> handleNewFood(event.newFoodEvent)
                        EventType.EvPlayerMove -> handlePlayerMove(event.playerMoveEvent)
                        EventType.EvPlayerGrow -> handlePlayerGrow(event.playerGrowEvent)
                        EventType.EvDestroyFood -> handleDestroyFood(event.destroyFoodEvent)
                        EventType.EvDestroyPlayer -> handleDestroyPlayer(event.destroyPlayerEvent)
                        EventType.EvPause -> handlePause(event.pauseEvent)
                        EventType.EvJoin -> handleJoin(event.joinEvent)
                        EventType.EvPause -> handlePause(event.pauseEvent)
                        else -> Log.d(TAG, "Unhandled event type: ${event.eventType}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing message: ${e.message}")
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "onClosing: $code / $reason")
                webSocketClient.sendLeaveGame()
                ws.close(1000, null)
                runOnUiThread {
                    Toast.makeText(this@GameActivity,
                        "Servidor cerrando: $reason",
                        Toast.LENGTH_SHORT).show()
                    btnReconnect.visibility = View.VISIBLE
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "onFailure: ${t.message}")
                webSocketClient.sendLeaveGame()
                runOnUiThread {
                    Toast.makeText(this@GameActivity,
                        "Error: ${t.message}. Intentando reconectar...",
                        Toast.LENGTH_LONG).show()
                    reconnectWithDelay()
                }
            }
        }

        // Creamos el cliente y conectamos
        webSocketClient = WebSocketClient(serverUrl, listener)
        btnReconnect.setOnClickListener {
            btnReconnect.visibility = View.GONE
            webSocketClient.connect()
        }
        connectToServer()
    }

    // Método para actualizar la visibilidad del botón de pausa
    private fun updatePauseButtonVisibility() {
        btnPause.visibility = if (isLeader && isPrivateGame) View.VISIBLE else View.GONE
    }

    // Método para pausar la partida
    private fun pauseGame() {
        webSocketClient.sendPauseGame()
        Toast.makeText(this, "Pausando partida...", Toast.LENGTH_SHORT).show()
        runOnUiThread {
            val intent = Intent(this, MainMenuActivity::class.java).apply {
                // Limpia el task actual y crea uno nuevo con MainMenuActivity en la raíz
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun reconnectWithDelay(delayMillis: Long = 3000) {
        Log.d(TAG, "Intentando reconectar en $delayMillis ms...")
        btnReconnect.visibility = View.GONE
        gameView.postDelayed({
            connectToServer()
        }, delayMillis)
    }

    private fun connectToServer() {
        //webSocketClient.close() // Cierra conexiones previas
        btnReconnect.visibility = View.GONE
        webSocketClient.connect()
    }

    /** Cuando recibimos que el jugador actual muere */
    private fun onPlayerDied() {
        var finalScore = 0
        try {
            // Busca en el último estado la puntuación
            val players = gameState?.getJSONArray("players")
            if (players != null) {
                for (i in 0 until players.length()) {
                    val p = players.getJSONObject(i)
                    if (p.getString("id") == gameView.currentPlayerId) {
                        finalScore = p.optInt("score", 0)
                        break
                    }
                }
            }
        } catch (_: Exception) { /* ignore */ }

        runOnUiThread {
            val uuid = UUID.fromString(userId)
            AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("Tu puntuación: $finalScore")
                .setPositiveButton("Jugar otra vez") { _, _ ->
                    webSocketClient.joinGame(uuid, userName, skinName, 1)
                }
                .setNegativeButton("Salir") { _, _ ->
                    finish()
                }
                .setCancelable(false)
                .show()
        }
    }

    /** Desde GameView.MoveListener: enviamos movimiento */
    override fun onMove(directionX: Float, directionY: Float) {
        Log.d(TAG, "Movimiento enviado: X=$directionX, Y=$directionY")
        if (directionX != 0f || directionY != 0f) {
            webSocketClient.sendMovement(directionX, directionY)
        }
    }

    override fun onDestroy() {
        val playerId = gameView.currentPlayerId
        if (playerId != null) {
            webSocketClient.sendLeaveGame()
        }
        webSocketClient.close()
        super.onDestroy()
    }

    // Utilidad para convertir color RGB a ARGB (opacidad completa)
    private fun rgbToArgb(rgb: Int): Int {
        return 0xFF000000.toInt() or (rgb and 0x00FFFFFF)
    }

    private fun handleNewPlayer(event: NewPlayerEvent) {
        val bytes = event.playerID.toByteArray()
        val bb = ByteBuffer.wrap(bytes)
        val uuid = UUID(bb.long, bb.long)
        val playerId = uuid.toString()

        val skin = event.skin
            .lowercase()
            .replace(Regex("[áäàâã]"), "a")
            .replace(Regex("[éëèê]"), "e")
            .replace(Regex("[íïìî]"), "i")
            .replace(Regex("[óöòôõ]"), "o")
            .replace(Regex("[úüùû]"), "u")
            .replace(Regex("[ñ]"), "gn")
            .replace(" ", "_")
            .replace(".png", "")

        val player = Player(
            id = playerId,
            x = event.position.x.toFloat(),
            y = event.position.y.toFloat(),
            radius = event.radius.toFloat(),
            color = rgbToArgb(event.color),
            skinName = if (skin.isNotEmpty()) skin else "aspecto_basico",
            username = event.username,
            score = event.radius.toInt()/10
        )
        runOnUiThread {
            gameView.updatePlayers(player)
            gameView.invalidate()
        }
    }

    private fun handleNewFood(event: NewFoodEvent) {
        try {
            val foodList = event.foodList
            if (foodList.isNullOrEmpty()) {
                Log.e(TAG, "EvNewFood: lista de comida vacía")
                return
            }

            val foodItems = foodList.map { food ->
                Food(
                    id = "${food.position.x.toInt()},${food.position.y.toInt()}",
                    x = food.position.x.toFloat(),
                    y = food.position.y.toFloat(),
                    radius = 20f, // Ajusta si el radio es configurable
                    color = ColorUtils.parseColor("#${Integer.toHexString(food.color)}")
                )
            }

            runOnUiThread {
                val currentIds = gameView.foodItems.map { it.id }.toSet()
                val foodsToAdd = foodItems.filter { it.id !in currentIds }
                gameView.updateFoodItems(foodItems)
                /*gameView.foodItems.addAll(foodsToAdd)
                gameView.invalidate()*/
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar evento EvNewFood: ${e.message}")
        }
    }

    private fun handlePlayerMove(event: PlayerMoveEvent) {
        val bytes = event.playerID.toByteArray()
        val bb = ByteBuffer.wrap(bytes)
        val uuid = UUID(bb.long, bb.long)
        val playerId = uuid.toString()
        Log.e(TAG, "ID del jugador: $playerId")
        val player = gameView.getPlayer(playerId)

        if (player != null) {
            // Actualizar las coordenadas del jugador
            player.targetX = event.position.x.toFloat()
            player.targetY = event.position.y.toFloat()

            // Validar que las coordenadas estén dentro de los límites del mapa
            if (player.x < 0 || player.x > Constants.DEFAULT_WORLD_WIDTH || player.y < 0 || player.y > Constants.DEFAULT_WORLD_HEIGHT) {
                Log.e(TAG, "Jugador fuera de los límites: ID=$playerId, X=${player.x}, Y=${player.y}")
            }

            // Si es el jugador actual, actualizar la cámara
            if (playerId == gameView.currentPlayerId) {
                gameView.updateCameraPosition()
            }

            runOnUiThread {
                gameView.invalidate() // Redibujar el juego
            }
        } else {
            Log.e(TAG, "Jugador no encontrado: ID=$playerId")
            if (deadPlayers.find { it == uuid } == null) {
                val currentplayer = gameView.currentPlayerId
                val name = gameView.players[currentplayer]?.username
                val skin = gameView.players[currentplayer]?.skinName
                if (name != null){
                    webSocketClient.joinGame(uuid, name, skin, 1)
                }
                Log.e(TAG, "Reconnecting")
            }
        }
    }

    private fun handlePlayerGrow(event: PlayerGrowEvent) {
        val bytes = event.playerID.toByteArray()
        val bb = ByteBuffer.wrap(bytes)
        val uuid = UUID(bb.long, bb.long)
        val playerId = uuid.toString()
        val player = gameView.getPlayer(playerId)
        if (player != null) {
            player.radius = event.radius.toFloat()
            player.score = player.radius.toInt()/10
            Log.d("TAG", "SCORE Y RADIO ACTUALIZADO: ${player.score} ${player.radius}")
            runOnUiThread {
                gameView.updateScoreRadius(player.id, player.score, player.radius)
                gameView.invalidate() // Redibuja el juego
            }
        }
    }

    private fun handleDestroyFood(event: DestroyFoodEvent) {
        val foodId = "${event.position.x.toInt()},${event.position.y.toInt()}"
        runOnUiThread {
            gameView.removeIfFood(foodId)
            gameView.invalidate() // Redibuja el juego
        }
    }

    private fun handleDestroyPlayer(event: DestroyPlayerEvent) {
        val bytes = event.playerID.toByteArray()
        val bb = ByteBuffer.wrap(bytes)
        val uuid = UUID(bb.long, bb.long)
        val playerId = uuid.toString()
        deadPlayers.add(uuid)
        gameView.removePlayer(playerId)
        runOnUiThread {
            gameView.invalidate() // Redibuja el juego
            if (playerId == gameView.currentPlayerId) {
                onPlayerDied()
            }
        }
    }

    private fun handleJoin(event: JoinEvent) {
        isJoinned = true
        val bytes = event.playerID.toByteArray()
        val bb = ByteBuffer.wrap(bytes)
        val uuid = UUID(bb.long, bb.long)
        val playerId = uuid.toString()
        val spawnX = event.position.x.toFloat()
        val spawnY = event.position.y.toFloat()

        gameView.updateCurrentPlayerId(playerId)
        val player = Player(
            id = playerId,
            x = spawnX,
            y = spawnY,
            radius = event.radius.toFloat(),
            color = rgbToArgb(event.color),
            skinName = if (event.skin.isNotEmpty()) event.skin else "aspecto_basico",
            username = userName,
            score = event.radius.toInt()
        )
        runOnUiThread {
            gameView.updatePlayers(player)
            gameView.initializeFood()
            gameView.invalidate()
        }
    }

    private fun handlePause(event: PauseEvent) {
        // Implementar lógica para manejar la pausa del juego
        Log.d(TAG, "Juego pausado")
        // Vuelve al menú principal
        runOnUiThread {
            val intent = Intent(this, MainMenuActivity::class.java).apply {
                // Limpia el task actual y crea uno nuevo con MainMenuActivity en la raíz
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    fun sendEatFood(food: Food) {
        val player = gameView.currentPlayerId?.let { gameView.getPlayer(it) }
        if (player != null) {
            // Suma de áreas: área total = área jugador + área comida
            val playerArea = Math.PI * player.radius * player.radius
            val foodArea = Math.PI * food.radius * food.radius
            val newArea = playerArea + foodArea
            val newRadius = Math.sqrt((player.radius * player.radius + FOOD_RADIUS * FOOD_RADIUS).toDouble()) * 1.0002
            player.radius = newRadius.toFloat()
            val foodId = "${food.x.toInt()},${food.y.toInt()}"
            gameView.removeIfFood(foodId)
            webSocketClient.sendEatFood(food.x, food.y, newRadius)
            player.score = player.radius.toInt() / 10
        }
    }

    fun sendEatPlayer(other: Player) {
        val player = gameView.currentPlayerId?.let { gameView.getPlayer(it) }
        if (player != null) {
            // Suma de áreas: área total = área jugador + área del otro jugador
            val playerArea = Math.PI * player.radius * player.radius
            val otherArea = Math.PI * other.radius * other.radius
            val newArea = playerArea + otherArea
            val newRadius = Math.sqrt((player.radius * player.radius + other.radius * other.radius).toDouble()) * 1.0002 //Math.sqrt(newArea / Math.PI).toFloat()
            val playerId = other.id
            player.radius = newRadius.toFloat()
            gameView.removePlayer(playerId)
            webSocketClient.sendEatPlayer(other.id, newRadius)
            player.score = player.radius.toInt() / 10
        }
    }


    companion object {
        private const val TAG = "GameActivity"
    }
}


class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 1️⃣ Crea el NavController
            val navController = rememberNavController()

            // 2️⃣ Pásalo al composable
            MainMenuScreen(navController)
        }
    }
}

