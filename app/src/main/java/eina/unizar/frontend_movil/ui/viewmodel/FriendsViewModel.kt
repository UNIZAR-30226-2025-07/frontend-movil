package eina.unizar.frontend_movil.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateListOf
import eina.unizar.frontend_movil.ui.models.Friend
import eina.unizar.frontend_movil.ui.functions.*
import kotlinx.serialization.json.Json

class FriendsViewModel : ViewModel() {
    val friends = mutableStateListOf<Friend>()

    fun loadFriends(userId: String) {  // Usamos String para que sea compatible con la API
        viewModelScope.launch {
            val response = Functions.get("friends/$userId")
            response?.let {
                try {
                    val friendsList = Json.decodeFromString<List<Friend>>(it)
                    friends.clear()
                    friends.addAll(friendsList)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Aquí podrías actualizar un estado para mostrar el error en la UI
                }
            }
        }
    }

    fun removeFriend(userId: String, friendId: String) {
        viewModelScope.launch {
            val response = Functions.delete("friends/$userId", """{"id": "$friendId"}""")
            if (response != null) {
                // Aquí actualizas la lista de amigos después de la eliminación
                loadFriends(userId)  // Recargar los amigos para que la lista se actualice
            }
        }
    }
    
}
