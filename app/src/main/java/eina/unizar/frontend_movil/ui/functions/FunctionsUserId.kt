import android.content.Context
import android.util.Base64
import android.util.Log
import org.json.JSONObject

object FunctionsUserId {
    fun extractUserId(token: String?): String? {
        if (token.isNullOrEmpty()) {
            Log.d("PlayerProgress", "Token es nulo o vacío")
            return null
        }

        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.e("PlayerProgress", "Token malformado: número de partes incorrecto")
                return null
            }

            // Ajustar el padding del Base64
            val payload = parts[1].padEnd((parts[1].length + 3) / 4 * 4, '=')
            val decoded = Base64.decode(payload, Base64.URL_SAFE)
            val jsonString = String(decoded, Charsets.UTF_8)
            Log.d("PlayerProgress", "Payload decodificado: $jsonString")

            val jsonObject = JSONObject(jsonString)
            jsonObject.optString("id", null).also { userId ->
                if (userId.isNullOrEmpty()) {
                    Log.e("PlayerProgress", "No se encontró el campo 'id' en el token")
                } else {
                    Log.d("PlayerProgress", "UserId extraído: $userId")
                }
            }
        } catch (e: IllegalArgumentException) {
            Log.e("PlayerProgress", "Error al decodificar Base64: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("PlayerProgress", "Error procesando el token: ${e.message}", e)
            null
        }
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return  sharedPreferences.getString("access_token", null)
    }
}