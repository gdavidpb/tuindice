@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.utils.extension

import com.gdavidpb.tuindice.base.presentation.mapper.localizedFullMonthNames
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val SHORT_MONTH_NAMES = listOf(
	"Ene.",
	"Feb.",
	"Mar.",
	"Abr.",
	"May.",
	"Jun.",
	"Jul.",
	"Ago.",
	"Sep.",
	"Oct.",
	"Nov.",
	"Dic."
)

fun academicTermDisplayName(startAtMillis: Long, endAtMillis: Long): String {
	return formatAcademicTermDisplayName(
		startAtMillis = startAtMillis,
		endAtMillis = endAtMillis,
		monthNames = localizedFullMonthNames().map(String::capitalize)
	)
}

fun academicTermShortDisplayName(startAtMillis: Long, endAtMillis: Long): String {
	return formatAcademicTermDisplayName(
		startAtMillis = startAtMillis,
		endAtMillis = endAtMillis,
		monthNames = SHORT_MONTH_NAMES
	)
}

private fun formatAcademicTermDisplayName(
	startAtMillis: Long,
	endAtMillis: Long,
	monthNames: List<String>
): String {
	val startDate = startAtMillis.toUtcLocalDate()
	val endDate = endAtMillis.toUtcLocalDate()
	val startMonth = monthNames[startDate.month.ordinal]
	val endMonth = monthNames[endDate.month.ordinal]

	return when {
		startDate.year == endDate.year && startDate.month == endDate.month ->
			"$startMonth ${startDate.year}"

		startDate.year == endDate.year ->
			"$startMonth - $endMonth ${startDate.year}"

		else ->
			"$startMonth ${startDate.year} - $endMonth ${endDate.year}"
	}
}

private fun Long.toUtcLocalDate(): LocalDate {
	return Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.UTC)
		.date
}
