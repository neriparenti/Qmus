package it.Seltz.Qmus.data.conjugation.forms

import it.Seltz.Qmus.data.conjugation.ConjugationForm

object FormIHamzated {
    
    // Form I with hamza as first radical (r1 = ء)
    // Examples: ʾakala (ء-ك-ل), ʾamara (ء-م-ر)
    
    fun generate(r1: Char, r2: Char, r3: Char, pastVowel: String = "a", presVowel: String = "u"): List<ConjugationForm> {
        val forms = mutableListOf<ConjugationForm>()
        val rootStr = "ء-$r2-$r3"
        
        fun vowelSign(v: String): String = when(v) { "a"->"َ"; "i"->"ِ"; "u"->"ُ"; else->"َ" }
        
        // Hamza seat rules for first radical:
        // - fatha (a): أَ
        // - damma (u): أُ
        // - kasra (i): إِ
        // - sukun (ْ): أْ (or إْ before kasra)
        
        fun hamzaWith(vowel: String): String = when(vowel) {
            "a", "َ" -> "أَ"
            "u", "ُ" -> "أُ"
            "i", "ِ" -> "إِ"
            "ْ", "" -> "أْ"
            else -> "أَ"
        }
        
        // For present passive: hamza + waw when preceded by damma
        fun hamzaPassive(vowel: String): String = when(vowel) {
            "a", "َ" -> "أَ"
            "u", "ُ" -> "ؤُ"  // hamza on waw
            "i", "ِ" -> "ئِ"  // hamza on ya
            else -> "أْ"
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
        
        // Past active: ʾaCvCvCa
        // r1 = ء with past vowel (usually a)
        val pastActivePrefix = hamzaWith(pastVowel)
        val pastPassivePrefix = "أُ"  // ʾu
        
        // Present stem: r1 takes sukun, r2 takes presVowel, r3 takes mood vowel
        // For active: ya + ʾ + sukun + r2 + presVowel + r3
        // For passive: yu + ʾ + sukun + r2 + a + r3
        
        val pastActiveStem = "${pastActivePrefix}${r2}َ${r3}"
        val pastPassiveStem = "${pastPassivePrefix}${r2}ِ${r3}"
        
        generatePastForms(forms, pastSuffixes, pastActiveStem, pastPassiveStem, rootStr)
        
        // Present
        for (p in presPrefixes) {
            val hasVowel = p.suffix == "َانِ" || p.suffix == "ُونَ" || p.suffix == "ِينَ" || p.suffix == "نَ"
            val indSuffix = if (hasVowel) p.suffix else "ُ" + p.suffix
            val subjSuffix = when { p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"; p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "نَ"; else -> "َ" }
            val jussSuffix = when { p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"; p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "ْنَ"; else -> "" }
            
            // Active: prefix (ya-/ta-/ʾa-/na-) + hamza on alif + sukun + r2 + presVowel + r3 + suffix
            val activePrefix = p.arabic  // يَ, تَ, أَ, نَ
            val activeHamza = "أْ"  // hamza with sukun
            val activeStem = "${activeHamza}${r2}${vowelSign(presVowel)}${r3}"
            
            // Passive: prefix in damma + hamza on waw + sukun + r2 + fatha + r3 + suffix
            val passivePrefix = when(p.arabic) { "يَ"->"يُ"; "تَ"->"تُ"; "أَ"->"أُ"; "نَ"->"نُ"; else->p.arabic }
            val passiveHamza = "ؤْ"  // hamza on waw with sukun (after damma)
            val passiveStem = "${passiveHamza}${r2}َ${r3}"
            
            forms.add(ConjugationForm(form = activePrefix + activeStem + indSuffix, description = "non-past indicative active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "indicative", voice = "active"))
            forms.add(ConjugationForm(form = activePrefix + activeStem + subjSuffix, description = "non-past subjunctive active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "subjunctive", voice = "active"))
            forms.add(ConjugationForm(form = activePrefix + activeStem + jussSuffix, description = "non-past jussive active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "jussive", voice = "active"))
            
            forms.add(ConjugationForm(form = passivePrefix + passiveStem + indSuffix, description = "non-past indicative passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "indicative", voice = "passive"))
            forms.add(ConjugationForm(form = passivePrefix + passiveStem + subjSuffix, description = "non-past subjunctive passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "subjunctive", voice = "passive"))
            forms.add(ConjugationForm(form = passivePrefix + passiveStem + jussSuffix, description = "non-past jussive passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "jussive", voice = "passive"))
        }
        
        return forms
    }
}