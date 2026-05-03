package it.Seltz.Qmus.data

object NumberGrammar {

    val easternDigits = mapOf(
        '0' to '٠', '1' to '١', '2' to '٢', '3' to '٣', '4' to '٤',
        '5' to '٥', '6' to '٦', '7' to '٧', '8' to '٨', '9' to '٩'
    )

    fun parseNumber(input: String): Int? {
        input.toIntOrNull()?.let { return it }
        val cleaned = input.map { c ->
            easternDigits.entries.firstOrNull { it.value == c }?.key ?: c
        }.joinToString("")
        return cleaned.toIntOrNull()
    }

    data class NumberResult(
        val number: Int,
        val masculineForm: String,
        val feminineForm: String,
        val masculineExample: String,
        val feminineExample: String,
        val rule: String
    )

    fun getNumberForm(number: Int, masculineNoun: String, feminineNoun: String): NumberResult {
        return when (number) {
            1 -> NumberResult(1, "وَاحِد", "وَاحِدَة",
                "${addCase(masculineNoun, "nom")} وَاحِد", "${addCase(feminineNoun, "nom")} وَاحِدَة",
                "1 follows the gender of the noun")
            2 -> NumberResult(2, "اِثْنَان", "اِثْنَتَان",
                "${addCase(masculineNoun, "nom")} اِثْنَان", "${addCase(feminineNoun, "nom")} اِثْنَتَان",
                "2 follows the gender of the noun")
            in 3..9 -> {
                val femNum = getFeminineNumber3to9(number)
                val mascNum = getMasculineNumber3to9(number)
                NumberResult(number, femNum, mascNum,
                    "$femNum ${addCase(masculineNoun, "gen")} (genitive plural)",
                    "$mascNum ${addCase(feminineNoun, "gen")} (genitive plural)",
                    "3-9: opposite gender, genitive plural")
            }
            10 -> NumberResult(10, "عَشَرَة", "عَشْر",
                "عَشَرَة ${addCase(masculineNoun, "gen")} (genitive plural)",
                "عَشْر ${addCase(feminineNoun, "gen")} (genitive plural)",
                "10: opposite gender")
            11 -> NumberResult(11, "أَحَدَ عَشَرَ", "إِحْدَى عَشْرَةَ",
                "أَحَدَ عَشَرَ ${addCase(masculineNoun, "acc")} (accusative singular)",
                "إِحْدَى عَشْرَةَ ${addCase(feminineNoun, "acc")} (accusative singular)",
                "11: follows gender, accusative")
            12 -> NumberResult(12, "اِثْنَا عَشَرَ", "اِثْنَتَا عَشْرَةَ",
                "اِثْنَا عَشَرَ ${addCase(masculineNoun, "acc")} (accusative singular)",
                "اِثْنَتَا عَشْرَةَ ${addCase(feminineNoun, "acc")} (accusative singular)",
                "12: follows gender, accusative")
            in 13..19 -> {
                val unit = number % 10
                val mascUnit = getFeminineNumber3to9(unit)
                val femUnit = getMasculineNumber3to9(unit)
                NumberResult(number, "$mascUnitَ عَشَرَ", "$femUnitَ عَشْرَةَ",
                    "$mascUnitَ عَشَرَ ${addCase(masculineNoun, "acc")} (accusative singular)",
                    "$femUnitَ عَشْرَةَ ${addCase(feminineNoun, "acc")} (accusative singular)",
                    "13-19: opposite gender for unit, tens follow gender, accusative")
            }
            20 -> NumberResult(20, "عِشْرُونَ", "عِشْرُونَ",
                "عِشْرُونَ ${addCase(masculineNoun, "acc")} (accusative singular)",
                "عِشْرُونَ ${addCase(feminineNoun, "acc")} (accusative singular)",
                "20-90: invariable, accusative")
            30 -> NumberResult(30, "ثَلاثُونَ", "ثَلاثُونَ", "", "", "")
            40 -> NumberResult(40, "أَرْبَعُونَ", "أَرْبَعُونَ", "", "", "")
            50 -> NumberResult(50, "خَمْسُونَ", "خَمْسُونَ", "", "", "")
            60 -> NumberResult(60, "سِتُّونَ", "سِتُّونَ", "", "", "")
            70 -> NumberResult(70, "سَبْعونَ", "سَبْعونَ", "", "", "")
            80 -> NumberResult(80, "ثَمَانُونَ", "ثَمَانُونَ", "", "", "")
            90 -> NumberResult(90, "تِسْعُونَ", "تِسْعُونَ", "", "", "")
            100 -> NumberResult(100, "مِئَة", "مِئَة",
                "مِئَة ${addCase(masculineNoun, "gen")} (genitive singular)",
                "مِئَة ${addCase(feminineNoun, "gen")} (genitive singular)",
                "100: invariable, genitive")
            200 -> NumberResult(200, "مِئَتَان", "مِئَتَان",
                "مِئَتَان ${addCase(masculineNoun, "gen")}", "مِئَتَان ${addCase(feminineNoun, "gen")}", "")
            300 -> NumberResult(300, "ثَلاثُمِئَة", "ثَلاثُمِئَة",
                "ثَلاثُمِئَة ${addCase(masculineNoun, "gen")}", "ثَلاثُمِئَة ${addCase(feminineNoun, "gen")}", "")
            400 -> NumberResult(400, "أَرْبَعُمِئَة", "أَرْبَعُمِئَة",
                "أَرْبَعُمِئَة ${addCase(masculineNoun, "gen")}", "أَرْبَعُمِئَة ${addCase(feminineNoun, "gen")}", "")
            500 -> NumberResult(500, "خَمْسُمِئَة", "خَمْسُمِئَة",
                "خَمْسُمِئَة ${addCase(masculineNoun, "gen")}", "خَمْسُمِئَة ${addCase(feminineNoun, "gen")}", "")
            600 -> NumberResult(600, "سِتُّمِئَة", "سِتُّمِئَة",
                "سِتُّمِئَة ${addCase(masculineNoun, "gen")}", "سِتُّمِئَة ${addCase(feminineNoun, "gen")}", "")
            700 -> NumberResult(700, "سَبْعُمِئَة", "سَبْعُمِئَة",
                "سَبْعُمِئَة ${addCase(masculineNoun, "gen")}", "سَبْعُمِئَة ${addCase(feminineNoun, "gen")}", "")
            800 -> NumberResult(800, "ثَمَانِيمِئَة", "ثَمَانِيمِئَة",
                "ثَمَانِيمِئَة ${addCase(masculineNoun, "gen")}", "ثَمَانِيمِئَة ${addCase(feminineNoun, "gen")}", "")
            900 -> NumberResult(900, "تِسْعُمِئَة", "تِسْعُمِئَة",
                "تِسْعُمِئَة ${addCase(masculineNoun, "gen")}", "تِسْعُمِئَة ${addCase(feminineNoun, "gen")}", "")
            1000 -> NumberResult(1000, "أَلْف", "أَلْف",
                "أَلْف ${addCase(masculineNoun, "gen")} (genitive singular)",
                "أَلْف ${addCase(feminineNoun, "gen")} (genitive singular)",
                "1000: invariable, genitive")
            2000 -> NumberResult(2000, "أَلْفَان", "أَلْفَان",
                "أَلْفَان ${addCase(masculineNoun, "gen")}", "أَلْفَان ${addCase(feminineNoun, "gen")}", "")
            in 21..99 -> {
                val tens = (number / 10) * 10
                val unit = number % 10
                val tensName = getTensName(tens)
                if (unit == 0) NumberResult(number, tensName, tensName, "", "", "")
                else if (unit == 1) NumberResult(number, "وَاحِد وَ$tensName", "وَاحِدَة وَ$tensName",
                    "وَاحِد وَ$tensName ${addCase(masculineNoun, "acc")}", "وَاحِدَة وَ$tensName ${addCase(feminineNoun, "acc")}",
                    "21-99: unit 1 follows gender, accusative")
                else if (unit == 2) NumberResult(number, "اِثْنَان وَ$tensName", "اِثْنَتَان وَ$tensName",
                    "اِثْنَان وَ$tensName ${addCase(masculineNoun, "acc")}", "اِثْنَتَان وَ$tensName ${addCase(feminineNoun, "acc")}",
                    "21-99: unit 2 follows gender, accusative")
                else {
                    val mascUnit = getFeminineNumber3to9(unit)
                    val femUnit = getMasculineNumber3to9(unit)
                    NumberResult(number, "$mascUnit وَ$tensName", "$femUnit وَ$tensName",
                        "$mascUnit وَ$tensName ${addCase(masculineNoun, "acc")}",
                        "$femUnit وَ$tensName ${addCase(feminineNoun, "acc")}",
                        "21-99: units 3-9 opposite, accusative")
                }
            }
            in 101..999 -> {
                val hundreds = number / 100
                val remainder = number % 100
                val hundredsName = when (hundreds) {
                    2 -> "مِئَتَان"; 3 -> "ثَلاثُمِئَة"; 4 -> "أَرْبَعُمِئَة"
                    5 -> "خَمْسُمِئَة"; 6 -> "سِتُّمِئَة"; 7 -> "سَبْعُمِئَة"
                    8 -> "ثَمَانِيمِئَة"; 9 -> "تِسْعُمِئَة"; else -> "مِئَة"
                }
                if (remainder == 0) {
                    NumberResult(number, hundredsName, hundredsName,
                        "$hundredsName ${addCase(masculineNoun, "gen")}",
                        "$hundredsName ${addCase(feminineNoun, "gen")}",
                        "Exact hundreds")
                } else {
                    val remResult = getNumberForm(remainder, masculineNoun, feminineNoun)
                    val mascEx = "$hundredsName وَ${remResult.masculineForm} ${addCase(masculineNoun, getCaseForNumber(remainder))}"
                    val femEx = "$hundredsName وَ${remResult.feminineForm} ${addCase(feminineNoun, getCaseForNumber(remainder))}"
                    NumberResult(number,
                        "$hundredsName وَ${remResult.masculineForm}",
                        "$hundredsName وَ${remResult.feminineForm}",
                        mascEx, femEx,
                        "Combination hundreds + $remainder")
                }
            }
            in 1001..9999 -> {
                val thousands = number / 1000
                val remainder = number % 1000
                val thousandsName = when (thousands) {
                    1 -> "أَلْف"; 2 -> "أَلْفَان"
                    in 3..10 -> "${getFeminineNumber3to9(thousands)} آلَاف"
                    else -> "$thousands أَلْف"
                }
                if (remainder == 0) {
                    NumberResult(number, thousandsName, thousandsName,
                        "$thousandsName ${addCase(masculineNoun, "gen")}",
                        "$thousandsName ${addCase(feminineNoun, "gen")}",
                        "Exact thousands")
                } else {
                    val remResult = getNumberForm(remainder, masculineNoun, feminineNoun)
                    val mascEx = "$thousandsName وَ${remResult.masculineForm} ${addCase(masculineNoun, getCaseForNumber(remainder))}"
                    val femEx = "$thousandsName وَ${remResult.feminineForm} ${addCase(feminineNoun, getCaseForNumber(remainder))}"
                    NumberResult(number,
                        "$thousandsName وَ${remResult.masculineForm}",
                        "$thousandsName وَ${remResult.feminineForm}",
                        mascEx, femEx,
                        "Combination thousands + $remainder")
                }
            }
            else -> NumberResult(number, "", "", "", "", "Number out of range (1-9999)")
        }
    }

