package it.Seltz.Qmus.ui

import it.Seltz.Qmus.data.conjugation.VerbConjugator
import it.Seltz.Qmus.data.conjugation.ConjugationForm
import it.Seltz.Qmus.data.conjugation.ConjugationManager
import it.Seltz.Qmus.data.DictDatabase
import it.Seltz.Qmus.data.detectByRomanization
import it.Seltz.Qmus.data.detectWazn
import it.Seltz.Qmus.data.detectWaznSimple
import it.Seltz.Qmus.data.HarakatGenerator
import it.Seltz.Qmus.data.VocalizedLoader
import it.Seltz.Qmus.data.Screen
import it.Seltz.Qmus.data.SearchMode
import it.Seltz.Qmus.data.NumberGrammar
import it.Seltz.Qmus.theme.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbConjugationPanel(
    arabicWord: String,
    root: String,
    verbForm: String,
    romanized: String,
    pos: String
) {
    if (!pos.contains("verb", ignoreCase = true)) {
        Text("Not a verb", color = JshoGray)
        return
    }

    var selectedTense by remember { mutableStateOf("past") }
    var isPassive by remember { mutableStateOf(false) }

    val cleanWord = arabicWord.replace(Regex("[\\u064B-\\u065F\\u0640]"), "")

    val allForms = remember(cleanWord, root, verbForm, romanized) {
        var effectiveRoot = if (root.isNotBlank()) root.replace(Regex("[\\s\\-،]"), "") else ""

        if (effectiveRoot.isBlank()) {
            val bare = cleanWord.filter { it in '\u0621'..'\u064A' }
            if (bare.length >= 3) {
                effectiveRoot = when {
                    bare.startsWith("أ") && bare.length == 4 -> bare.drop(1).chunked(1).joinToString("-")
                    bare.contains("ا") && !bare.startsWith("ا") && bare.length == 4 -> bare.filter { it != 'ا' }.chunked(1).joinToString("-")
                    else -> bare.take(3).chunked(1).joinToString("-")
                }
            }
        }

        if (effectiveRoot.isNotBlank()) {
            val detectedForm = detectByRomanization(romanized, effectiveRoot)

            // Check for Form III pattern: first radical + ā + second radical
            val r = effectiveRoot.replace("-", "")
            if (r.length >= 3) {
                val r1 = r[0]; val r2 = r[1]
                // Form III has ā between r1 and r2 in the past
                if (cleanWord.contains("ا") && cleanWord.indexOf("ا") > 0 && cleanWord.indexOf("ا") < cleanWord.length - 1) {
                    // Could be Form III
                }
            }

            val form = when {
                verbForm.isNotBlank() -> verbForm.trim()
                detectedForm != null && detectedForm.startsWith("FORM_") -> detectedForm.removePrefix("FORM_").replace("_", " ").trim()
                // Deduction from word pattern
                cleanWord.startsWith("أ") && cleanWord.length == 4 -> "IV"
                cleanWord.contains("ا") && !cleanWord.startsWith("ا") && cleanWord.length == 4 -> "III"
                else -> "I"
            }

            VerbConjugator.generateByForm(effectiveRoot, form)
        } else {
            emptyList()
        }
    }

    // Filtra per tempo/umore
    val filteredForms = remember(allForms, selectedTense, isPassive) {
        allForms.filter { form ->
            val tense = form.tense.ifEmpty {
                if (form.description.lowercase().contains("past") && !form.description.lowercase().contains("non-past")) "past" else "non-past"
            }
            val mood = form.mood.ifEmpty {
                val d = form.description.lowercase()
                when {
                    d.contains("subjunctive") -> "subjunctive"
                    d.contains("jussive") -> "jussive"
                    d.contains("imperative") -> "imperative"
                    d.contains("indicative") -> "indicative"
                    d.contains("non-past") -> "indicative"
                    else -> ""
                }
            }
            val voice = form.voice.ifEmpty {
                if (form.description.lowercase().contains("passive")) "passive" else "active"
            }

            val tenseMatch = when (selectedTense) {
                "past" -> tense == "past"
                "present" -> tense == "non-past" && mood == "indicative"
                "subjunctive" -> mood == "subjunctive"
                "jussive" -> mood == "jussive"
                "imperative" -> mood == "imperative"
                else -> true
            }
            val voiceMatch = if (isPassive) voice == "passive" else voice == "active"
            tenseMatch && voiceMatch
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Tense chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tenses = listOf(
                "past" to "Past",
                "present" to "Present",
                "subjunctive" to "Subjunctive",
                "jussive" to "Jussive",
                "imperative" to "Imperative"
            )
            tenses.forEach { (value, label) ->
                FilterChip(
                    selected = selectedTense == value,
                    onClick = { selectedTense = value },
                    label = { Text(label, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JshoBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.LightGray.copy(alpha = 0.3f),
                        labelColor = Color.Black
                    )
                )
            }
        }

        // Active/Passive toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (isPassive) "Passive" else "Active",
                fontSize = 12.sp,
                color = if (isPassive) JshoBlue else JshoGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isPassive,
                onCheckedChange = { isPassive = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = JshoBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tabella coniugazione
        if (filteredForms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No forms for this tense/mood", color = JshoGray)
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JshoWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(JshoBrown.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Text("Person", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
                        Text("Form", modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
                    }

                    // Rows
                    filteredForms.forEach { form ->
                        val person = if (form.person.isNotBlank()) {
                            formatPerson(form.person, form.gender, form.number)
                        } else {
                            extractPersonFromDesc(form.description)
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(person, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                            Text(form.form, modifier = Modifier.weight(2f), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JshoBlue, textAlign = TextAlign.Center)
                        }
                        Divider(color = JshoDivider.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

private fun formatPerson(person: String, gender: String, number: String): String {
    return when {
        person == "3" && gender == "m" && number == "s" -> "He"
        person == "3" && gender == "f" && number == "s" -> "She"
        person == "2" && gender == "m" && number == "s" -> "You (m)"
        person == "2" && gender == "f" && number == "s" -> "You (f)"
        person == "1" && number == "s" -> "I"
        person == "1" && number == "p" -> "We"
        person == "3" && gender == "m" && number == "d" -> "They two (m)"
        person == "3" && gender == "f" && number == "d" -> "They two (f)"
        person == "3" && gender == "m" && number == "p" -> "They (m)"
        person == "3" && gender == "f" && number == "p" -> "They (f)"
        person == "2" && number == "d" -> "You two"
        person == "2" && gender == "m" && number == "p" -> "You (m pl)"
        person == "2" && gender == "f" && number == "p" -> "You (f pl)"
        else -> "${person}${gender}${number}"
    }
}

private fun extractPersonFromDesc(desc: String): String {
    return when {
        desc.contains("first-person") && desc.contains("singular") -> "I"
        desc.contains("first-person") && desc.contains("plural") -> "We"
        desc.contains("second-person") && desc.contains("masculine") && desc.contains("singular") -> "You (m)"
        desc.contains("second-person") && desc.contains("feminine") && desc.contains("singular") -> "You (f)"
        desc.contains("second-person") && desc.contains("dual") -> "You two"
        desc.contains("second-person") && desc.contains("plural") -> "You"
        desc.contains("third-person") && desc.contains("masculine") && desc.contains("singular") -> "He"
        desc.contains("third-person") && desc.contains("feminine") && desc.contains("singular") -> "She"
        desc.contains("third-person") && desc.contains("dual") -> "They two"
        desc.contains("third-person") && desc.contains("plural") -> "They"
        desc.contains("imperative") && desc.contains("singular") -> "You!"
        desc.contains("imperative") && desc.contains("dual") -> "You two!"
        desc.contains("imperative") && desc.contains("plural") -> "You!"
        else -> desc.take(20)
    }
}