package eina.unizar.frontend_movil

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import eina.unizar.frontend_movil.ui.navigation.AppNavigation
import eina.unizar.frontend_movil.ui.utils.MusicManager

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
        iniciarMusicaDeFondo()
    }

    private fun iniciarMusicaDeFondo() {
        MusicManager.mediaPlayer = MediaPlayer.create(this, R.raw.music1)
        MusicManager.mediaPlayer?.apply {
            isLooping = true
            setVolume(MusicManager.volume.value, MusicManager.volume.value)
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        mediaPlayer?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun MyApp() {
    MaterialTheme {
        AppNavigation()
    }
}