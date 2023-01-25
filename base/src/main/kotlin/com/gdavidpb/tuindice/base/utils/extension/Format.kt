package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import java.text.SimpleDateFormat
import java.util.*

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

fun String.capitalize() =
	if (isNotEmpty() && this[0].isLowerCase())
		"${substring(0, 1).uppercase(DEFAULT_LOCALE)}${substring(1)}"
	else
		this

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