    private fun getMasculineNumber3to9(n: Int): String = when (n) { 3->"ثَلاثَة"; 4->"أَرْبَعَة"; 5->"خَمْسَة"; 6->"سِتَة"; 7->"سَبْعَة"; 8->"ثَمَانِيَة"; 9->"تِسْعَة"; else->"" }
    private fun getFeminineNumber3to9(n: Int): String = when (n) { 3->"ثَلاث"; 4->"أَرْبَع"; 5->"خَمْس"; 6->"سِتّ"; 7->"سَبْع"; 8->"ثَمَان"; 9->"تِسْع"; else->"" }
    private fun getTensName(n: Int): String = when (n) { 20->"عِشْرُونَ"; 30->"ثَلاثُونَ"; 40->"أَرْبَعُونَ"; 50->"خَمْسُونَ"; 60->"سِتُّونَ"; 70->"سَبْعونَ"; 80->"ثَمَانُونَ"; 90->"تِسْعُونَ"; else->"" }

    private fun addCase(noun: String, case: String): String {
        return when (case) {
            "acc" -> "$noun\u064B"   // ً
            "gen" -> "$noun\u064D"   // ٍ
            "nom" -> "$noun\u064C"   // ٌ
            else -> noun
        }
    }

    private fun getCaseForNumber(n: Int): String {
        return when {
            n == 1 || n == 2 -> "nom"
            n in 3..10 -> "gen"
            n in 11..99 -> "acc"
            n == 100 || n == 1000 -> "gen"
            n == 0 -> "gen"
            else -> "acc"
        }
    }

    fun formatResult(result: NumberResult): String {
        return buildString {
            append("${result.masculineForm} ||| ")
            if (result.masculineExample.isNotBlank()) append("👨 ${result.masculineExample} ||| ")
            if (result.feminineExample.isNotBlank()) append("👩 ${result.feminineExample} ||| ")
            append("📏 ${result.rule}")
        }
    }

    fun stripHarakat(text: String): String {
        return text.replace(Regex("[\\u064B-\\u065F\\u0670]"), "")
    }
}