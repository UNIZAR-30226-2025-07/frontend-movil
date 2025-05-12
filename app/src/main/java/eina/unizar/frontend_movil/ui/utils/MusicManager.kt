package eina.unizar.frontend_movil.ui.utils

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.mutableStateOf

object MusicManager {
    var volume = mutableStateOf(0.38f)
    var mediaPlayer: MediaPlayer? = null

    // Inicializa la música con un recurso específico
    fun initMusic(context: Context, resourceId: Int) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(volume.value, volume.value)
        }
    }

    // Inicia la reproducción de música si no está sonando
    fun playMusic() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    // Pausa la música si está sonando
    fun pauseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
            }
        }
    }

    // Libera recursos de la música
    fun releaseMusic() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
            mediaPlayer = null
        }
    }
}