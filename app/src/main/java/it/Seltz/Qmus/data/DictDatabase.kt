package it.Seltz.Qmus.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import it.Seltz.Qmus.util.INVALID_ROOT_CHARS
import it.Seltz.Qmus.util.normalizeArabicForSearch
import java.io.FileOutputStream

class DictDatabase(context: Context) : SQLiteOpenHelper(context.applicationContext, "dictionary.db", null, 1) {
    companion object {
        @Volatile private var INSTANCE: DictDatabase? = null
        fun getInstance(context: Context): DictDatabase = INSTANCE ?: synchronized(this) {
            val dbFile = context.getDatabasePath("dictionary.db")
            if (!dbFile.exists()) {
                dbFile.parentFile?.mkdirs()
                context.assets.open("dictionary.db").use { input ->
                    FileOutputStream(dbFile).use { output -> input.copyTo(output) }
                }
            }
            DictDatabase(context).also { INSTANCE = it }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun getFkCol(db: SQLiteDatabase): String {
        val c = db.rawQuery("PRAGMA table_info(senses)", null)
        var col = "wordId"
        while (c.moveToNext()) if (c.getString(1).lowercase().contains("word")) { col = c.getString(1); break }
        c.close(); return col
    }

    fun getRandomNoun(gender: String): String {
        val db = readableDatabase
        val cursor = when (gender) {
            "masculine" -> db.rawQuery(
                "SELECT word FROM words WHERE pos = 'noun' AND word NOT LIKE '%ة' AND word NOT LIKE '%ات' AND word NOT IN (SELECT feminine FROM noun_forms WHERE feminine != '') ORDER BY RANDOM() LIMIT 1", null
            )
            "feminine" -> db.rawQuery(
                "SELECT feminine FROM noun_forms WHERE feminine != '' ORDER BY RANDOM() LIMIT 1", null
            )
            else -> db.rawQuery("SELECT word FROM words WHERE pos = 'noun' ORDER BY RANDOM() LIMIT 1", null)
        }
        val word = if (cursor.moveToFirst()) cursor.getString(0) else "شَيْء"
        cursor.close()
        return word
    }

    private fun query(sql: String, vararg args: String): List<Map<String, String>> {
        val results = mutableListOf<Map<String, String>>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                val m = mutableMapOf<String, String>()
                for (i in 0 until cursor.columnCount) m[cursor.getColumnName(i)] = cursor.getString(i) ?: ""
                results.add(m)
            }
        }
        return results
    }

    fun searchArabic(q: String) = query(
        "SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.word LIKE ? || '%' OR w.romanized LIKE ? || '%' GROUP BY w.id ORDER BY w.frequency_rank ASC, w.word ASC LIMIT 30", q, q
    )

    fun searchEnglish(q: String): List<Map<String, String>> {
        val cleanQ = q.trim().lowercase()
        return query(
            """SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs,
        CASE 
            WHEN LOWER(s.definition) = ? THEN 0
            WHEN LOWER(s.definition) LIKE ? || ',%' THEN 1
            WHEN LOWER(s.definition) LIKE ? || ' %' THEN 2
            WHEN LOWER(s.definition) LIKE '% ' || ? || ',%' THEN 3
            WHEN LOWER(s.definition) LIKE '% ' || ? || ' %' THEN 4
            WHEN LOWER(s.definition) LIKE '% ' || ? || '.%' THEN 5
            WHEN LOWER(s.definition) LIKE '% ' || ? THEN 6
            ELSE 7
        END as match_priority
        FROM words w 
        LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} 
        WHERE LOWER(s.definition) LIKE ? || '%'
           OR LOWER(s.definition) LIKE '% ' || ? || ' %' 
           OR LOWER(s.definition) LIKE '% ' || ?
           OR LOWER(s.definition) = ?
           OR LOWER(s.definition) LIKE '% ' || ? || ',%'
           OR LOWER(s.definition) LIKE '% ' || ? || '.%'
        GROUP BY w.id 
        ORDER BY match_priority ASC, w.frequency_rank ASC 
        LIMIT 30""",
            cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ
        )
    }

