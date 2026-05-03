package it.Seltz.Qmus.data.conjugation.forms

data class SuffixInfo(
    val arabic: String,
    val person: String,
    val gender: String,
    val number: String
)

data class PrefixInfo(
    val arabic: String,
    val suffix: String,
    val person: String,
    val gender: String,
    val number: String
)