package it.Seltz.Qmus.data

import it.Seltz.Qmus.util.normalizeArabicForSearch

fun detectByRomanization(romanized: String, root: String): String? {
    if (romanized.isBlank() || root.length < 3) return null
    val r = root.replace(Regex("[\\s\\-،]"), "")
    if (r.length < 3) return null

    val r1 = r[0]; val r2 = r[1]; val r3 = r[2]
    val rom = romanized.lowercase().replace("-", "").replace(" ", "")

    val arabicToRoman = mapOf(
        'ا' to listOf("ā", "a"), 'ب' to listOf("b"), 'ت' to listOf("t"),
        'ث' to listOf("th", "ṯ"), 'ج' to listOf("j"),
        'ح' to listOf("ḥ"), 'خ' to listOf("kh", "ḫ", "ḵ"),
        'د' to listOf("d"), 'ذ' to listOf("dh", "ḏ"), 'ر' to listOf("r"),
        'ز' to listOf("z"), 'س' to listOf("s"), 'ش' to listOf("sh", "š"),
        'ص' to listOf("ṣ"), 'ض' to listOf("ḍ"), 'ط' to listOf("ṭ"),
        'ظ' to listOf("ẓ"), 'ع' to listOf("ʿ", "ʕ", "'"), 'غ' to listOf("gh", "ġ", "ḡ"),
        'ف' to listOf("f"), 'ق' to listOf("q"), 'ك' to listOf("k"),
        'ل' to listOf("l"), 'م' to listOf("m"), 'ن' to listOf("n"),
        'ه' to listOf("h"), 'و' to listOf("w"), 'ي' to listOf("y"),
        'أ' to listOf("ʾ", "'", "a"), 'إ' to listOf("ʾ", "'", "i"),
        'ء' to listOf("ʾ", "'"), 'ؤ' to listOf("ʾ", "'"), 'ئ' to listOf("ʾ", "'"),
        'ى' to listOf("ā", "a"), 'ة' to listOf("h", "t")
    )

    fun String.romanVariants(): List<String> =
        if (length == 1 && this[0] in arabicToRoman) arabicToRoman[this[0]]!!
        else listOf(this)

    val r1Variants = r1.toString().romanVariants()
    val r2Variants = r2.toString().romanVariants()
    val r3Variants = r3.toString().romanVariants()

    // AGGIUNGI QUESTO LOG
    android.util.Log.d("WAZN_DEBUG", "r1=$r1, r1Variants=$r1Variants, r2=$r2, r2Variants=$r2Variants, r3=$r3, r3Variants=$r3Variants, rom=$rom")

    // Form II: second root doubled
    for (v in r2Variants) {
        val regex = Regex("${Regex.escape(v)}${Regex.escape(v)}")
        val matches = regex.containsMatchIn(rom)
        android.util.Log.d("WAZN_DEBUG", "Form II check: v=$v, regex=$regex, rom=$rom, matches=$matches")
        if (v.length >= 1 && matches) {
            return "FORM_II"
        }
    }
    // Form III: ā between r1 and r2, final -a
    for (v1 in r1Variants) {
        for (v2 in r2Variants) {
            if (Regex("${Regex.escape(v1)}ā${Regex.escape(v2)}.*a$").containsMatchIn(rom)) return "FORM_III"
        }
    }

    val r1v = r1Variants.first()
    val r2v = r2Variants.first()
    val r3v = r3Variants.first()

    // Active participle
    if (Regex("${Regex.escape(r1v)}ā${Regex.escape(r2v)}i${Regex.escape(r3v)}").containsMatchIn(rom)) return "ACTIVE_PARTICIPLE"
    if (Regex("${Regex.escape(r1v)}ā${Regex.escape(r2v)}u${Regex.escape(r3v)}").containsMatchIn(rom)) return "ACTIVE_PARTICIPLE"

    // Passive participle
    if (Regex("ma${Regex.escape(r1v)}${Regex.escape(r2v)}ū${Regex.escape(r3v)}").containsMatchIn(rom)) return "PASSIVE_PARTICIPLE"
    if (Regex("mu${Regex.escape(r1v)}${Regex.escape(r2v)}a${Regex.escape(r3v)}").containsMatchIn(rom)) return "PASSIVE_PARTICIPLE"

    // Form IV: ʾa- prefix
    for (v1 in r1Variants) {
        for (v2 in r2Variants) {
            if (Regex("^(ʾa|'a|a)${Regex.escape(v1)}${Regex.escape(v2)}a${Regex.escape(r3v)}").containsMatchIn(rom)) return "FORM_IV"
        }
    }

    // Form VII: in- prefix
    for (v1 in r1Variants) {
        for (v2 in r2Variants) {
            if (Regex("^in${Regex.escape(v1)}a${Regex.escape(v2)}a${Regex.escape(r3v)}").containsMatchIn(rom)) return "FORM_VII"
        }
    }

    // Form VIII: -ta- infix
    for (v1 in r1Variants) {
        for (v2 in r2Variants) {
            if (Regex("${Regex.escape(v1)}ta${Regex.escape(v2)}a${Regex.escape(r3v)}").containsMatchIn(rom)) return "FORM_VIII"
        }
    }

    // Form IX: doubling of the third root (iḥmarra)
    for (v3 in r3Variants) {
        if (Regex("^i${Regex.escape(r1v)}${Regex.escape(r2v)}a${Regex.escape(v3)}${Regex.escape(v3)}a").containsMatchIn(rom)) return "FORM_IX"
    }

    // Form X: ista- prefix
    for (v1 in r1Variants) {
        for (v2 in r2Variants) {
            if (Regex("^ista${Regex.escape(v1)}${Regex.escape(v2)}a${Regex.escape(r3v)}").containsMatchIn(rom)) return "FORM_X"
        }
    }

    // Form I
    for (v1 in r1Variants) {
        for (v2 in r2Variants) {
            for (v3 in r3Variants) {
                if (Regex("${Regex.escape(v1)}a${Regex.escape(v2)}a${Regex.escape(v3)}").containsMatchIn(rom)) return "FORM_I_A"
                if (Regex("${Regex.escape(v1)}a${Regex.escape(v2)}i${Regex.escape(v3)}").containsMatchIn(rom)) return "FORM_I_I"
                if (Regex("${Regex.escape(v1)}a${Regex.escape(v2)}u${Regex.escape(v3)}").containsMatchIn(rom)) return "FORM_I_U"
            }
        }
    }

    android.util.Log.d("WAZN_DEBUG", "No form detected, returning null")
    return null
}

