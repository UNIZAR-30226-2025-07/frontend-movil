package eina.unizar.frontend_movil.ui.utils

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "auth_tokens"
    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(ACCESS_TOKEN, accessToken)
            putString(REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    fun getAccessToken(): String? = prefs.getString(ACCESS_TOKEN, null)
    fun getRefreshToken(): String? = prefs.getString(REFRESH_TOKEN, null)
    fun clearTokens() = prefs.edit().clear().apply()
}