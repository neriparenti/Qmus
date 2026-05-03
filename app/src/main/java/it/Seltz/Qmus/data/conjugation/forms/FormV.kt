package it.Seltz.Qmus.data.conjugation.forms

import it.Seltz.Qmus.data.conjugation.ConjugationForm

object FormV {

    fun generate(r1: Char, r2: Char, r3: Char): List<ConjugationForm> {
        val forms = mutableListOf<ConjugationForm>()
        val rootStr = "$r1-$r2-$r3"

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

        val pastStem = "تَ${r1}َ${r2}َّ${r3}"
        val presStem = "${r1}َ${r2}َّ${r3}"

        generatePastForms(forms, pastSuffixes, pastStem, null, rootStr)
        generatePresentForms(forms, presPrefixes, presStem, null, rootStr)

        return forms
    }
}