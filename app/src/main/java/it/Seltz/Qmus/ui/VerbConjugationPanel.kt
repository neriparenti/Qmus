package it.Seltz.Qmus.ui

import it.Seltz.Qmus.data.conjugation.ConjugationForm
import it.Seltz.Qmus.data.DictDatabase
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerbConjugationPanel(
    arabicWord: String,
    root: String,
    verbForm: String,
    romanized: String,
    pos: String,
    verbPattern: String = "",
    wordId: String = ""
) {
    if (!pos.contains("verb", ignoreCase = true)) {
        Text("Not a verb", color = JshoGray)
        return
    }

    var selectedTense by remember { mutableStateOf("past") }
    var isPassive by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val patterns = remember(verbPattern) {
        Regex("Form I: ([aiu])/([aiu])").findAll(verbPattern)
            .map { "${it.groupValues[1]}/${it.groupValues[2]}" }
            .toList()
            .distinct()
    }

    var selectedPattern by remember(patterns) { mutableStateOf(patterns.firstOrNull() ?: "a/u") }

    val allForms = remember(wordId) {
        android.util.Log.d("DB_DEBUG", "wordId='$wordId'")
        if (wordId.isNotBlank()) {
            val db = DictDatabase.getInstance(context)
            val rows = db.getVerbFormsWithMetadata(wordId)
            android.util.Log.d("DB_DEBUG", "rows=${rows.size}")
            rows.map { row ->
                val t = row["tense"] ?: ""
                val m = row["mood"] ?: ""
                val v = row["voice"] ?: ""
                ConjugationForm(
                    form = row["form"] ?: "",
                    description = "$t $m $v".trim(),
                    tense = t,
                    mood = m,
                    voice = v,
                    person = row["person"] ?: "",
                    gender = row["gender"] ?: "",
                    number = row["number"] ?: ""
                )
            }
        } else {
            emptyList()
        }
    }


    val filteredForms = remember(allForms, selectedTense, isPassive) {
        allForms.filter { form ->
            val tense = form.tense
            val mood = form.mood
            val voice = form.voice.ifEmpty { "active" }

            val tenseMatch = when (selectedTense) {
                "past" -> tense == "past"
                "present" -> tense == "non-past" && mood == "indicative"
                "subjunctive" -> mood == "subjunctive"
                "jussive" -> mood == "jussive"
                "imperative" -> tense == "imperative" || mood == "imperative"
                else -> true
            }
            val voiceMatch = if (isPassive) voice == "passive" else voice != "passive"
            tenseMatch && voiceMatch
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (patterns.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                patterns.forEach { pattern ->
                    val label = when(pattern) {
                        "a/a" -> "a-a (فَعَلَ-يَفْعَلُ)"
                        "a/i" -> "a-i (فَعَلَ-يَفْعِلُ)"
                        "a/u" -> "a-u (فَعَلَ-يَفْعُلُ)"
                        "i/a" -> "i-a (فَعِلَ-يَفْعَلُ)"
                        "i/i" -> "i-i (فَعِلَ-يَفْعِلُ)"
                        "u/u" -> "u-u (فَعُلَ-يَفْعُلُ)"
                        else -> pattern
                    }
                    FilterChip(
                        selected = selectedPattern == pattern,
                        onClick = { selectedPattern = pattern },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JshoPurple, selectedLabelColor = Color.White,
                            containerColor = Color.LightGray.copy(alpha = 0.3f), labelColor = Color.Black
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("past" to "Past", "present" to "Present", "subjunctive" to "Subjunctive", "jussive" to "Jussive", "imperative" to "Imperative").forEach { (value, label) ->
                FilterChip(
                    selected = selectedTense == value,
                    onClick = { selectedTense = value },
                    label = { Text(label, fontSize = 13.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JshoBlue, selectedLabelColor = Color.White,
                        containerColor = Color.LightGray.copy(alpha = 0.3f), labelColor = Color.Black
                    )
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            Text(if (isPassive) "Passive" else "Active", fontSize = 12.sp, color = if (isPassive) JshoBlue else JshoGray, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = isPassive, onCheckedChange = { isPassive = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = JshoBlue, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color.LightGray))
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredForms.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("No forms for this tense/mood", color = JshoGray)
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = JshoWhite), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().background(JshoBrown.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(8.dp)) {
                        Text("Person", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
                        Text("Form", modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
                    }
                    filteredForms.forEach { form ->
                        val person = getPerson(form)
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

private fun getPerson(form: ConjugationForm): String {
    if (form.person.isNotBlank()) return formatPerson(form.person, form.gender, form.number)
    return extractPersonFromDesc(form.description)
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
        else -> desc.take(20)
    }
}