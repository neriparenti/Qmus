package it.Seltz.Qmus.data.conjugation.forms

import it.Seltz.Qmus.data.conjugation.ConjugationForm

object FormI {

    fun generate(r1: Char, r2: Char, r3: Char, pastVowel: String = "a", presVowel: String = "u"): List<ConjugationForm> {
        // Normalize hamza variants to bare hamza (ء)
        val nr1 = when(r1) { 'أ'->'ء'; 'إ'->'ء'; 'ؤ'->'ء'; 'ئ'->'ء'; else->r1 }
        val nr2 = when(r2) { 'أ'->'ء'; 'إ'->'ء'; 'ؤ'->'ء'; 'ئ'->'ء'; else->r2 }
        val nr3 = when(r3) { 'أ'->'ء'; 'إ'->'ء'; 'ؤ'->'ء'; 'ئ'->'ء'; else->r3 }

        // Check if first radical is hamza - delegate to specialized class
        if (nr1 == 'ء') {
            return FormIHamzated.generate(nr1, nr2, nr3, pastVowel, presVowel)
        }

        // Check if second radical is hamza
        if (nr2 == 'ء') {
            return FormIHamzatedR2.generate(nr1, nr2, nr3, pastVowel, presVowel)
        }

        // Check if third radical is hamza
        if (nr3 == 'ء') {
            return FormIHamzatedR3.generate(nr1, nr2, nr3, pastVowel, presVowel)
        }

        val forms = mutableListOf<ConjugationForm>()
        val rootStr = "$nr1-$nr2-$nr3"

        fun vowelSign(v: String): String = when(v) { "a"->"َ"; "i"->"ِ"; "u"->"ُ"; else->"َ" }

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

        // Past: faʿala / faʿila / faʿula
        val pastActiveStem = "${nr1}${vowelSign(pastVowel)}${nr2}َ${nr3}"
        val pastPassiveStem = "${nr1}ُ${nr2}ِ${nr3}"

        for (s in pastSuffixes) {
            forms.add(ConjugationForm(form = pastActiveStem + s.arabic, description = "past active", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number, tense = "past", mood = "", voice = "active"))
            forms.add(ConjugationForm(form = pastPassiveStem + s.arabic, description = "past passive", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number, tense = "past", mood = "", voice = "passive"))
        }

        // Present: yafʿulu / yafʿilu / yafʿalu
        for (p in presPrefixes) {
            val hasVowel = p.suffix == "َانِ" || p.suffix == "ُونَ" || p.suffix == "ِينَ" || p.suffix == "نَ"

            // Indicative: r2 has presVowel, r3 has damma (unless suffix has vowel)
            val indSuffix = if (hasVowel) p.suffix else "ُ" + p.suffix
            val activeIndicative = "${p.arabic}${nr1}ْ${nr2}${vowelSign(presVowel)}${nr3}$indSuffix"
            val passiveIndicative = "${p.arabic}${nr1}ْ${nr2}َ${nr3}$indSuffix"

            // Subjunctive: r3 has fatha (unless suffix has vowel)
            val subjSuffix = when {
                p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"
                p.suffix == "َانِ" -> "َا"; hasVowel -> p.suffix; else -> "َ"
            }
            val activeSubjunctive = "${p.arabic}${nr1}ْ${nr2}${vowelSign(presVowel)}${nr3}$subjSuffix"
            val passiveSubjunctive = "${p.arabic}${nr1}ْ${nr2}َ${nr3}$subjSuffix"

            // Jussive: r3 has sukun (unless suffix has vowel)
            val jussSuffix = when {
                p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"
                p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "ْنَ"
                hasVowel -> p.suffix; else -> ""
            }
            val activeJussive = "${p.arabic}${nr1}ْ${nr2}${vowelSign(presVowel)}${nr3}$jussSuffix"
            val passiveJussive = "${p.arabic}${nr1}ْ${nr2}َ${nr3}$jussSuffix"

            forms.add(ConjugationForm(form = activeIndicative, description = "non-past indicative active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "indicative", voice = "active"))
            forms.add(ConjugationForm(form = activeSubjunctive, description = "non-past subjunctive active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "subjunctive", voice = "active"))
            forms.add(ConjugationForm(form = activeJussive, description = "non-past jussive active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "jussive", voice = "active"))

            forms.add(ConjugationForm(form = passiveIndicative, description = "non-past indicative passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "indicative", voice = "passive"))
            forms.add(ConjugationForm(form = passiveSubjunctive, description = "non-past subjunctive passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "subjunctive", voice = "passive"))
            forms.add(ConjugationForm(form = passiveJussive, description = "non-past jussive passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "jussive", voice = "passive"))
        }

        return forms
    }
}