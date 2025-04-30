package eina.unizar.frontend_movil.ui.utils

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = TokenManager.getAccessToken()

        // Añadir token de acceso a la solicitud original
        val requestWithToken = originalRequest.newBuilder()
            .header("Auth", accessToken ?: "")
            .build()

        var response = chain.proceed(requestWithToken)

        // Si la respuesta es 401, intentar refrescar el token
        if (response.code == 401) {
            response.close()

            val newToken = runBlocking { refreshAccessToken() }

            return if (newToken != null) {
                // Reintentar la solicitud original con el nuevo token
                val newRequest = originalRequest.newBuilder()
                    .header("Auth", newToken)
                    .build()
                chain.proceed(newRequest)
            } else {
                response
            }
        }

        return response
    }

    private fun refreshAccessToken(): String? {
        val refreshToken = TokenManager.getRefreshToken() ?: return null

        val client = okhttp3.OkHttpClient()
        val json = JSONObject().put("refreshToken", refreshToken).toString()

        val request = Request.Builder()
            .url("http://galaxy.t2dc.es:3000/auth/refresh-token")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val newToken = JSONObject(response.body?.string() ?: "").getString("accessToken")
                    TokenManager.saveTokens(newToken, refreshToken)
                    newToken
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

}