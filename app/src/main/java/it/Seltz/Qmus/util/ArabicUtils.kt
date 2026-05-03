package it.Seltz.Qmus.util

val arabicWordRegex = Regex("[\\u0621-\\u064A\\u0671-\\u06D3\\u064B-\\u065F\\u0670\\u0610-\\u061A\\u0640]+")
val INVALID_ROOT_CHARS = setOf("آ", "ة", "أ", "ؤ", "إ", "ئ", "ا", "ى")

fun normalizeArabicForSearch(text: String): String {
    return Regex("[\\u064B-\\u065F\\u0670\\u0610-\\u061A\\u0640\\u0656-\\u065F]").replace(text, "")
}

fun Char.isArabic(): Boolean = this in '\u0621'..'\u064A'