package it.Seltz.Qmus.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.Seltz.Qmus.data.DictDatabase
import it.Seltz.Qmus.theme.*
import it.Seltz.Qmus.util.INVALID_ROOT_CHARS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun RootSelectionScreen(db: DictDatabase, onRootSelected: (String) -> Unit, onBack: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var r1 by remember { mutableStateOf("") }  // First letter (on the RIGHT in arabic) → brown
    var r2 by remember { mutableStateOf("") }  // Second letter (central) → blue
    var r3 by remember { mutableStateOf("") }  // Third letter (on the LEFT in arabic) → green
    var allRoots by remember { mutableStateOf<List<String>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { allRoots = db.getAllRoots() }
        Log.d("ROOT_DEBUG", "allRoots.size=${allRoots.size}, contiene ب-و-ب? ${allRoots.contains("ب-و-ب")}, contiene ر-م-ي? ${allRoots.contains("ر-م-ي")}")
        loaded = true
    }

    // Step 0: choose r1 (first letter, on the right)
    // Step 1: choose r2 (second letter, central)
    // Step 2: choose r3 (third letter, on the left)
    val avail = remember(step, r1, r2, allRoots) {
        when (step) {
            0 -> allRoots.map { it.split("-")[0] }.filter { it.length == 1 && it[0] in '\u0621'..'\u064A' && it !in INVALID_ROOT_CHARS }.distinct().sorted()
            1 -> allRoots.filter { it.split("-")[0] == r1 }.map { it.split("-")[1] }.filter { it.length == 1 && it[0] in '\u0621'..'\u064A' && it !in INVALID_ROOT_CHARS }.distinct().sorted()
            2 -> {
                val filtered = allRoots.filter { val p = it.split("-"); p.size >= 3 && p[0] == r1 && p[1] == r2 }
                Log.d("ROOT_DEBUG", "step=2, r1=$r1, r2=$r2, filtered.size=${filtered.size}, filtered=$filtered")
                val thirdLetters = filtered.map { it.split("-")[2] }.filter { it.length == 1 && it[0] in '\u0621'..'\u064A' && it !in INVALID_ROOT_CHARS }.distinct().sorted()
                Log.d("ROOT_DEBUG", "thirdLetters=${thirdLetters}")
                thirdLetters
            }
            else -> emptyList()
        }
    }

    val ok = r1.isNotEmpty() && r2.isNotEmpty() && r3.isNotEmpty()

    if (!loaded) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("⏳ Loading...", color = JshoGray) }
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(JshoBg)) {
        Row(modifier = Modifier.fillMaxWidth().background(JshoBrown).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("←", color = Color.White, fontSize = 20.sp) }
            Text("Roots", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White, modifier = Modifier.weight(1f))
            Text("${allRoots.size}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
        }

        // Visualization: r3 - r2 - r1 (green - blue - brown)
        Row(modifier = Modifier.fillMaxWidth().background(JshoWhite).padding(20.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            RootBox(r3.ifEmpty { null }, JshoGreen)
            Text("─", fontSize = 24.sp, color = JshoGray)
            RootBox(r2.ifEmpty { null }, JshoBlue)
            Text("─", fontSize = 24.sp, color = JshoGray)
            RootBox(r1.ifEmpty { null }, JshoBrown)
        }

        Text(
            when (step) { 0 -> "First letter (right):"; 1 -> "Second letter:"; 2 -> "Third letter (left):"; else -> "" },
            fontWeight = FontWeight.Medium, fontSize = 14.sp, color = JshoBrown,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (ok) {
            Button(onClick = { onRootSelected("$r1 $r2 $r3") }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = JshoGreen)) {
                Text("🔍 Search $r1-$r2-$r3", fontWeight = FontWeight.Bold)
            }
        }
        Divider(color = JshoDivider)

        if (avail.isEmpty() && step > 0 && step < 3) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("😔", fontSize = 48.sp)
                    Text("No letter", color = JshoGray)
                    TextButton(onClick = {
                        when (step) {
                            2 -> { r2 = ""; step = 1 }
                            1 -> { r1 = ""; step = 0 }
                        }
                    }) { Text("↩ Back") }
                }
            }
        } else if (step < 3) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val rows = avail.chunked(6)
                items(rows.size) { ri ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        rows[ri].forEach { letter ->
                            LetterBtn(letter, isSelected = when { step == 0 && letter == r1 -> true; step == 1 && letter == r2 -> true; step == 2 && letter == r3 -> true; else -> false }) {
                                when (step) { 0 -> { r1 = letter; step = 1 }; 1 -> { r2 = letter; step = 2 }; 2 -> { r3 = letter; step = 3 } }
                            }
                        }
                        repeat(6 - rows[ri].size) { Spacer(modifier = Modifier.size(52.dp)) }
                    }
                }
                if (step > 0) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(52.dp).background(JshoGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).clickable {
                                when (step) {
                                    3 -> { r3 = ""; step = 2 }
                                    2 -> { r2 = ""; step = 1 }
                                    1 -> { r1 = ""; step = 0 }
                                }
                            }, contentAlignment = Alignment.Center) { Text("⌫", fontSize = 22.sp, color = JshoGray) }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        } else if (step < 3) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // ... grid of the letters ...
            }
        }else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val rows = avail.chunked(6)
                items(rows.size) { ri ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        rows[ri].forEach { letter ->
                            LetterBtn(letter, isSelected = when { step == 0 && letter == r1 -> true; step == 1 && letter == r2 -> true; step == 2 && letter == r3 -> true; else -> false }) {
                                when (step) {
                                    0 -> { r1 = letter; step = 1 }
                                    1 -> { r2 = letter; step = 2 }
                                    2 -> { r3 = letter; step = 3 }  // ← add step = 3
                                }
                            }
                        }
                        repeat(6 - rows[ri].size) { Spacer(modifier = Modifier.size(52.dp)) }
                    }
                }
                if (step > 0) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.Center) {
                            Box(modifier = Modifier.size(52.dp).background(JshoGray.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).clickable {
                                when (step) {
                                    3 -> { r3 = ""; step = 2 }
                                    2 -> { r2 = ""; step = 1 }
                                    1 -> { r1 = ""; step = 0 }
                                }
                            }, contentAlignment = Alignment.Center) { Text("⌫", fontSize = 22.sp, color = JshoGray) }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().background(JshoWhite).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = { step = 0; r1 = ""; r2 = ""; r3 = "" }) { Text("↺") }
            if (step > 0) { TextButton(onClick = { if (step == 1) { r1 = ""; step = 0 } else { r2 = ""; step = 1 } }) { Text("↩") } }
        }
    }
}