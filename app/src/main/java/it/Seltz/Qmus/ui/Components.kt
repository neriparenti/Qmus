package it.Seltz.Qmus.ui

import androidx.compose.ui.zIndex
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.theme.*
import it.Seltz.Qmus.util.arabicWordRegex

@Composable
fun WordCard(word: Map<String, String>, onClick: () -> Unit) {
    val aw = word["word"] ?: ""
    val rom = word["romanized"] ?: ""
    val pos = word["pos"] ?: ""
    val root = word["root"] ?: ""
    val defs = word["defs"]?.split(" ||| ")?.filter { it.isNotBlank() } ?: emptyList()
    val fr = word["frequency_rank"]?.toIntOrNull() ?: 999999
    var exp by remember { mutableStateOf(false) }
    val fd = defs.firstOrNull() ?: ""
    val ip = fd.trimStart().startsWith("plural of", true)
    val im = fd.trimStart().startsWith("verbal noun of", true)
    val vd = if (exp) defs else if (defs.size <= 4) defs else defs.take(3)
    val hc = defs.size - vd.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 1.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = JshoWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(aw, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    if (rom.isNotBlank()) Text(rom, fontSize = 14.sp, color = JshoGray)
                }
                Text("›", fontSize = 24.sp, color = JshoGray, modifier = Modifier.align(Alignment.CenterVertically))
            }
            Row(modifier = Modifier.padding(top = 4.dp)) {
                when {
                    fr <= 100 -> BadgeChip("⭐", JshoGreen)
                    fr <= 500 -> BadgeChip("📖", JshoBlue)
                    fr <= 1000 -> BadgeChip("📚", JshoGray)
                }
                if (ip) BadgeChip("plural", JshoOrange)
                if (im) BadgeChip("masdar", JshoGreen)
                if (pos.isNotBlank()) BadgeChip(pos, JshoBrown)
                if (root.isNotBlank()) {
                    Spacer(Modifier.width(6.dp))
                    BadgeChip("√$root", JshoBlue)
                }
            }
            if (vd.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                vd.forEachIndexed { i, d ->
                    Text("${i + 1}. $d", fontSize = 14.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (hc > 0 || exp) TextButton(
                onClick = { exp = !exp },
                modifier = Modifier.align(Alignment.Start),
                contentPadding = PaddingValues(0.dp)
            ) { Text(if (exp) "▲ nascondi" else "▼ altre $hc", fontSize = 12.sp, color = JshoBlue) }
        }
        Divider(color = JshoDivider, thickness = 0.5.dp)
    }
}

@Composable
fun BadgeChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClickableDefinition(definition: String, onWordClick: (String) -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Text(
        text = definition,
        style = TextStyle(fontSize = 15.sp, color = Color.DarkGray),
        modifier = Modifier
            .zIndex(1f)  // ← >Bring to the foreground to receive touches
            .combinedClickable(
                onClick = {
                    val arabicWords = arabicWordRegex.findAll(definition).toList()
                    if (arabicWords.size == 1) {
                        onWordClick(arabicWords[0].value)
                    }
                },
                onLongClick = {
                    showDialog = true
                }
            )
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Copia", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            val arabicText = arabicWordRegex.findAll(definition)
                                .map { it.value }
                                .joinToString(" ")
                            copyToClipboard(context, arabicText, "Arabo copiato!")
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📝 Copia testo arabo", fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = {
                            val romanRegex = Regex("[a-zA-ZāīūʾʿḍḥṣṭẓĀĪŪ]+[a-zA-ZāīūʾʿḍḥṣṭẓĀĪŪ\\s]*")
                            val romanText = romanRegex.findAll(definition)
                                .map { it.value.trim() }
                                .filter { it.isNotBlank() }
                                .joinToString(" ")
                            if (romanText.isNotBlank()) {
                                copyToClipboard(context, romanText, "Romanizzazione copiata!")
                            } else {
                                Toast.makeText(context, "Nessuna romanizzazione trovata", Toast.LENGTH_SHORT).show()
                            }
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔤 Copia romanizzazione", fontSize = 16.sp)
                    }

                    TextButton(
                        onClick = {
                            val cleanDefinition = definition
                                .replace(arabicWordRegex, "")
                                .replace(Regex("[a-zA-ZāīūʾʿḍḥṣṭẓĀĪŪ]+"), "")
                                .trim()
                                .replace(Regex("\\s+"), " ")
                            if (cleanDefinition.isNotBlank()) {
                                copyToClipboard(context, cleanDefinition, "Definizione copiata!")
                            } else {
                                copyToClipboard(context, definition, "Testo copiato!")
                            }
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📄 Copia definizione", fontSize = 16.sp)
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    TextButton(
                        onClick = {
                            copyToClipboard(context, definition, "Tutto copiato!")
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("📋 Copia tutto", fontSize = 16.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Annulla", color = JshoGray)
                }
            }
        )
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("arabic_text", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

@Composable
fun RootBox(letter: String?, color: Color) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                if (letter != null) color.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter ?: "?",
            fontSize = if (letter != null) 28.sp else 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (letter != null) color else JshoGray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun LetterBtn(letter: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(
                if (isSelected) JshoBrown.copy(alpha = 0.2f) else JshoWhite,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter,
            fontSize = 24.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) JshoBrown else Color.DarkGray
        )
    }
}