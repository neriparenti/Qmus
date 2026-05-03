package it.Seltz.Qmus.data.conjugation

import it.Seltz.Qmus.data.conjugation.forms.*

object VerbConjugator {
    
    /**
     * Generate conjugation forms for a given root and form number.
     * @param root The triliteral root in format "k-t-b" or "ktr"
     * @param form The form number (I, II, III, ... X)
     * @return List of ConjugationForm objects
     */
    fun generateByForm(root: String, form: String): List<ConjugationForm> {
        val r = root.replace(Regex("[\\s\\-،]"), "")
        if (r.length < 3) return emptyList()
        val r1 = r[0]; val r2 = r[1]; val r3 = r[2]

        // Normalize hamza
        val nr1 = when(r1) { 'أ'->'ء'; 'إ'->'ء'; 'ؤ'->'ء'; 'ئ'->'ء'; else->r1 }

        val formNum = form.trim()

        // For Form I, check if it's hamzated
        if (formNum == "I" && nr1 == 'ء') {
            return FormIHamzated.generate(nr1, r2, r3)
        }

        return when (formNum) {
            "I"   -> FormI.generate(r1, r2, r3)
            "II"  -> FormII.generate(r1, r2, r3)
            "III" -> FormIII.generate(r1, r2, r3)
            "IV"  -> FormIV.generate(r1, r2, r3)
            "V"   -> FormV.generate(r1, r2, r3)
            "VI"  -> FormVI.generate(r1, r2, r3)
            "VII" -> FormVII.generate(r1, r2, r3)
            "VIII"-> FormVIII.generate(r1, r2, r3)
            "IX"  -> FormIX.generate(r1, r2, r3)
            "X"   -> FormX.generate(r1, r2, r3)
            else  -> FormI.generate(r1, r2, r3)
        }
    }
}