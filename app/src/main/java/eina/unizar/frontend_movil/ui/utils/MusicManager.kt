package eina.unizar.frontend_movil.ui.utils

import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf

object MusicManager {
    var volume = mutableStateOf(0.38f)
    var mediaPlayer: MediaPlayer? = null
}