package it.Seltz.Qmus.data.conjugation.forms

import it.Seltz.Qmus.data.conjugation.ConjugationForm

/**
 * Generates all present tense forms (indicative, subjunctive, jussive)
 * for a given set of prefixes and stems.
 * 
 * @param forms The mutable list to add ConjugationForm objects to
 * @param presPrefixes List of present tense prefixes with person/gender/number info
 * @param activeStem The stem for active voice (without prefix)
 * @param passiveStem The stem for passive voice (without prefix), can be null if no passive
 * @param rootStr The root string for the lemma field
 */
fun generatePresentForms(
    forms: MutableList<ConjugationForm>,
    presPrefixes: List<PrefixInfo>,
    activeStem: String,
    passiveStem: String?,
    rootStr: String
) {
    for (p in presPrefixes) {
        val hasVowel = p.suffix == "َانِ" || p.suffix == "ُونَ" || p.suffix == "ِينَ" || p.suffix == "نَ"
        
        // Indicative suffix
        val indSuffix = if (hasVowel) p.suffix else "ُ" + p.suffix
        
        // Subjunctive suffix
        val subjSuffix = when {
            p.suffix == "ُونَ" -> "ُوا"
            p.suffix == "ِينَ" -> "ِي"
            p.suffix == "َانِ" -> "َا"
            p.suffix == "نَ" -> "نَ"
            else -> "َ"
        }
        
        // Jussive suffix
        val jussSuffix = when {
            p.suffix == "ُونَ" -> "ُوا"
            p.suffix == "ِينَ" -> "ِي"
            p.suffix == "َانِ" -> "َا"
            p.suffix == "نَ" -> "ْنَ"
            else -> "ْ"
        }

        // Active forms
        forms.add(ConjugationForm(
            form = p.arabic + activeStem + indSuffix,
            description = "non-past indicative active", lemma = rootStr,
            person = p.person, gender = p.gender, number = p.number,
            tense = "non-past", mood = "indicative", voice = "active"
        ))
        forms.add(ConjugationForm(
            form = p.arabic + activeStem + subjSuffix,
            description = "non-past subjunctive active", lemma = rootStr,
            person = p.person, gender = p.gender, number = p.number,
            tense = "non-past", mood = "subjunctive", voice = "active"
        ))
        forms.add(ConjugationForm(
            form = p.arabic + activeStem + jussSuffix,
            description = "non-past jussive active", lemma = rootStr,
            person = p.person, gender = p.gender, number = p.number,
            tense = "non-past", mood = "jussive", voice = "active"
        ))

        // Passive forms (if applicable)
        if (passiveStem != null) {
            forms.add(ConjugationForm(
                form = p.arabic + passiveStem + indSuffix,
                description = "non-past indicative passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number,
                tense = "non-past", mood = "indicative", voice = "passive"
            ))
            forms.add(ConjugationForm(
                form = p.arabic + passiveStem + subjSuffix,
                description = "non-past subjunctive passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number,
                tense = "non-past", mood = "subjunctive", voice = "passive"
            ))
            forms.add(ConjugationForm(
                form = p.arabic + passiveStem + jussSuffix,
                description = "non-past jussive passive", lemma = rootStr,
                person = p.person, gender = p.gender, number = p.number,
                tense = "non-past", mood = "jussive", voice = "passive"
            ))
        }
    }
}

/**
 * Generates all past tense forms for a given set of suffixes and stems.
 */
fun generatePastForms(
    forms: MutableList<ConjugationForm>,
    pastSuffixes: List<SuffixInfo>,
    activeStem: String,
    passiveStem: String?,
    rootStr: String
) {
    for (s in pastSuffixes) {
        forms.add(ConjugationForm(
            form = activeStem + s.arabic,
            description = "past active", lemma = rootStr,
            person = s.person, gender = s.gender, number = s.number,
            tense = "past", mood = "", voice = "active"
        ))
        if (passiveStem != null) {
            forms.add(ConjugationForm(
                form = passiveStem + s.arabic,
                description = "past passive", lemma = rootStr,
                person = s.person, gender = s.gender, number = s.number,
                tense = "past", mood = "", voice = "passive"
            ))
        }
    }
}