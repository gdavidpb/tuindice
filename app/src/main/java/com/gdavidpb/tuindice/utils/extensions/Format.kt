package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.DEFAULT_TIME_ZONE
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

fun Long.toCountdown(): String {
    val min = TimeUnit.MILLISECONDS.toMinutes(this)
    val sec = TimeUnit.MILLISECONDS.toSeconds(this - TimeUnit.MINUTES.toSeconds(min))

    return "%02d:%02d".format(min, sec)
}

fun Double.formatGrade(decimals: Int) = "%.${decimals}f".format(this)

fun Float.formatGrade(decimals: Int) = "%.${decimals}f".format(this)

fun Int.formatGrade() = "%d".format(this)

fun Date.formatQuarterName(date: Date): String {
    val start = format("MMM")?.capitalize()
    val end = date.format("MMM")?.capitalize()
    val year = format("yyyy")

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

fun String.trimAll() = replace("\\s+", "")