package it.Seltz.Qmus.ui

import it.Seltz.Qmus.data.detectByRomanization
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.data.*
import it.Seltz.Qmus.theme.*

@Composable
fun SearchResultCard(
    word: Map<String, String>,
    onClick: () -> Unit,
    showHarakat: Boolean = false
) {
    val arabicWord = word["word"] ?: ""
    val romanized = word["romanized"] ?: ""
    val pos = word["pos"] ?: ""
    val root = word["root"] ?: ""
    val defs = word["defs"]?.split(" ||| ")?.filter { it.isNotBlank() }?.distinct() ?: emptyList()
    val frequencyRank = word["frequency_rank"]?.toIntOrNull() ?: 999999
    val verbPattern = word["verb_pattern"] ?: ""
    val vocalizedFromDb = word["vocalized"] ?: ""
    val vocalizedFromFile = VocalizedLoader.getVocalized(arabicWord)
    val vocalizedGenerated = if (romanized.isNotBlank()) HarakatGenerator.addHarakat(arabicWord, romanized) else null

    val effectiveVocalized = when {
        vocalizedGenerated != null -> vocalizedGenerated
        vocalizedFromFile != null -> vocalizedFromFile
        else -> vocalizedFromDb
    }
    val displayWord = if (showHarakat && effectiveVocalized.isNotBlank()) effectiveVocalized else arabicWord
    val extraTags = word["extra_tags"] ?: ""
    val isNumber = pos == "number"

    val firstDef = defs.firstOrNull() ?: ""

// Check if romanization looks like a verb pattern
    val looksLikeVerb = romanized.endsWith("a") ||  // past tense: kataba, kassara
            romanized.endsWith("ū") ||  // past plural: katabū
            romanized.endsWith("ā") ||  // past dual: katabā
            romanized.startsWith("ta") ||  // Form V, VI
            romanized.startsWith("in") ||  // Form VII
            romanized.startsWith("ista") ||  // Form X
            (romanized.startsWith("i") && romanized.contains("ta")) ||  // Form VIII
            romanized.contains("ّ")  // shadda indicates Form II or V

    val isVerb = (pos.contains("verb", ignoreCase = true))
            && !romanized.endsWith("in")
            && !romanized.endsWith("an")
            && !romanized.endsWith("un")

    if (pos.contains("verb", ignoreCase = true) || word["type"] == "verb") {
        Log.d("VERB_DEBUG", "word=$arabicWord, pos=$pos, type=${word["type"]}, romanized=$romanized, firstDef='${firstDef.take(50)}', isVerb=$isVerb")
    }

    val isPlural = firstDef.trimStart().startsWith("plural of", ignoreCase = true)
    val isMasdar = ((firstDef.trimStart().startsWith("verbal noun of", ignoreCase = true)
            || (word["masdar"] ?: "").isNotBlank())
            && !isVerb
            && !romanized.endsWith("in")
            && !romanized.endsWith("an")
            && !romanized.endsWith("un"))
    val isFeminine = firstDef.trimStart().startsWith("female equivalent of", ignoreCase = true)
    val isFemininePlural = extraTags.contains("feminine_plural")

    // Noun detection: explicit noun or masdar
    val isNoun = pos.contains("noun", ignoreCase = true) || isMasdar

    var expanded by remember { mutableStateOf(false) }
    val visibleDefs = if (expanded || isNumber) defs else defs.take(3)

    val waznInfo = remember(arabicWord, isVerb, root, romanized) {
        if (isVerb) null
        else detectWazn(arabicWord, pos, root, romanized) ?: detectWaznSimple(arabicWord)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isVerb) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = displayWord, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (romanized.isNotBlank()) Text(text = romanized, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = JshoBrown, modifier = Modifier.padding(top = 2.dp))
            }
            if (isVerb) Text(text = "›", fontSize = 28.sp, color = JshoGray, modifier = Modifier.padding(start = 8.dp))
        }

        Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Frequency badges
            when {
                frequencyRank <= 100 -> BadgeChip("common", JshoGreen)
                frequencyRank <= 500 -> BadgeChip("frequent", JshoBlue)
            }

            // Part of speech - mutually exclusive: verb, masdar, noun, adjective, other
            if (isMasdar) {
                BadgeChip("masdar", JshoGreen)
            } else if (isVerb) {
                BadgeChip("verb", JshoOrange)
            } else if (isNoun && !isNumber) {
                BadgeChip("noun", JshoBrown)
            } else if (pos.isNotBlank() && !isNumber) {
                BadgeChip(
                    when {
                        pos.contains("adj") -> "adjective"
                        else -> pos
                    },
                    when {
                        pos.contains("adj") -> JshoBlue
                        else -> JshoGray
                    }
                )
            }

            if (isNumber) BadgeChip("number", JshoBlue)
            if (isPlural) BadgeChip("plural", JshoOrange)
            if (isFeminine) BadgeChip("feminine", Color(0xFFFF69B4))
            if (isFemininePlural) BadgeChip("f. plural", Color(0xFFFF69B4))
            if (waznInfo != null) BadgeChip(waznInfo.pattern, JshoBlue)
            if (root.isNotBlank()) BadgeChip("√$root", JshoPurple)

            // Gender badges
            val gender = word["gender"] ?: ""
            if (gender == "masculine") BadgeChip("m", JshoBlue)
            if (gender == "feminine") BadgeChip("f", Color(0xFFFF69B4))
            if (gender == "both") BadgeChip("m/f", JshoPurple)

            // Number badges
            val number = word["number"] ?: ""
            if (number == "dual") BadgeChip("dual", JshoOrange)
            if (number == "collective") BadgeChip("coll", JshoGreen)
            if (number == "singulative") BadgeChip("singulative", JshoGreen)

            // Other grammatical badges
            val declension = word["declension"] ?: ""
            if (declension == "diptote") BadgeChip("dipt", JshoGray)
            val degree = word["degree"] ?: ""
            if (degree == "elative") BadgeChip("elat", JshoOrange)

            // Verb form badge (only for verbs)
            val verbForm = word["verb_form"] ?: ""
            val effectiveVerbForm = if (verbForm.isNotBlank()) verbForm else {
                val detected = detectByRomanization(romanized, root)
                if (detected != null && detected.startsWith("FORM_") && isVerb) {
                    detected.removePrefix("FORM_").replace("_", " ")
                } else ""
            }
            if (effectiveVerbForm.isNotBlank() && effectiveVerbForm != "I") BadgeChip("F.$effectiveVerbForm", JshoOrange)
        }

        if (defs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Column {
                visibleDefs.forEach { def ->
                    Text("• $def", fontSize = 14.sp, color = Color.DarkGray, maxLines = if (isNumber) Int.MAX_VALUE else 2, modifier = Modifier.padding(top = 1.dp))
                }
            }
            if (defs.size > 3 && !expanded && !isNumber) {
                TextButton(onClick = { expanded = true }, modifier = Modifier.padding(top = 0.dp), contentPadding = PaddingValues(vertical = 0.dp)) {
                    Text("show all (${defs.size})", fontSize = 12.sp, color = JshoBlue)
                }
            }
            if (expanded && defs.size > 3 && !isNumber) {
                TextButton(onClick = { expanded = false }, modifier = Modifier.padding(top = 0.dp), contentPadding = PaddingValues(vertical = 0.dp)) {
                    Text("collapse", fontSize = 12.sp, color = JshoBlue)
                }
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = JshoDivider)
    }
}