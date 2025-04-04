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
import java.net.URL
import java.net.HttpURLConnection


object functions {
    const val BASE_URL = "https://10.0.2.2" //DUDA, ÍRIA UN /API DESPUÉS O TB PUEDE QUE NO SE DEBA QUITAR CUANDO TIREMOS POR DOCKER

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

    suspend fun post(endpoint: String, jsonBody: String): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$endpoint")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            connection.outputStream.bufferedWriter().use { it.write(jsonBody) }

            if (connection.responseCode == HttpURLConnection.HTTP_OK || connection.responseCode == HttpURLConnection.HTTP_CREATED) {
                return@withContext connection.inputStream.bufferedReader().readText()
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}