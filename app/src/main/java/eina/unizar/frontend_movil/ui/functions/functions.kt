package eina.unizar.frontend_movil.ui.functions

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection



object functions {
    const val BASE_URL = "http://10.0.2.2:3000" //DUDA
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
}