    fun searchEnglishStarting(q: String): List<Map<String, String>> {
        val cleanQ = q.trim().lowercase()
        return query(
            """SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs
        FROM words w 
        LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} 
        WHERE LOWER(s.definition) LIKE ? || ' %'
           OR LOWER(s.definition) LIKE ? || ',%'
           OR LOWER(s.definition) = ?
        GROUP BY w.id 
        ORDER BY w.frequency_rank ASC 
        LIMIT 30""",
            cleanQ, cleanQ, cleanQ
        )
    }

    fun searchById(id: String) = query(
        "SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.id = ? GROUP BY w.id LIMIT 1", id
    ).firstOrNull()

    fun searchByArabicText(text: String) = query(
        "SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.word = ? GROUP BY w.id LIMIT 1", normalizeArabicForSearch(text)
    ).firstOrNull()

    fun searchWildcard(q: String) = query(
        "SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.word LIKE ? GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 50", q.trim().replace("*", "%").replace("?", "_")
    )

    fun searchExact(q: String): List<Map<String, String>> {
        val cleanQ = q.trim().lowercase()
        return query(
            """SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs,
        CASE 
            WHEN LOWER(s.definition) = ? THEN 0
            WHEN LOWER(s.definition) LIKE ? || ',%' THEN 1
            WHEN LOWER(s.definition) LIKE ? || ' %' THEN 2
            WHEN LOWER(s.definition) LIKE '% ' || ? || ',%' THEN 3
            WHEN LOWER(s.definition) LIKE '% ' || ? || ' %' THEN 4
            WHEN LOWER(s.definition) LIKE '% ' || ? || '.%' THEN 5
            WHEN LOWER(s.definition) LIKE '% ' || ? THEN 6
            ELSE 7
        END as match_priority
        FROM words w 
        LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} 
        WHERE LOWER(s.definition) LIKE '% ' || ? || ' %' 
           OR LOWER(s.definition) LIKE ? || ' %'
           OR LOWER(s.definition) LIKE ? || ',%'
           OR LOWER(s.definition) LIKE '% ' || ?
           OR LOWER(s.definition) = ?
           OR LOWER(s.definition) LIKE '% ' || ? || ',%'
           OR LOWER(s.definition) LIKE '% ' || ? || '.%'
        GROUP BY w.id 
        ORDER BY match_priority ASC, w.frequency_rank ASC 
        LIMIT 30""",
            cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ, cleanQ
        )
    }

    fun searchAllByArabicText(text: String) = query(
        "SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.word = ? GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 10", normalizeArabicForSearch(text)
    )

    fun searchByRoot(rq: String): List<Map<String, String>> {
        val clean = rq.replace(Regex("[\\s\\-،آأةؤإئ\\u064B-\\u065F]"), "")
        if (clean.length < 3) return emptyList()
        return query("SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE REPLACE(REPLACE(REPLACE(w.root, ' ', ''), '-', ''), 'ـ', '') = ? GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 100", clean)
    }


    fun getVerbFormsWithMetadata(wordId: String): List<Map<String, String>> {
        return query(
            "SELECT form, tense, mood, voice, person, gender, number FROM forms WHERE word_id = ?",
            wordId
        )
    }

    fun getAllRoots(): List<String> {
        val roots = mutableListOf<String>()
        readableDatabase.rawQuery("SELECT DISTINCT root FROM words WHERE root IS NOT NULL AND root != '' AND root LIKE '%-%-%'", null).use { c ->
            while (c.moveToNext()) {
                val r = c.getString(0)
                if (r.isNotBlank()) {
                    val parts = r.split("-")
                    if (parts.size == 3 && parts.all { it.length == 1 && it !in INVALID_ROOT_CHARS }) roots.add(r)
                }
            }
        }
        return roots.distinct()
    }

