package com.gdavidpb.tuindice.base.presentation.mapper

import kotlinx.datetime.LocalDate

fun LocalDate.formatLocalizedMonthYear(): String {
	return "${localizedFullMonthNames()[month.ordinal]} $year"
}

fun localizedShortWeekdayNames(): List<String> {
	return platformShortWeekdayNames().map(::normalizeLocalizedDateToken)
}

fun localizedFullWeekdayNames(): List<String> {
	return platformFullWeekdayNames().map(::normalizeLocalizedDateToken)
}

fun localizedFullMonthNames(): List<String> {
	return platformFullMonthNames().map(::normalizeLocalizedDateToken)
}

fun normalizeLocalizedDateToken(value: String): String {
	return value
		.trim()
		.trimEnd('.')
		.lowercase()
}

expect fun platformFullMonthNames(): List<String>

expect fun platformFullWeekdayNames(): List<String>

expect fun platformShortWeekdayNames(): List<String>
