package it.Seltz.Qmus.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagFilterBar(
    allTags: List<String>,
    selectedTag: String?,
    onTagSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        FilterChip(
            selected = selectedTag == null,
            onClick = { onTagSelected(null) },
            label = { Text("Tutti", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = JshoBrown,
                selectedLabelColor = Color.White
            )
        )

        allTags.forEach { tag ->
            if (tag == "---") {
                Spacer(modifier = Modifier.width(4.dp))
            } else {
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = { onTagSelected(if (selectedTag == tag) null else tag) },
                    label = {
                        Text(
                            when (tag) {
                                "plural" -> "plural"
                                "masdar" -> "masdar"
                                "alt_form" -> "alt"
                                "noun" -> "noun"
                                "verb" -> "verb"
                                "adj" -> "adj"
                                "adv" -> "adv"
                                "pron" -> "pron"
                                "prep" -> "prep"
                                "conj" -> "conj"
                                "particle" -> "part"
                                else -> tag
                            },
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when {
                            tag == "plural" -> JshoOrange
                            tag == "masdar" -> JshoGreen
                            tag == "alt_form" -> JshoPurple
                            tag == "verb" -> JshoOrange
                            tag == "noun" -> JshoBrown
                            tag == "adj" -> JshoBlue
                            else -> JshoGray
                        },
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}