    fun searchByTag(tag: String, query: String = ""): List<Map<String, String>> {
        val cleanQuery = query.trim().removeSurrounding("\"")
        val exactSearch = query.trim().startsWith("\"") && query.trim().endsWith("\"")
        val isArabic = cleanQuery.any { it in '\u0621'..'\u064A' }

        if (cleanQuery.isBlank()) {
            return searchByTagOnly(tag)
        }

        val searchResults = when {
            isArabic && exactSearch -> searchArabicExact(cleanQuery)
            isArabic && (query.contains("*") || query.contains("?")) -> searchArabicWildcard(query)
            isArabic -> searchArabic(cleanQuery)
            exactSearch -> searchExact(cleanQuery)
            query.contains("*") || query.contains("?") -> searchEnglishWildcard(query)
            else -> searchEnglish(query)
        }

        val finalResults = if (isArabic && searchResults.isEmpty() && cleanQuery.length > 1) {
            searchArabic(cleanQuery.dropLast(1))
        } else {
            searchResults
        }

        val taggedResults = finalResults.filter { word -> matchesTag(word, tag) }

        val enrichedResults = if (isArabic && taggedResults.isNotEmpty()) {
            taggedResults.map { word ->
                val searchedWord = cleanQuery
                val foundWord = word["word"] ?: ""
                if (searchedWord != foundWord) {
                    val mutable = word.toMutableMap()
                    val conjugatedForm = searchedWord
                    mutable["forms_info"] = when {
                        conjugatedForm.endsWith("َة") || conjugatedForm.endsWith("ة") -> "fem"
                        conjugatedForm.endsWith("َات") || conjugatedForm.endsWith("ات") -> "pl fem"
                        conjugatedForm.endsWith("ِينَ") || conjugatedForm.endsWith("ين") -> "pl obl"
                        conjugatedForm.endsWith("ُونَ") || conjugatedForm.endsWith("ون") -> "pl masc nom"
                        conjugatedForm.endsWith("َانِ") || conjugatedForm.endsWith("ان") -> "dual nom"
                        conjugatedForm.endsWith("َيْنِ") -> "dual obl"
                        else -> "form"
                    }
                    mutable["conjugated_form"] = conjugatedForm
                    mutable as Map<String, String>
                } else {
                    word
                }
            }
        } else {
            taggedResults
        }

        return enrichedResults
    }

    private fun searchArabicExact(q: String): List<Map<String, String>> {
        val exact = query(
            "SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.word = ? GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 30", q
        )
        return if (exact.isNotEmpty()) exact else searchArabic(q)
    }

    private fun searchArabicWildcard(q: String): List<Map<String, String>> {
        return query(
            "SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE w.word LIKE ? GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 50",
            q.trim().replace("*", "%").replace("?", "_")
        )
    }


    fun findLemmaByForm(conjugatedWord: String): Map<String, String>? {
        val norm = normalizeArabicForSearch(conjugatedWord)
        val fk = getFkCol(readableDatabase)

        val result = query(
            "SELECT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs, f.form as conjugated_form " +
                    "FROM words w INNER JOIN forms f ON w.id = f.word_id " +
                    "LEFT JOIN senses s ON w.id = s.$fk " +
                    "WHERE f.normalizedArabic = ? GROUP BY w.id LIMIT 1", norm
        ).firstOrNull()

        if (result != null) {
            val conjugatedForm = result["conjugated_form"] ?: ""
            val word = result["word"] ?: ""

            val formInfo = when {
                conjugatedForm.startsWith("ي") && conjugatedForm.length > word.length -> "3ms present"
                conjugatedForm.startsWith("ت") && conjugatedForm.length > word.length -> "3fs/2ms present"
                conjugatedForm.startsWith("أ") && conjugatedForm.length > word.length -> "1s present"
                conjugatedForm.startsWith("ن") && conjugatedForm.length > word.length -> "1p present"
                conjugatedForm.endsWith("تْ") || conjugatedForm.endsWith("تَ") -> "past"
                conjugatedForm.endsWith("وا") -> "3mp past"
                conjugatedForm != word -> "form"
                else -> ""
            }

            val mutable = result.toMutableMap()
            if (formInfo.isNotBlank()) {
                mutable["forms_info"] = formInfo
            }
            return mutable
        }
        return null
    }