fun detectWaznSimple(word: String): WaznInfo? {
    val bare = normalizeArabicForSearch(word)
    if (bare.length == 4 && bare[1] == 'ا' && bare[2] !in listOf('ا', 'و', 'ي')) {
        return WaznInfo("فَاعِل", "Participio attivo", "Colui che compie l'azione", 0.6f)
    }
    if (bare.length == 5 && bare[0] == 'م' && bare[2] == 'و') {
        return WaznInfo("مَفْعُول", "Participio passivo", "Ciò che subisce l'azione", 0.6f)
    }
    if (bare.length == 5 && bare[0] == 'م' && bare[3] == 'ا') {
        return WaznInfo("مِفْعَال", "Strumento", "Strumento per l'azione", 0.6f)
    }
    if (bare.length == 4 && bare[1] == 'ُ' && bare[3] == 'و') {
        return WaznInfo("فُعُول", "Nome astratto", "Concetto/plurale", 0.5f)
    }
    return null
}

fun detectWazn(word: String, pos: String, root: String, romanized: String = ""): WaznInfo? {
    if (word.isBlank() || root.isBlank()) return null
    if (romanized.isNotBlank() && root.length >= 3) {
        val r = root.replace(Regex("[\\s\\-،]"), "")
        val r1 = r[0]; val r2 = r[1]; val r3 = r[2]
        val activePattern = Regex("${r1}ā${r2}i${r3}", RegexOption.IGNORE_CASE)
        val form3Pattern = Regex("${r1}ā${r2}a${r3}", RegexOption.IGNORE_CASE)
        if (activePattern.containsMatchIn(romanized)) return WaznInfo("فَاعِل", "Participio attivo (I)", "Colui che compie l'azione", 0.95f)
        if (form3Pattern.containsMatchIn(romanized)) return null
    }
    if (pos.contains("verb", ignoreCase = true)) return null
    val bare = try { normalizeArabicForSearch(word).replace(Regex("^ال"), "").replace(Regex("^[و ف]"), "") } catch (e: Exception) { return null }
    val rl = root.replace(Regex("[\\s\\-،]"), ""); if (rl.length < 3) return null
    val r1 = rl[0]; val r2 = rl[1]; val r3 = rl[2]
    val patterns = listOf(
        WaznInfo("فَاعِل", "Participio attivo (I)", "Colui che compie l'azione", 0.7f) to Regex("^${r1}ا${r2}${r3}$"),
        WaznInfo("مَفْعُول", "Participio passivo (I)", "Ciò che subisce l'azione", 0.7f) to Regex("^م${r1}${r2}و${r3}$"),
        WaznInfo("مِفْعَال", "Strumento intensivo", "Strumento/potente", 0.7f) to Regex("^م${r1}${r2}ا${r3}$"),
        WaznInfo("فُعُول", "Nome astratto", "Concetto/plurale", 0.6f) to Regex("^${r1}ُ${r2}و${r3}$"),
        WaznInfo("فَعِيل", "Aggettivo", "Qualità", 0.6f) to Regex("^${r1}َ${r2}ي${r3}$")
    )
    for ((w, rx) in patterns) {
        try { if (rx.matches(bare)) return w } catch (e: Exception) {}
    }
    return null
}

