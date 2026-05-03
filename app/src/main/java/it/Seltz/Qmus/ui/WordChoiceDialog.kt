package it.Seltz.Qmus.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.theme.*

@Composable
fun WordChoiceDialog(
    options: List<Map<String, String>>,
    onDismiss: () -> Unit,
    onSelect: (Map<String, String>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Choose meaning",
                fontWeight = FontWeight.Bold,
                color = JshoBrown
            )
        },
        text = {
            LazyColumn {
                items(options) { option ->
                    val word = option["word"] ?: ""
                    val romanized = option["romanized"] ?: ""
                    val pos = option["pos"] ?: ""
                    val verbPattern = option["verb_pattern"] ?: ""
                    val defs = option["defs"]?.split(" ||| ")?.filter { it.isNotBlank() } ?: emptyList()
                    val firstDef = defs.firstOrNull() ?: ""

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(option) },
                        colors = CardDefaults.cardColors(containerColor = JshoWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(word, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = JshoBrown)
                                Text("→", fontSize = 20.sp, color = JshoGray)
                            }
                            
                            if (romanized.isNotBlank()) {
                                Text(romanized, fontSize = 14.sp, color = JshoGray)
                            }
                            
                            if (firstDef.isNotBlank()) {
                                Text(firstDef, fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 4.dp))
                            }
                            
                            Row(modifier = Modifier.padding(top = 4.dp)) {
                                if (pos.isNotBlank()) BadgeChip(pos, JshoBrown)
                                if (verbPattern.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    BadgeChip(verbPattern.take(12), JshoBlue)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = JshoGray)
            }
        }
    )
}