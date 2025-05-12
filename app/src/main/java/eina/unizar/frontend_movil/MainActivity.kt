package eina.unizar.frontend_movil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import eina.unizar.frontend_movil.ui.navigation.AppNavigation
import eina.unizar.frontend_movil.ui.utils.MusicManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
        iniciarMusicaDeFondo()
    }

    private fun iniciarMusicaDeFondo() {
        // Inicializa la música en el MusicManager
        MusicManager.initMusic(this, R.raw.music1)
        MusicManager.playMusic()
    }

    override fun onPause() {
        super.onPause()
        // Pausa la música cuando la app va a segundo plano
        MusicManager.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        // Reanuda la música cuando la app vuelve a primer plano
        MusicManager.playMusic()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libera los recursos de música cuando la app se destruye
        MusicManager.releaseMusic()
    }
}

@Composable
fun MyApp() {
    MaterialTheme {
        AppNavigation()
    }
}