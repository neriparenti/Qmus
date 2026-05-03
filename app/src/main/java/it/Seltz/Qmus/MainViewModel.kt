package it.Seltz.Qmus

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.Seltz.Qmus.data.conjugation.ConjugationManager
import it.Seltz.Qmus.data.DictDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _ready = MutableStateFlow(false)
    val ready: StateFlow<Boolean> = _ready

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Load database and conjugations in background,
            // without blocking the main UI thread
            try {
                DictDatabase.getInstance(application).readableDatabase
                ConjugationManager.load(application)
            } catch (e: Exception) {
                // In the future: show a StateFlow to show
                // a messagge to the user if the DB won't load
            }
            _ready.value = true
        }
    }
}
