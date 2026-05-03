package it.Seltz.Qmus.data.conjugation

import it.Seltz.Qmus.data.conjugation.ConjugationForm
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object ConjugationManager {
    private var conjugations: Map<String, List<ConjugationForm>> = emptyMap()
    var loaded = false
        private set

    fun load(context: Context) {
        if (loaded) return
        try {
            val json = context.assets.open("conjugations.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, List<ConjugationForm>>>() {}.type
            conjugations = Gson().fromJson(json, type)
            loaded = true
            Log.d("CONJUG", "Caricate ${conjugations.size} coniugazioni")
        } catch (e: Exception) {
            Log.e("CONJUG", "Errore: ${e.message}")
        }
    }

    private fun clean(lemma: String): String {
        return lemma.replace(Regex("[\\u064B-\\u065F\\u0610-\\u061A\\u0656-\\u065F]"), "")
    }

    fun hasForms(lemma: String): Boolean {
        if (conjugations.containsKey(lemma)) return true
        val c = clean(lemma)
        return conjugations.keys.any { clean(it) == c }
    }

    fun getForms(lemma: String): List<ConjugationForm> {
        conjugations[lemma]?.let { return it }
        val c = clean(lemma)
        val key = conjugations.keys.firstOrNull { clean(it) == c }
        return key?.let { conjugations[it] } ?: emptyList()
    }
}