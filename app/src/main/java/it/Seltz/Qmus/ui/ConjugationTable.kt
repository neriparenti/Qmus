package it.Seltz.Qmus.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.data.conjugation.ConjugationForm
import it.Seltz.Qmus.theme.*

data class ConjugationGroup(
    val title: String,
    val forms: List<ConjugationForm>
)

@Composable
fun ConjugationTable(lemma: String, forms: List<ConjugationForm>) {
    if (forms.isEmpty()) return

    val groups = LinkedHashMap<String, MutableList<ConjugationForm>>()

    for (form in forms) {
        if (form.description == "inflection") continue

        val key = when {
            form.tense == "past" && form.voice == "active" -> "Past Active"
            form.tense == "past" && form.voice == "passive" -> "Past Passive"
            form.tense == "non-past" && form.mood == "indicative" && form.voice == "active" -> "Present Active"
            form.tense == "non-past" && form.mood == "indicative" && form.voice == "passive" -> "Present Passive"
            form.tense == "non-past" && form.mood == "subjunctive" && form.voice == "active" -> "Subjunctive Active"
            form.tense == "non-past" && form.mood == "subjunctive" && form.voice == "passive" -> "Subjunctive Passive"
            form.tense == "non-past" && form.mood == "jussive" && form.voice == "active" -> "Jussive Active"
            form.tense == "non-past" && form.mood == "jussive" && form.voice == "passive" -> "Jussive Passive"
            // Fallback per forme vecchie
            form.tense.isEmpty() -> {
                val desc = form.description.lowercase()
                when {
                    desc.contains("past") && !desc.contains("non-past") && desc.contains("active") -> "Past Active"
                    desc.contains("past") && !desc.contains("non-past") && desc.contains("passive") -> "Past Passive"
                    desc.contains("non-past") && desc.contains("indicative") && desc.contains("active") -> "Present Active"
                    desc.contains("non-past") && desc.contains("indicative") && desc.contains("passive") -> "Present Passive"
                    desc.contains("subjunctive") && desc.contains("active") -> "Subjunctive Active"
                    desc.contains("subjunctive") && desc.contains("passive") -> "Subjunctive Passive"
                    desc.contains("jussive") && desc.contains("active") -> "Jussive Active"
                    desc.contains("jussive") && desc.contains("passive") -> "Jussive Passive"
                    else -> "Other"
                }
            }
            else -> "Other"
        }

        groups.getOrPut(key) { mutableListOf() }.add(form)
    }

    val orderedKeys = listOf(
        "Past Active", "Past Passive",
        "Present Active", "Present Passive",
        "Subjunctive Active", "Subjunctive Passive",
        "Jussive Active", "Jussive Passive",
        "Other"
    )
    val sortedGroups = LinkedHashMap<String, List<ConjugationForm>>()
    for (key in orderedKeys) {
        groups[key]?.let { sortedGroups[key] = it }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Conjugation: $lemma", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = JshoBrown, modifier = Modifier.padding(bottom = 12.dp))

        sortedGroups.forEach { (title, groupForms) ->
            ConjugationGroupCard(ConjugationGroup(title, groupForms))
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun ConjugationGroupCard(group: ConjugationGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JshoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(group.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = JshoBrown, modifier = Modifier.padding(bottom = 8.dp))

            Row(modifier = Modifier.fillMaxWidth().background(JshoBrown.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(8.dp)) {
                Text("Person", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
                Text("Form", modifier = Modifier.weight(2f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
                Text("Roman", modifier = Modifier.weight(1.5f), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JshoBrown, textAlign = TextAlign.Center)
            }

            group.forms.forEach { form ->
                val person = formatPerson(form.person, form.gender, form.number)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(person, modifier = Modifier.weight(1f), fontSize = 12.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                    Text(form.form, modifier = Modifier.weight(2f), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = JshoBlue, textAlign = TextAlign.Center)
                    Text(form.roman, modifier = Modifier.weight(1.5f), fontSize = 11.sp, color = JshoGray, textAlign = TextAlign.Center)
                }
                Divider(color = JshoDivider.copy(alpha = 0.3f))
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