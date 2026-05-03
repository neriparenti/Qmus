package it.Seltz.Qmus.data.conjugation.forms

import it.Seltz.Qmus.data.conjugation.ConjugationForm

object FormIHamzatedR2 {
    
    // Form I with hamza as SECOND radical (r2 = ء)
    // Examples: saʾala (س-ء-ل), qaraʾa (ق-ر-ء)
    
    fun generate(r1: Char, r2: Char, r3: Char, pastVowel: String = "a", presVowel: String = "a"): List<ConjugationForm> {
        val forms = mutableListOf<ConjugationForm>()
        val rootStr = "$r1-ء-$r3"
        
        fun vowelSign(v: String): String = when(v) { "a"->"َ"; "i"->"ِ"; "u"->"ُ"; else->"َ" }
        
        // Hamza seat rules for middle radical:
        // Determined by the vowel BEFORE the hamza
        // - fatha before: أ (on alif)
        // - damma before:ؤ (on waw)  
        // - kasra before: ئ (on ya)
        // - sukun before: أ (on alif) - default
        
        fun hamzaSeat(prevVowel: String): String = when(prevVowel) {
            "a", "َ", "" -> "أ"  // on alif (fatha or sukun)
            "u", "ُ" -> "ؤ"       // on waw
            "i", "ِ" -> "ئ"       // on ya
            else -> "أ"
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
        
        // Past active: r1 + pastVowel + hamza(seat based on pastVowel) + fatha + r3
        val hamzaActive = hamzaSeat(pastVowel)
        val pastActiveStem = "${r1}${vowelSign(pastVowel)}${hamzaActive}َ${r3}"
        
        // Past passive: r1 + damma + hamza(on ya because preceded by damma) + kasra + r3
        val pastPassiveStem = "${r1}ُئِ${r3}"  // suʾila: سُئِلَ
        
        for (s in pastSuffixes) {
            forms.add(ConjugationForm(form = pastActiveStem + s.arabic, description = "past active", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number, tense = "past", mood = "", voice = "active"))
            forms.add(ConjugationForm(form = pastPassiveStem + s.arabic, description = "past passive", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number, tense = "past", mood = "", voice = "passive"))
        }
        
        // Present: prefix + r1 + sukun + hamza(on alif, because preceded by sukun) + fatha + r3 + suffix
        for (p in presPrefixes) {
            val hasVowel = p.suffix == "َانِ" || p.suffix == "ُونَ" || p.suffix == "ِينَ" || p.suffix == "نَ"
            val indSuffix = if (hasVowel) p.suffix else "ُ" + p.suffix
            val subjSuffix = when { p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"; p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "نَ"; else -> "َ" }
            val jussSuffix = when { p.suffix == "ُونَ" -> "ُوا"; p.suffix == "ِينَ" -> "ِي"; p.suffix == "َانِ" -> "َا"; p.suffix == "نَ" -> "ْنَ"; else -> "" }
            
            // Active stem: r1 + sukun + hamza(on alif) + fatha (presVowel) + r3
            val activeStem = "${r1}ْأ${vowelSign(presVowel)}${r3}"
            val passiveStem = "${r1}ْأَ${r3}"  // yusʾalu: hamza on alif, fatha
            
            // Passive prefix: yu-, tu-, ʾu-, nu-
            val passivePrefix = when(p.arabic) { "يَ"->"يُ"; "تَ"->"تُ"; "أَ"->"أُ"; "نَ"->"نُ"; else->p.arabic }
            
            forms.add(ConjugationForm(form = p.arabic + activeStem + indSuffix, description = "non-past indicative active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "indicative", voice = "active"))
            forms.add(ConjugationForm(form = p.arabic + activeStem + subjSuffix, description = "non-past subjunctive active", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number, tense = "non-past", mood = "subjunctive", voice = "active"))
            forms.add(ConjugationForm(form = p.arabic + activeStem + jussSuffix, description = "non-past jussive active", lemma = rootStr,
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