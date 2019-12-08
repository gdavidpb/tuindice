package com.gdavidpb.tuindice.utils.extensions

import com.gdavidpb.tuindice.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.utils.DEFAULT_TIME_ZONE
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.floor

fun Date.formatLastUpdate(): String {
    val now = Date()
    val diff = now.time - time
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        time == 0L -> "Nunca"
        isToday() -> format("'Hoy,' hh:mm aa", DEFAULT_TIME_ZONE)
        isYesterday() -> format("'Ayer,' hh:mm aa", DEFAULT_TIME_ZONE)
        days < 7 -> format("EEEE',' hh:mm aa", DEFAULT_TIME_ZONE)
        else -> format("dd 'de' MMMM yyyy")
    }?.capitalize() ?: "-"
}

fun Long.toCountdown(): String {
    val min = TimeUnit.MILLISECONDS.toMinutes(this)
    val sec = TimeUnit.MILLISECONDS.toSeconds(this - TimeUnit.MINUTES.toMillis(min))

    return "%02d:%02d".format(min, sec)
}

fun Double.toGrade() = floor(this * 100000) / 100000

fun Double.formatGrade() = "%.4f".format(this)

private val dateFormatCache = hashMapOf<String, SimpleDateFormat>()

fun Date.format(format: String, zone: TimeZone = TimeZone.getTimeZone("GMT")) = dateFormatCache.getOrPut(format) {
    SimpleDateFormat(format, DEFAULT_LOCALE).apply {
        timeZone = zone
    }
}.runCatching { format(this@format) }.getOrNull()

fun String.parse(format: String, zone: TimeZone = TimeZone.getTimeZone("GMT")) = dateFormatCache.getOrPut(format) {
    SimpleDateFormat(format, DEFAULT_LOCALE).apply {
        timeZone = zone
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