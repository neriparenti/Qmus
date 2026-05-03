package it.Seltz.Qmus.ui

import it.Seltz.Qmus.data.HarakatGenerator
import it.Seltz.Qmus.data.VocalizedLoader
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.Seltz.Qmus.data.*
import it.Seltz.Qmus.theme.*
import it.Seltz.Qmus.util.arabicWordRegex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private var cachedRootMap: Map<String, String>? = null

private fun lookupRootFromFile(context: Context, word: String): String {
    if (cachedRootMap == null) {
        try {
            val json = context.assets.open("roots.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, String>>() {}.type
            cachedRootMap = Gson().fromJson(json, type)
        } catch (e: Exception) {
            cachedRootMap = emptyMap()
        }
    }
    return cachedRootMap?.get(word) ?: ""
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WordDetailScreen(word: Map<String, String>, onWordClick: (String) -> Unit, onBack: () -> Unit, showHarakat: Boolean = false) {
    val arabicWord = word["word"] ?: ""
    val romanized = word["romanized"] ?: ""
    val pos = word["pos"] ?: ""
    val root = word["root"] ?: ""
    val vocalizedFromDb = word["vocalized"] ?: ""
    val vocalizedFromFile = VocalizedLoader.getVocalized(arabicWord)
    val vocalizedGenerated: String? = if (romanized.isNotBlank()) {
        HarakatGenerator.addHarakat(arabicWord, romanized)
    } else null

    val effectiveVocalized: String = when {
        vocalizedGenerated != null -> vocalizedGenerated
        vocalizedFromFile != null -> vocalizedFromFile
        else -> vocalizedFromDb
    }
    val displayWord = if (showHarakat && effectiveVocalized.isNotBlank()) effectiveVocalized else arabicWord
    val defs = word["defs"]?.split(" ||| ")?.filter { it.isNotBlank() } ?: emptyList()
    val extraTags = word["extra_tags"] ?: ""
    val frequencyRank = word["frequency_rank"]?.toIntOrNull() ?: 999999
    val verbPatternRaw = word["verb_pattern"] ?: ""
    val allPatterns = verbPatternRaw.split(" | ").filter { it.isNotBlank() }
    val verbPattern = allPatterns.firstOrNull() ?: ""
    val isFromDb = verbPattern.isNotBlank()
    val context = LocalContext.current
    val db = remember { DictDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    var homographOptions by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var showHomographDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(displayWord, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            navigationIcon = { TextButton(onClick = onBack) { Text("←", color = Color.White, fontSize = 20.sp) } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = JshoBrown, titleContentColor = Color.White)
        )
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(displayWord, fontSize = 36.sp, fontWeight = FontWeight.Bold, color = JshoBrown)
                if (romanized.isNotBlank()) Text("[$romanized]", fontSize = 18.sp, color = JshoGray)
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    if (pos.isNotBlank()) BadgeChip(pos, JshoBrown)
                    if (root.isNotBlank()) { Spacer(modifier = Modifier.width(8.dp)); BadgeChip("√$root", JshoBlue) }
                }
                if (frequencyRank <= 1000) {
                    Spacer(modifier = Modifier.height(4.dp))
                    BadgeChip(when { frequencyRank <= 100 -> "⭐ Very common"; frequencyRank <= 500 -> "📖 Frequent"; else -> "📚 Common" }, JshoGreen)
                }
            }

            // Box verb
            if (pos.contains("verb", ignoreCase = true)) {
                item {
                    val cleanWord = arabicWord.replace(Regex("[\\u064B-\\u065F\\u0640]"), "")
                    val verbForm = word["verb_form"] ?: ""
                    var effectiveRoot = root
                    if (effectiveRoot.isBlank()) effectiveRoot = lookupRootFromFile(context, cleanWord)
                    if (effectiveRoot.isBlank()) {
                        val bare = cleanWord.filter { it in '\u0621'..'\u064A' }
                        if (bare.length >= 3) {
                            // For Form III (contains alif in the middle), take first 3 chars
                            // For Form IV (starts with أ), drop the first char
                            effectiveRoot = when {
                                bare.startsWith("أ") && bare.length == 4 -> bare.drop(1).chunked(1).joinToString("-")
                                bare.contains("ا") && !bare.startsWith("ا") && bare.length == 4 -> bare.filter { it != 'ا' }.chunked(1).joinToString("-")
                                else -> bare.take(3).chunked(1).joinToString("-")
                            }
                        }
                    }

                    if (effectiveRoot.isNotBlank()) {
                        val r = effectiveRoot.replace(Regex("[\\s\\-،]"), "")
                        var form = "I"
                        var isKnownForm = false
                        var fromDb = false
                        var pastVowel = "a"
                        var presVowel = "u"
                        var desc = ""

                        if (r.length >= 3) {
                            val detectedForm = detectByRomanization(romanized, effectiveRoot)

                            // Try to get form from verbPattern first
                            val match = Regex("Form ([IVX]+): ([aiu])/([aiu])").find(verbPattern)

                            if (match != null) {
                                // verbPattern has form info, use it
                                form = match.groupValues[1]
                                pastVowel = match.groupValues[2]
                                presVowel = match.groupValues[3]
                                desc = when (form) {
                                    "I" -> when {
                                        pastVowel == "a" && presVowel == "u" -> "Transitive/active"
                                        pastVowel == "a" && presVowel == "i" -> "State/result"
                                        pastVowel == "a" && presVowel == "a" -> "Perception"
                                        pastVowel == "i" && presVowel == "a" -> "Temporary state"
                                        pastVowel == "u" && presVowel == "u" -> "Permanent quality"
                                        else -> ""
                                    }
                                    "II" -> "Intensive/causative"
                                    "III" -> "Reciprocal/associative"
                                    "IV" -> "Causative"
                                    "V" -> "Reflexive of intensive"
                                    "VI" -> "Reflexive of reciprocal"
                                    "VII" -> "Passive/reflexive"
                                    "VIII" -> "Reflexive/middle"
                                    "IX" -> "Colors/defects"
                                    "X" -> "Request"
                                    else -> ""
                                }
                                fromDb = true
                                isKnownForm = true
                            } else if (detectedForm != null && detectedForm.startsWith("FORM_")) {
                                // detectedForm from romanization
                                form = detectedForm.removePrefix("FORM_").replace("_", " ")
                                pastVowel = "?"
                                presVowel = "?"
                                desc = when (form) {
                                    "II" -> "Intensive/causative"
                                    "III" -> "Reciprocal/associative"
                                    "IV" -> "Causative"
                                    "VII" -> "Passive/reflexive"
                                    "VIII" -> "Reflexive/middle"
                                    "IX" -> "Colors/defects"
                                    "X" -> "Request"
                                    else -> ""
                                }
                                fromDb = false
                                isKnownForm = true
                            } else if (verbForm.isNotBlank()) {
                                // verbForm from database
                                form = verbForm.trim()
                                isKnownForm = true
                            } else {
                                // Deduce form from word pattern
                                form = when {
                                    cleanWord.startsWith("أ") && cleanWord.length == 4 -> "IV"
                                    cleanWord.contains("ا") && !cleanWord.startsWith("ا") && cleanWord.length == 4 -> "III"
                                    else -> "I"
                                }
                                isKnownForm = form != "I"
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = JshoOrange.copy(alpha = 0.1f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val titleForm = if (isKnownForm) form else "I"
                                    Text(
                                        "VERB FORM $titleForm${if (!fromDb && !isKnownForm) " (estimated)" else if (!fromDb) " (~roman)" else ""}",
                                        fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JshoOrange
                                    )
                                    if (desc.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(desc, fontSize = 12.sp, color = JshoGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tab Row
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = JshoDivider)

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = JshoWhite,
                    contentColor = JshoBrown,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text("Definitions",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                    )
                    if (pos.contains("verb", ignoreCase = true)) {
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text("Conjugation",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }

            // Conjugation tab
            if (selectedTab == 1 && pos.contains("verb", ignoreCase = true)) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    VerbConjugationPanel(
                        arabicWord = arabicWord,
                        root = root,
                        verbForm = word["verb_form"] ?: "",
                        romanized = romanized,
                        pos = pos
                    )
                }
            }

            // Definitions tab
            if (selectedTab == 0) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Definitions:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = JshoBrown)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(defs.size) { index ->
                    val d = defs[index]
                    val ip = d.trimStart().startsWith("plural of", true)
                    val im = d.trimStart().startsWith("verbal noun of", true)
                    val ia = d.trimStart().startsWith("alternative form of", true)
                    val iFem = d.trimStart().startsWith("female equivalent of", true) || d.trimStart().startsWith("feminine of", true) || arabicWord.endsWith("ة")
                    var showCopyDialog by remember { mutableStateOf(false) }

                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), colors = CardDefaults.cardColors(containerColor = JshoWhite), elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Text("${index + 1}.", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JshoBrown, modifier = Modifier.width(24.dp))
                            Column {
                                if (ip || im || ia || iFem) {
                                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                                        val gender = word["gender"] ?: ""
                                        if (gender == "masculine") BadgeChip("maschile", JshoBlue)
                                        if (gender == "feminine") BadgeChip("femminile", Color(0xFFFF69B4))
                                        if (gender == "both") BadgeChip("m/f", JshoPurple)
                                        val declension = word["declension"] ?: ""
                                        if (declension == "diptote") BadgeChip("diptota", JshoGray)
                                        val degree = word["degree"] ?: ""
                                        if (degree == "elative") BadgeChip("elativo", JshoOrange)
                                        val number = word["number"] ?: ""
                                        if (number == "dual") BadgeChip("duale", JshoOrange)
                                        if (number == "collective") BadgeChip("collettivo", JshoGreen)
                                    }
                                }
                                Text(text = d, style = TextStyle(fontSize = 15.sp, color = Color.DarkGray),
                                    modifier = Modifier.pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = { showCopyDialog = true },
                                            onTap = {
                                                scope.launch {
                                                    val text = arabicWordRegex.find(d)?.value ?: d
                                                    val results = withContext(Dispatchers.IO) { db.searchAllByArabicText(text) }
                                                    if (results.size == 1) onWordClick(results[0]["word"] ?: "")
                                                    else if (results.size > 1) { homographOptions = results; showHomographDialog = true }
                                                }
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    if (showCopyDialog) {
                        AlertDialog(
                            onDismissRequest = { showCopyDialog = false },
                            title = { Text("Copy", fontWeight = FontWeight.Bold) },
                            text = {
                                Column {
                                    TextButton(onClick = {
                                        val txt = arabicWordRegex.findAll(d).map { it.value }.joinToString(" ")
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cb.setPrimaryClip(ClipData.newPlainText("text", txt))
                                        Toast.makeText(context, "Arabic copied!", Toast.LENGTH_SHORT).show()
                                        showCopyDialog = false
                                    }) { Text("📝 Copy Arabic Text", fontSize = 16.sp) }
                                    TextButton(onClick = {
                                        val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        cb.setPrimaryClip(ClipData.newPlainText("text", d))
                                        Toast.makeText(context, "All copied!", Toast.LENGTH_SHORT).show()
                                        showCopyDialog = false
                                    }) { Text("📋 Copy all", fontSize = 16.sp) }
                                }
                            },
                            confirmButton = { TextButton(onClick = { showCopyDialog = false }) { Text("Cancel", color = JshoGray) } }
                        )
                    }
                }
            }
        }
    }

    if (showHomographDialog) {
        WordChoiceDialog(options = homographOptions, onDismiss = { showHomographDialog = false }, onSelect = { selected -> showHomographDialog = false; onWordClick(selected["word"] ?: "") })
    }
}