    fun searchEnglishWildcard(q: String): List<Map<String, String>> {
        return query(
            "SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs FROM words w LEFT JOIN senses s ON w.id = s.${getFkCol(readableDatabase)} WHERE s.definition LIKE ? GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 50",
            q.trim().replace("*", "%").replace("?", "_")
        )
    }

    private fun matchesTag(word: Map<String, String>, tag: String): Boolean {
        return when (tag) {
            "noun" -> word["pos"] == "noun"
            "adjective" -> word["pos"] == "adj" || word["type"] == "adjective"
            "verb" -> word["pos"] == "verb"
            "feminine" -> word["gender"] == "feminine"
            "plural" -> {
                val defs = word["defs"] ?: ""
                word["number"] == "plural" || defs.startsWith("plural of", ignoreCase = true)
            }
            "masdar" -> {
                val defs = word["defs"] ?: ""
                (defs.startsWith("verbal noun of", ignoreCase = true) || (word["masdar"] ?: "").isNotBlank())
                        && word["type"] != "verb"
            }
            "alt_form" -> {
                val defs = word["defs"] ?: ""
                defs.startsWith("alternative form of", ignoreCase = true)
            }
            else -> word["pos"] == tag
        }
    }

    private fun searchByTagOnly(tag: String): List<Map<String, String>> {
        val db = readableDatabase
        val fk = getFkCol(db)

        val (condition, fromExtra) = when (tag) {
            "noun" -> "w.pos = 'noun'" to ""
            "adjective" -> "(w.pos = 'adj' OR w.type = 'adjective')" to ""
            "verb" -> "w.pos = 'verb'" to ""
            "feminine" -> "w.gender = 'feminine'" to ""
            "plural" -> "(w.number = 'plural' OR nf.plural IS NOT NULL OR s.definition LIKE 'plural of%')" to " LEFT JOIN noun_forms nf ON w.id = nf.word_id"
            "masdar" -> "(s.definition LIKE 'verbal noun of%' OR w.masdar != '') AND w.type != 'verb'" to ""
            "alt_form" -> "s.definition LIKE 'alternative form of%'" to ""
            else -> "w.pos = '$tag'" to ""
        }

        val sql = """
            SELECT DISTINCT w.*, GROUP_CONCAT(s.definition, ' ||| ') as defs 
            FROM words w LEFT JOIN senses s ON w.id = s.$fk
            $fromExtra
            WHERE $condition
            GROUP BY w.id ORDER BY w.frequency_rank ASC LIMIT 50
        """

        val results = mutableListOf<Map<String, String>>()
        db.rawQuery(sql, null).use { cursor ->
            while (cursor.moveToNext()) {
                val m = mutableMapOf<String, String>()
                for (i in 0 until cursor.columnCount) m[cursor.getColumnName(i)] = cursor.getString(i) ?: ""
                results.add(m)
            }
        }
        return results
    }

    fun getAllTags(): List<String> {
        return listOf("noun", "adjective", "verb", "feminine", "plural", "masdar", "alt_form")
    }

    fun getNounForms(wordId: String): Map<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM noun_forms WHERE word_id = ? LIMIT 1", arrayOf(wordId))
        val result = if (cursor.moveToFirst()) {
            val map = mutableMapOf<String, String>()
            for (i in 0 until cursor.columnCount) map[cursor.getColumnName(i)] = cursor.getString(i) ?: ""
            map
        } else null
        cursor.close()
        return result
    }
}