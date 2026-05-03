package it.Seltz.Qmus

import it.Seltz.Qmus.data.VocalizedLoader
import android.util.Log
import it.Seltz.Qmus.data.conjugation.ConjugationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import it.Seltz.Qmus.data.DictDatabase
import it.Seltz.Qmus.theme.JshoBg
import it.Seltz.Qmus.theme.JshoBrown
import it.Seltz.Qmus.theme.JshoDivider
import it.Seltz.Qmus.theme.JshoWhite
import it.Seltz.Qmus.ui.MainApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ConjugationManager.load(this)
        VocalizedLoader.load(this)
        Log.d("MAIN_DEBUG", "Coniugazioni caricate: ${ConjugationManager.loaded}")
        Log.d("MAIN_DEBUG", "Verbi disponibili: ${ConjugationManager.getForms("كَتَبَ").size}")
        try { DictDatabase.getInstance(this).readableDatabase } catch (_: Exception) {}
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                background = JshoBg, surface = JshoWhite, primary = JshoBrown,
                onBackground = Color.Black, onSurface = Color.Black, outline = JshoDivider
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = JshoBg) {
                    MainApp()
                }
            }
        }
    }
}