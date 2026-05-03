package it.Seltz.Qmus.data

import com.google.gson.JsonParser

object RootExtractor {

    private val rootCategoryRegex = Regex("Arabic terms belonging to the root ([\\u0621-\\u064A] [\\u0621-\\u064A] [\\u0621-\\u064A])")

    /**
     * Estrae la radice da una riga JSONL di Wiktionary
     * @param jsonLine una riga del dump JSONL
     * @return la radice nel formato "ك-ت-ب" o null se non trovata
     */
    fun extractRoot(jsonLine: String): String? {
        try {
            val obj = JsonParser.parseString(jsonLine).asJsonObject
            
            // Search in categories
            val categories = obj.getAsJsonArray("categories")
            if (categories != null) {
                for (cat in categories) {
                    val name = cat.asJsonObject?.get("name")?.asString ?: continue
                    val match = rootCategoryRegex.find(name)
                    if (match != null) {
                        val root = match.groupValues[1]
                        // Convert "ك ت ب" to "ك-ت-ب"
                        return root.replace(" ", "-")
                    }
                }
            }

            // Search in senses -> categories (some voices have categories in senses)
            val senses = obj.getAsJsonArray("senses")
            if (senses != null) {
                for (sense in senses) {
                    val senseCategories = sense.asJsonObject?.getAsJsonArray("categories")
                    if (senseCategories != null) {
                        for (cat in senseCategories) {
                            val name = cat.asJsonObject?.get("name")?.asString ?: continue
                            val match = rootCategoryRegex.find(name)
                            if (match != null) {
                                val root = match.groupValues[1]
                                return root.replace(" ", "-")
                            }
                        }
                    }
                }
            }

            // Search in etymology_templates (some templates have roots)
            val etyTemplates = obj.getAsJsonArray("etymology_templates")
            if (etyTemplates != null) {
                for (template in etyTemplates) {
                    val name = template.asJsonObject?.get("name")?.asString ?: continue
                    if (name == "ar-root" || name == "ar-verb") {
                        val args = template.asJsonObject?.getAsJsonObject("args")
                        val root = args?.get("1")?.asString
                        if (root != null && root.length == 3) {
                            return root.chunked(1).joinToString("-")
                        }
                    }
                }
            }

        } catch (e: Exception) {
            // Ignore parsing errors
        }
        return null
    }

    /**
     * Processa un file JSONL e crea una mappa word -> root
     * @param lines le righe del file JSONL
     * @return mappa di parola araba -> radice
     */
    fun buildRootMap(lines: List<String>): Map<String, String> {
        val rootMap = mutableMapOf<String, String>()
        
        for (line in lines) {
            if (line.isBlank()) continue
            val root = extractRoot(line) ?: continue
            
            try {
                val obj = JsonParser.parseString(line).asJsonObject
                val word = obj.get("word")?.asString ?: continue
                
                if (word.isNotBlank() && root.isNotBlank()) {
                    rootMap[word] = root
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
        
        return rootMap
    }
}