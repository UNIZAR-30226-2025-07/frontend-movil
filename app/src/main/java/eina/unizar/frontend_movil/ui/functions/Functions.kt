package eina.unizar.frontend_movil.ui.functions

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
//import eina.unizar.frontend_movil.ui.utils.AuthInterceptor

object Functions {
    const val BASE_URL = "http://galaxy.t2dc.es:3000"


    suspend fun get(endpoint: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return@withContext connection.inputStream.bufferedReader().readText()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getWithHeaders(endpoint: String, headers: Map<String, String>): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                return@withContext connection.inputStream.bufferedReader().readText()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    suspend fun post(endpoint: String, jsonBody: JSONObject): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            connection.outputStream.bufferedWriter().use {
                it.write(jsonBody.toString())
            }

            return@withContext when (connection.responseCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    connection.inputStream.bufferedReader().readText()
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    connection.errorStream.bufferedReader().readText()
                }
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    /*fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(password.toByteArray())
        return hashedBytes.joinToString("") { "%02x".format(it) }
    }*/

    //Modificada para que las headers sean opcionales pero puedan ser usadas
    suspend fun delete(endpoint: String, jsonBody: String, headers: Map<String, String> = emptyMap()): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "DELETE"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            // Añadir headers personalizados
            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }

            // Escribimos el cuerpo JSON
            connection.outputStream.bufferedWriter().use {
                it.write(jsonBody)
            }

            return@withContext when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> connection.inputStream.bufferedReader().readText()
                HttpURLConnection.HTTP_UNAUTHORIZED -> connection.errorStream.bufferedReader().readText()
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun put(endpoint: String, body: String, headers: Map<String, String> = emptyMap()): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "PUT"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            // Añadir headers personalizados
            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }

            // Escribir el cuerpo de la solicitud
            connection.outputStream.bufferedWriter().use {
                it.write(body)
            }

            return@withContext when (connection.responseCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_ACCEPTED, HttpURLConnection.HTTP_NO_CONTENT -> {
                    if (connection.contentLength > 0) {
                        connection.inputStream.bufferedReader().readText()
                    } else {
                        "{\"success\":true}"
                    }
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    // Manejar caso de token inválido
                    val errorResponse = connection.errorStream.bufferedReader().readText()
                    android.util.Log.e("API", "Error 401 en PUT a $endpoint: $errorResponse")
                    null
                }
                else -> {
                    val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Error desconocido"
                    android.util.Log.e("API", "Error ${connection.responseCode} en PUT a $endpoint: $errorResponse")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("API", "Excepción en PUT a $endpoint: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    // Método adicional para verificar la integridad del token
    suspend fun postWithBody(endpoint: String, body: String, headers: Map<String, String> = emptyMap()): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            // Añadir headers personalizados
            for ((key, value) in headers) {
                connection.setRequestProperty(key, value)
            }

            // Escribir el cuerpo de la solicitud
            connection.outputStream.bufferedWriter().use {
                it.write(body)
            }

            return@withContext when (connection.responseCode) {
                HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED -> {
                    connection.inputStream.bufferedReader().readText()
                }
                else -> {
                    val errorResponse = connection.errorStream?.bufferedReader()?.readText() ?: "Error desconocido"
                    android.util.Log.e("API", "Error ${connection.responseCode} en POST a $endpoint: $errorResponse")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("API", "Excepción en POST a $endpoint: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
