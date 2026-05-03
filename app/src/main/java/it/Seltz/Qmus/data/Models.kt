package it.Seltz.Qmus.data

data class WaznInfo(val pattern: String, val name: String, val description: String, val confidence: Float)
data class VerbVocalization(val past: String, val present: String, val pattern: String, val description: String, val example: String)

enum class SearchMode { ARABIC, ENGLISH }

sealed class Screen {
    object MainSearch : Screen()
    object RootSelection : Screen()
    data class WordDetail(val word: Map<String, String>) : Screen()
}