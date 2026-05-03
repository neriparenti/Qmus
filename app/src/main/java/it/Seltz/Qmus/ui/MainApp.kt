package it.Seltz.Qmus.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import it.Seltz.Qmus.data.DictDatabase
import it.Seltz.Qmus.data.Screen
import it.Seltz.Qmus.data.SearchMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MainApp() {
    val context = LocalContext.current
    val db = remember { DictDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.MainSearch)) }
    val currentScreen = screenStack.last()
    var rootResults by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var showRootResults by remember { mutableStateOf(false) }
    var savedQuery by remember { mutableStateOf("") }
    var savedResults by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var savedSearchMode by remember { mutableStateOf(SearchMode.ARABIC) }
    var showHarakat by remember { mutableStateOf(false) }

    BackHandler(enabled = screenStack.size > 1 || showRootResults) {
        if (showRootResults) showRootResults = false
        else if (screenStack.size > 1) screenStack = screenStack.dropLast(1)
    }

    fun navigateTo(screen: Screen) { screenStack = screenStack + screen }
    fun goBack() { if (screenStack.size > 1) screenStack = screenStack.dropLast(1) }

    when (currentScreen) {
        is Screen.MainSearch -> MainSearchScreen(
            db = db,
            onWordSelected = { w -> scope.launch { val ex = withContext(Dispatchers.IO) { db.searchById(w["id"] ?: "") }; if (ex != null) navigateTo(Screen.WordDetail(ex)) } },
            onRootSearchClicked = { navigateTo(Screen.RootSelection) },
            externalResults = if (showRootResults) rootResults else null,
            onClearExternalResults = { showRootResults = false; rootResults = emptyList() },
            savedQuery = savedQuery, savedResults = savedResults, savedSearchMode = savedSearchMode,
            onSaveState = { q, r, m -> savedQuery = q; savedResults = r; savedSearchMode = m },
            showHarakat = showHarakat,
            onToggleHarakat = { showHarakat = !showHarakat }
        )
        is Screen.RootSelection -> RootSelectionScreen(db = db,
            onRootSelected = { root -> scope.launch { rootResults = withContext(Dispatchers.IO) { db.searchByRoot(root) }; showRootResults = true; screenStack = listOf(Screen.MainSearch) } },
            onBack = { goBack() })
        is Screen.WordDetail -> WordDetailScreen(word = currentScreen.word,
            onWordClick = { text -> scope.launch { val f = withContext(Dispatchers.IO) { db.searchByArabicText(text) }; if (f != null) navigateTo(Screen.WordDetail(f)) } },
            onBack = { goBack() },
            showHarakat = showHarakat)
    }
}