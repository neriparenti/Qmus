package it.Seltz.Qmus.data.conjugation

data class ConjugationForm(
    val form: String = "",
    val roman: String = "",
    val description: String = "",
    val lemma: String = "",
    val person: String = "",
    val gender: String = "",
    val number: String = "",
    val tense: String = "",
    val mood: String = "",
    val voice: String = ""
)