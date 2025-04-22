package eina.unizar.frontend_movil.ui.functions

import android.content.Context



// FUNCIONES NECESARIAS PARA PODER SABER EL ID DEL USUARIO POR EJEMPLO AL ELIMINAR AMIGO


object SharedPrefsUtil {

    private const val PREFS_NAME = "UserPrefs"
    // DEBERIA REALIZARSE AL INICIAR SESION PARA TENERLO DISPONIBLE TODA LA SESION
    fun saveUserId(context: Context, userId: String) {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("userId", userId)
        editor.apply()
    }
    // USADO PARA SABER EL ID DEL USUARIO AL ELIMINAR AMIGO
    fun getUserId(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString("userId", null)
    }
}
