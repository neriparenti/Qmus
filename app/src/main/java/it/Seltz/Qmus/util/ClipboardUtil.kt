package it.Seltz.Qmus.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    try {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("ArabicDictionary", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Errore copia", Toast.LENGTH_SHORT).show()
    }
}