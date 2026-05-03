package it.Seltz.Qmus.data.conjugation.forms

import it.Seltz.Qmus.data.conjugation.ConjugationForm

object FormIHamzatedR3 {

    private fun mergeHamzaAlif(text: String): String {
        return text.replace("أَا", "آ")
            .replace("ءَا", "آ")
            .replace("ئَا", "آ")
    }

    fun generate(r1: Char, r2: Char, r3: Char, pastVowel: String = "a", presVowel: String = "a"): List<ConjugationForm> {
        val forms = mutableListOf<ConjugationForm>()
        val rootStr = "$r1-$r2-ء"

        fun vowelSign(v: String): String = when(v) { "a"->"َ"; "i"->"ِ"; "u"->"ُ"; else->"َ" }

        fun hamzaSeat(prevVowel: String): String = when(prevVowel) {
            "a" -> "أ"; "u" -> "ؤ"; "i" -> "ئ"; else -> "أ"
        }

        val pastSuffixes = listOf(
            SuffixInfo("َ", "3","m","s"), SuffixInfo("َتْ", "3","f","s"), SuffixInfo("َا", "3","","d"),
            SuffixInfo("َتَا", "3","f","d"), SuffixInfo("ُوا", "3","m","p"), SuffixInfo("ْنَ", "3","f","p"),
            SuffixInfo("ْتَ", "2","m","s"), SuffixInfo("ْتِ", "2","f","s"), SuffixInfo("ْتُمَا", "2","","d"),
            SuffixInfo("ْتُمْ", "2","m","p"), SuffixInfo("ْتُنَّ", "2","f","p"), SuffixInfo("ْتُ", "1","","s"),
            SuffixInfo("ْنَا", "1","","p")
        )

        val presPrefixes = listOf(
            PrefixInfo("يَ", "", "3","m","s"), PrefixInfo("تَ", "", "3","f","s"),
            PrefixInfo("يَ", "َانِ", "3","m","d"), PrefixInfo("تَ", "َانِ", "3","f","d"),
            PrefixInfo("يَ", "ُونَ", "3","m","p"), PrefixInfo("يَ", "نَ", "3","f","p"),
            PrefixInfo("تَ", "", "2","m","s"), PrefixInfo("تَ", "ِينَ", "2","f","s"),
            PrefixInfo("تَ", "َانِ", "2","","d"), PrefixInfo("تَ", "ُونَ", "2","m","p"),
            PrefixInfo("تَ", "نَ", "2","f","p"), PrefixInfo("أَ", "", "1","","s"),
            PrefixInfo("نَ", "", "1","","p")
        )

        // ==================== PAST ====================
        val pastSeat = hamzaSeat("a")
        val pastActiveStem = "${r1}${vowelSign(pastVowel)}${r2}َ${pastSeat}"
        val pastPassiveStem = "${r1}ُ${r2}ِئ"

        for (s in pastSuffixes) {
            val activeStemForSuffix = if (s.arabic.contains("وا")) {
                "${r1}${vowelSign(pastVowel)}${r2}َؤ"
            } else {
                pastActiveStem
            }
            val passiveStemForSuffix = if (s.arabic.contains("وا")) {
                "${r1}ُ${r2}ِئ"
            } else {
                pastPassiveStem
            }

            forms.add(ConjugationForm(form = mergeHamzaAlif(activeStemForSuffix + s.arabic),
                description = "past active", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number,
                tense = "past", mood = "", voice = "active"))
            forms.add(ConjugationForm(form = mergeHamzaAlif(passiveStemForSuffix + s.arabic),
                description = "past passive", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number,
                tense = "past", mood = "", voice = "passive"))
        }

        // ==================== PRESENT ====================
        val presSeat = hamzaSeat(presVowel)       // active: based on presVowel
        val passiveSeat = hamzaSeat("a")           // passive default: fatha → alif
        val activeBaseStem = "${r1}ْ${r2}${vowelSign(presVowel)}"
        val passiveBaseStem = "${r1}ْ${r2}َ"

        for (p in presPrefixes) {
            val hasVowel = p.suffix == "َانِ" || p.suffix == "ُونَ" || p.suffix == "ِينَ" || p.suffix == "نَ"
            val passPrefix = when(p.arabic) { "يَ"->"يُ"; "تَ"->"تُ"; "أَ"->"أُ"; "نَ"->"نُ"; else->p.arabic }

            if (hasVowel) {
                val indSuff = p.suffix
                val subjSuff = when {
                    p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"
                    p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "نَ"; else -> p.suffix
                }
                val jussSuff = when {
                    p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"
                    p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "ْنَ"; else -> ""
                }

                // Seat for passive: if the suffix contains و, use waw
                val passSeatInd = if (indSuff.contains("و")) "ؤ" else passiveSeat
                val passSeatSubj = if (subjSuff.contains("و")) "ؤ" else passiveSeat
                val passSeatJuss = if (jussSuff.contains("و")) "ؤ" else passiveSeat

                // ==================== ACTIVE ====================
                forms.add(ConjugationForm(form = mergeHamzaAlif(p.arabic + activeBaseStem + presSeat + indSuff),
                    description = "non-past indicative active", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "indicative", voice = "active"))
                forms.add(ConjugationForm(form = mergeHamzaAlif(p.arabic + activeBaseStem + presSeat + subjSuff),
                    description = "non-past subjunctive active", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "subjunctive", voice = "active"))
                forms.add(ConjugationForm(form = mergeHamzaAlif(p.arabic + activeBaseStem + presSeat + jussSuff),
                    description = "non-past jussive active", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "jussive", voice = "active"))

                // ==================== PASSIVE ====================
                forms.add(ConjugationForm(form = mergeHamzaAlif(passPrefix + passiveBaseStem + passSeatInd + indSuff),
                    description = "non-past indicative passive", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "indicative", voice = "passive"))
                forms.add(ConjugationForm(form = mergeHamzaAlif(passPrefix + passiveBaseStem + passSeatSubj + subjSuff),
                    description = "non-past subjunctive passive", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "subjunctive", voice = "passive"))
                forms.add(ConjugationForm(form = mergeHamzaAlif(passPrefix + passiveBaseStem + passSeatJuss + jussSuff),
                    description = "non-past jussive passive", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "jussive", voice = "passive"))
            } else {
                // ==================== ACTIVE (no suffix) ====================
                val indHamza = presSeat + "ُ"
                val subjHamza = presSeat + "َ"
                val jussHamza = presSeat + "ْ"

                forms.add(ConjugationForm(form = p.arabic + activeBaseStem + indHamza,
                    description = "non-past indicative active", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "indicative", voice = "active"))
                forms.add(ConjugationForm(form = p.arabic + activeBaseStem + subjHamza,
                    description = "non-past subjunctive active", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "subjunctive", voice = "active"))
                forms.add(ConjugationForm(form = p.arabic + activeBaseStem + jussHamza,
                    description = "non-past jussive active", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "jussive", voice = "active"))

                // ==================== PASSIVE (no suffix) ====================
                val passIndHamza = passiveSeat + "ُ"
                val passSubjHamza = passiveSeat + "َ"
                val passJussHamza = passiveSeat + "ْ"

                forms.add(ConjugationForm(form = passPrefix + passiveBaseStem + passIndHamza,
                    description = "non-past indicative passive", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "indicative", voice = "passive"))
                forms.add(ConjugationForm(form = passPrefix + passiveBaseStem + passSubjHamza,
                    description = "non-past subjunctive passive", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "subjunctive", voice = "passive"))
                forms.add(ConjugationForm(form = passPrefix + passiveBaseStem + passJussHamza,
                    description = "non-past jussive passive", lemma = rootStr,
                    person = p.person, gender = p.gender, number = p.number,
                    tense = "non-past", mood = "jussive", voice = "passive"))
            }
        }

        return forms
    }
}