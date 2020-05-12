package com.gdavidpb.tuindice.utils.extensions

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
    val min = TimeUnit.SECONDS.toMinutes(this)
    val sec = TimeUnit.SECONDS.toSeconds(this - TimeUnit.MINUTES.toSeconds(min))

    return "%02d:%02d".format(min, sec)
}

fun Double.formatGrade(decimals: Int) = "%.${decimals}f".format(this)

fun Int.formatGrade() = "%d".format(this)

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

fun String.toShortName(): String {
    val array = split("\\s+".toRegex())

    return when {
        array.size == 1 -> arrayOf(0)
        array.size == 2 -> arrayOf(0, 1)
        array.size >= 3 -> arrayOf(0, 2)
        else -> emptyArray()
    }.run {
        joinToString(" ") { array[it] }
    }
}

fun String.trimAll() = replace("\\s+", "")