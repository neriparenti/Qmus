package it.Seltz.Qmus.data

import android.util.Log

object HarakatGenerator {

    private val arabicToRoman = mapOf(
        'ب' to "b", 'ت' to "t", 'ث' to "th", 'ج' to "j", 'ح' to "ḥ",
        'خ' to "kh", 'د' to "d", 'ذ' to "dh", 'ر' to "r", 'ز' to "z",
        'س' to "s", 'ش' to "sh", 'ص' to "ṣ", 'ض' to "ḍ", 'ط' to "ṭ",
        'ظ' to "ẓ", 'ع' to "ʿ", 'غ' to "gh", 'ف' to "f", 'ق' to "q",
        'ك' to "k", 'ل' to "l", 'م' to "m", 'ن' to "n", 'ه' to "h",
        'و' to "w", 'ي' to "y"
    )

    // Accept both ʿ (U+02BF) and ʕ (U+0295) for ع
    private val romanVariants = mapOf(
        "ʿ" to "ʕ",
        "ʕ" to "ʿ"
    )

    fun addHarakat(arabicWord: String, romanized: String): String {
        if (romanized.isBlank()) return arabicWord

        val cleanWord = arabicWord.replace(Regex("[\\u064B-\\u065F\\u0640\\u0670]"), "")
        val rom = romanized.lowercase().replace("-", "").replace(" ", "")

        val result = StringBuilder()
        var romIndex = 0

        for (i in cleanWord.indices) {
            val arabicChar = cleanWord[i]
            result.append(arabicChar)

            // Determina se و o ي sono vocali lunghe o consonanti
            val isLongVowel = when (arabicChar) {
                'و' -> {
                    if (romIndex < rom.length && rom[romIndex] == 'w') false
                    else true
                }
                'ي' -> {
                    if (romIndex < rom.length && rom[romIndex] == 'y') false
                    else true
                }
                'ا' -> true
                else -> false
            }

            if (isLongVowel && i > 0) continue

            // Ta marbuta finale: nessun harakat
            if (arabicChar == 'ة' && i == cleanWord.length - 1) continue

            // Controlla shadda
            val romCons = arabicToRoman[arabicChar]
            if (romCons != null && romIndex + romCons.length * 2 <= rom.length) {
                val nextTwo = rom.substring(romIndex, romIndex + romCons.length * 2)
                if (nextTwo == romCons + romCons) {
                    result.append("\u0651") // shadda
                    romIndex += romCons.length
                }
            }

            // Check if current position matches the consonant (or its variant)
            if (romCons != null && romIndex < rom.length) {
                val matchLen = romCons.length
                val candidate = if (romIndex + matchLen <= rom.length) rom.substring(romIndex, romIndex + matchLen) else ""
                val variant = romanVariants[romCons]
                val variantCandidate = if (variant != null && romIndex + variant.length <= rom.length)
                    rom.substring(romIndex, romIndex + variant.length) else ""

                if (candidate == romCons || (variant != null && variantCandidate == variant)) {
                    romIndex += if (candidate == romCons) matchLen else variant!!.length
                }
            }

            // Ora romIndex punta al carattere DOPO la consonante
            // Se è una vocale, aggiungila
            if (romIndex < rom.length && rom[romIndex] in "aiuāīū") {
                val v = rom[romIndex]
                when (v) {
                    'a' -> result.append("\u064E")
                    'i' -> result.append("\u0650")
                    'u' -> result.append("\u064F")
                    'ā' -> {}
                    'ī' -> {}
                    'ū' -> {}
                }
                romIndex++
            } else if (i + 1 < cleanWord.length && cleanWord[i + 1] !in "اوي") {
                // Se il prossimo carattere arabo è una consonante, aggiungi sukun
                result.append("\u0652")
            }
        }
        // Dopo il for loop, gestisci la nunazione finale (tanween)
        val remaining = rom.substring(romIndex)
        if (remaining == "an" && cleanWord.isNotEmpty()) {
            result.append("\u064B")  // tanween fatha
        } else if (remaining == "in" && cleanWord.isNotEmpty()) {
            result.append("\u064D")  // tanween kasra
        } else if (remaining == "un" && cleanWord.isNotEmpty()) {
            result.append("\u064C")  // tanween damma
        } else if (remaining == "n" && cleanWord.isNotEmpty()) {
            // La vocale prima della n è già stata aggiunta, aggiungi solo il tanween corrispondente
            // Controlla l'ultima vocale aggiunta
            val lastChar = result.lastOrNull()
            when (lastChar) {
                '\u064E' -> { // fatha
                    result.deleteCharAt(result.length - 1)
                    result.append("\u064B")  // tanween fatha
                }
                '\u0650' -> { // kasra
                    result.deleteCharAt(result.length - 1)
                    result.append("\u064D")  // tanween kasra
                }
                '\u064F' -> { // damma
                    result.deleteCharAt(result.length - 1)
                    result.append("\u064C")  // tanween damma
                }
            }
        }
        return result.toString()
    }
}