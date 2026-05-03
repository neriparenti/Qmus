package it.Seltz.Qmus.ui

import android.util.Log
import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.data.DictDatabase
import it.Seltz.Qmus.data.NumberGrammar
import it.Seltz.Qmus.data.SearchMode
import it.Seltz.Qmus.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSearchScreen(
    db: DictDatabase,
    onWordSelected: (Map<String, String>) -> Unit,
    onRootSearchClicked: () -> Unit,
    externalResults: List<Map<String, String>>? = null,
    onClearExternalResults: (() -> Unit)? = null,
    savedQuery: String = "",
    savedResults: List<Map<String, String>> = emptyList(),
    savedSearchMode: SearchMode = SearchMode.ARABIC,
    onSaveState: (String, List<Map<String, String>>, SearchMode) -> Unit = { _, _, _ -> },
    showHarakat: Boolean = false,
    onToggleHarakat: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(savedQuery) }
    var results by remember { mutableStateOf(savedResults) }
    var searchMode by remember { mutableStateOf(savedSearchMode) }
    var searching by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val imm = remember { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    val displayResults = if (externalResults != null && externalResults.isNotEmpty()) externalResults else results
    LaunchedEffect(query, results, searchMode) { onSaveState(query, results, searchMode) }

    val filterOptions = listOf(null to "Word", "noun" to "Noun", "adjective" to "Adjective", "verb" to "Verb", "feminine" to "Feminine", "plural" to "Plural", "masdar" to "Masdar", "alt_form" to "Alt Form")

    fun doSearch(t: String): List<Map<String, String>> {
        return when {
            t.startsWith("\"") && t.endsWith("\"") -> {
                val exact = db.searchEnglishStarting(t.removeSurrounding("\""))
                if (exact.isNotEmpty()) exact
                else db.searchEnglish(t.removeSurrounding("\""))
            }
            t.contains("*") || t.contains("?") -> db.searchWildcard(t)
            searchMode == SearchMode.ARABIC -> {
                val d = db.searchArabic(t)
                val exactMatch = d.filter { it["word"] == t }
                if (exactMatch.isNotEmpty()) {
                    exactMatch
                } else if (d.isNotEmpty()) {
                    d
                } else {
                    val l = db.findLemma(t)
                    if (l != null) listOf(l) else emptyList()
                }
            }
            else -> db.searchEnglish(t)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Box {
                    TextButton(onClick = { menuExpanded = true }, contentPadding = PaddingValues(0.dp)) {
                        Text(when { externalResults != null && externalResults.isNotEmpty() -> "Root Results"; selectedTag == null -> "Search: ${if (searchMode == SearchMode.ARABIC) "Word" else "English"}"; selectedTag == "noun" -> "Search: Noun"; selectedTag == "adjective" -> "Search: Adjective"; selectedTag == "verb" -> "Search: Verb"; selectedTag == "plural" -> "Search: Plural"; selectedTag == "masdar" -> "Search: Masdar"; selectedTag == "alt_form" -> "Search: Alt Form"; else -> "Search: Word" }, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text(" ▼", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        filterOptions.forEach { (tag, label) ->
                            DropdownMenuItem(text = { Row(verticalAlignment = Alignment.CenterVertically) { if (tag == selectedTag || (tag == null && selectedTag == null)) { Text("✓ ", color = JshoBrown, fontWeight = FontWeight.Bold) } else { Spacer(modifier = Modifier.width(20.dp)) }; Text(label, fontWeight = if (tag == selectedTag) FontWeight.Bold else FontWeight.Normal) } }, onClick = { selectedTag = tag; menuExpanded = false; if (query.isNotBlank() || tag != null) { searching = true; scope.launch { val t = query.trim(); results = withContext(Dispatchers.IO) { if (selectedTag != null) db.searchByTag(selectedTag!!, t) else doSearch(t) }; searching = false } } })
                        }
                    }
                }
            },
            navigationIcon = { if (externalResults != null && externalResults.isNotEmpty()) TextButton(onClick = { onClearExternalResults?.invoke() }) { Text("←", color = Color.White, fontSize = 20.sp) } },
            actions = {
                TextButton(onClick = { searchMode = if (searchMode == SearchMode.ARABIC) SearchMode.ENGLISH else SearchMode.ARABIC; query = ""; results = emptyList() }) { Text(if (searchMode == SearchMode.ARABIC) "🇸🇦" else "🇬🇧", fontSize = 22.sp) }
                TextButton(onClick = onRootSearchClicked) { Text("جذر", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                TextButton(onClick = { onToggleHarakat?.invoke() }) { Text(if (showHarakat) "حَ" else "ح", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (showHarakat) JshoGreen else Color.White) }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = JshoBrown, titleContentColor = Color.White)
        )

        if (externalResults == null || externalResults.isEmpty()) {
            OutlinedTextField(
                value = query,
                onValueChange = { newQuery ->
                    query = newQuery
                    searchJob?.cancel()
                    if (newQuery.length >= 1 || selectedTag != null) {
                        searching = true
                        searchJob = scope.launch {
                            delay(300)
                            val t = newQuery.trim()
                            val parsedNumber = NumberGrammar.parseNumber(t)
                            if (parsedNumber != null && parsedNumber in 1..9999) {
                                val mascNoun = withContext(Dispatchers.IO) { db.getRandomNoun("masculine") }
                                val femNoun = withContext(Dispatchers.IO) { db.getRandomNoun("feminine") }
                                val mascNounClean = NumberGrammar.stripHarakat(mascNoun)
                                val femNounClean = NumberGrammar.stripHarakat(femNoun)
                                val result = NumberGrammar.getNumberForm(parsedNumber, mascNounClean, femNounClean)
                                results = listOf(mapOf("word" to "عدد $parsedNumber", "defs" to NumberGrammar.formatResult(result), "pos" to "number", "romanized" to ""))
                                searching = false
                                return@launch
                            }
                            results = withContext(Dispatchers.IO) {
                                if (selectedTag != null) db.searchByTag(selectedTag!!, t)
                                else doSearch(t)
                            }
                            searching = false
                        }
                    } else {
                        results = emptyList()
                        searching = false
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                placeholder = { Text(if (searchMode == SearchMode.ARABIC) "Search: كلمة, Kalima" else "Search english word...", color = JshoGray) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus(); imm.hideSoftInputFromWindow(null, 0) }),
                leadingIcon = { Text(if (searchMode == SearchMode.ARABIC) "ع" else "EN", fontWeight = FontWeight.Bold, color = JshoBrown, modifier = Modifier.padding(start = 8.dp)) },
                trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = ""; results = emptyList() }, contentPadding = PaddingValues(0.dp)) { Text("✕", fontSize = 16.sp, color = JshoGray) } },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JshoBrown, unfocusedBorderColor = JshoDivider, focusedContainerColor = JshoWhite, unfocusedContainerColor = JshoWhite)
            )
        } else {
            Text("${displayResults.size} pwords found", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = JshoBrown, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
        }

        if (displayResults.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(displayResults) { w ->
                    val foundWord = w["word"] ?: ""
                    val searchedWord = query.trim().replace(Regex("[\\u064B-\\u065F]"), "")
                    val isFromLemma = query.isNotBlank() && searchedWord != foundWord
                    val formInfo = w["forms_info"] ?: ""
                    Log.d("LEMMA_BUG", "query='$query', searchedWord='$searchedWord', foundWord='$foundWord', isFromLemma=$isFromLemma, formInfo='$formInfo'")

                    Column(modifier = Modifier.fillMaxWidth()) {
                        SearchResultCard(
                            word = w,
                            onClick = {
                                focusManager.clearFocus()
                                imm.hideSoftInputFromWindow(null, 0)
                                onWordSelected(w)
                            },
                            showHarakat = showHarakat
                        )

                        if (isFromLemma) {
                            Text(
                                if (formInfo.isNotBlank()) "→ $formInfo" else "→ lemma",
                                fontSize = 11.sp,
                                color = JshoOrange,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        } else if ((query.isNotEmpty() || selectedTag != null) && !searching && (externalResults == null || externalResults.isEmpty())) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No results", color = JshoGray, fontSize = 16.sp) }
        }
    }
}