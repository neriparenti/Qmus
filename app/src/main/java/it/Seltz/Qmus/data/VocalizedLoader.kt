package it.Seltz.Qmus.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object VocalizedLoader {
    private var vocalizedMap: Map<String, String>? = null
    var loaded = false
        private set

    fun load(context: Context) {
        if (loaded) return
        try {
            val json = context.assets.open("vocalized.json").bufferedReader().readText()
            val type = object : TypeToken<Map<String, String>>() {}.type
            vocalizedMap = Gson().fromJson(json, type)
            loaded = true
        } catch (e: Exception) {
            vocalizedMap = emptyMap()
            loaded = true
        }
    }

    fun getVocalized(word: String): String? {
        return vocalizedMap?.get(word)
    }
}