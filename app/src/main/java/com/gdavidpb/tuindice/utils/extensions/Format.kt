package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import com.gdavidpb.tuindice.utils.mappers.capitalize
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

fun Date.formatLastUpdate(): String {
    val now = Date()
    val diff = now.time - time
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        time == 0L -> "Nunca"
        isToday() -> format("'Hoy,' hh:mm aa")
        isYesterday() -> format("'Ayer,' hh:mm aa")
        days < 7 -> format("EEEE',' hh:mm aa")
        else -> format("dd 'de' MMMM yyyy")
    }?.capitalize() ?: "-"
}

fun Double.formatGrade() = if (this % 1 == 0.0) toInt().formatGrade() else formatGrade(2)

fun Double.formatGrade(decimals: Int) = "%.${decimals}f".format(round(decimals))

fun Float.formatGrade(decimals: Int) = "%.${decimals}f".format(toDouble().round(decimals))

fun Int.formatGrade() = "$this"

fun Pair<Date, Date>.formatQuarterName(): String {
    val start = first.format("MMM")?.capitalize()
    val end = second.format("MMM")?.capitalize()
    val year = first.format("yyyy")

    return "$start - $end $year".replace("\\.".toRegex(), "")
}

private val dateFormatCache = hashMapOf<String, SimpleDateFormat>()

fun Date.format(format: String) = dateFormatCache.getOrPut(format) {
    SimpleDateFormat(format, DEFAULT_LOCALE).apply {
        timeZone = DEFAULT_TIME_ZONE
    }
}.runCatching { format(this@format) }.getOrNull()

fun String.parse(format: String) = dateFormatCache.getOrPut(format) {
    SimpleDateFormat(format, DEFAULT_LOCALE).apply {
        timeZone = DEFAULT_TIME_ZONE
    }
}.runCatching { parse(this@parse) }.getOrNull()

fun Account.toShortName(): String {
    val firstName = firstNames.substringBefore(' ')
    val lastName = lastNames.substringBefore(' ')

    return "$firstName $lastName"
}