fun detectVocalization(word: String, pos: String, senses: List<String>): VerbVocalization? {
    if (!pos.contains("verb", ignoreCase = true)) return null
    val bare = normalizeArabicForSearch(word)
    if (bare.length !in 3..5) return null
    val text = senses.joinToString(" ").lowercase()
    val fathaCount = word.count { it == '\u064E' }
    val kasraCount = word.count { it == '\u0650' }
    val dammaCount = word.count { it == '\u064F' }
    if (fathaCount >= 1 && kasraCount >= 1 && dammaCount == 0) return VerbVocalization("i", "a", "فَعِلَ - يَفْعَلُ", "Stato temporaneo", "شَرِبَ - يَشْرَبُ (bere)")
    if (fathaCount >= 2 && kasraCount == 0 && dammaCount == 0) return VerbVocalization("a", "a", "فَعَلَ - يَفْعَلُ", "Percezione/sensazione", "فَتَحَ - يَفْتَحُ (aprire)")
    if (dammaCount >= 1 && fathaCount == 0) return VerbVocalization("u", "u", "فَعُلَ - يَفْعُلُ", "Qualità permanente", "حَسُنَ - يَحْسُنُ (essere bello)")
    if (fathaCount >= 2 && dammaCount >= 1) return VerbVocalization("a", "u", "فَعَلَ - يَفْعُلُ", "Transitivo/attivo", "كَتَبَ - يَكْتُبُ (scrivere)")
    return when {
        text.contains("to be ") -> VerbVocalization("a", "i", "فَعَلَ - يَفْعِلُ", "Stato/risultato", "جَلَسَ - يَجْلِسُ (sedersi)")
        text.contains("drink") || text.contains("bere") -> VerbVocalization("i", "a", "فَعِلَ - يَفْعَلُ", "Stato temporaneo", "شَرِبَ - يَشْرَبُ (bere)")
        else -> VerbVocalization("a", "u", "فَعَلَ - يَفْعُلُ", "Transitivo/attivo", "كَتَبَ - يَكْتُبُ (scrivere)")
    }
}