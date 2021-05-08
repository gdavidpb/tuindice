package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.extensions.parse
import com.gdavidpb.tuindice.utils.extensions.trimAll
import java.util.*

fun String.parseStartEndDate(): List<Date> {
    val normalizedText = "\\w+\\s*-\\s*\\w+\\s*\\d{4}".toRegex().find(this)?.value

    checkNotNull(normalizedText) { "toStartEndDate: $this" }

    val year = normalizedText.substringAfterLast(' ').trimAll().toIntOrNull() ?: 0
    val months = normalizedText.substringBeforeLast(' ').trimAll()

    val nextYear = Calendar.getInstance().get(Calendar.YEAR).inc()

    check(year <= nextYear) { "toStartEndDate: $this" }

    return months
            .split("\\s*-\\s*".toRegex())
            .mapNotNull { month -> "$month $year".parse("MMMM yyyy") }
}

private data class Replace(
        val target: String,
        val replacement: String,
        val isRegex: Boolean
)

private val replacements = listOf(
        /* TSU acronym */
        Replace("T.S.U.", "TSU", false),
        /* Some misspellings */
        Replace("(?<=\\w)\\.(?=\\w)", ". ", true),
        Replace("(?<=\\w):(?=\\w)", ": ", true),
        Replace("(?<=\\b)sue#o(?=\\b|\$)", "sueño", true),
        Replace("(?<=\\b)dise#o(?=\\b|\$)", "diseño", true),
        Replace("(?<=\\b)diseno(?=\\b|\$)", "diseño", true),
        /* Romans numbers */
        Replace("(?<=\\b)1(?=\\b|\$)", "I", true),
        Replace("(?<=\\b)2(?=\\b|\$)", "II", true),
        Replace("(?<=\\b)3(?=\\b|\$)", "III", true),
        Replace("(?<=\\b)4(?=\\b|\$)", "IV", true),
        Replace("(?<=\\b)5(?=\\b|\$)", "V", true),
        Replace("(?<=\\b)6(?=\\b|\$)", "VI", true),
        Replace("(?<=\\b)7(?=\\b|\$)", "VII", true),
        Replace("(?<=\\b)8(?=\\b|\$)", "VIII", true),
        Replace("(?<=\\b)9(?=\\b|\$)", "IX", true),
        Replace("(?<=\\b)10(?=\\b|\$)", "X", true)
)

fun String.parseSubjectName() = replacements
        .fold(this) { acc, (target, replacement, isRegex) ->
            if (isRegex)
                acc.replace(target.toRegex(RegexOption.IGNORE_CASE), replacement)
            else
                acc.replace(target, replacement)
        }.uppercase(DEFAULT_LOCALE)