@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.presentation.mapper

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val fullMonthNames = listOf(
	"enero",
	"febrero",
	"marzo",
	"abril",
	"mayo",
	"junio",
	"julio",
	"agosto",
	"septiembre",
	"octubre",
	"noviembre",
	"diciembre"
)

data class ParsedDate(
	val time: Long
)

private fun DayOfWeek.fullName() = when (this) {
	DayOfWeek.MONDAY -> "lunes"
	DayOfWeek.TUESDAY -> "martes"
	DayOfWeek.WEDNESDAY -> "miércoles"
	DayOfWeek.THURSDAY -> "jueves"
	DayOfWeek.FRIDAY -> "viernes"
	DayOfWeek.SATURDAY -> "sábado"
	DayOfWeek.SUNDAY -> "domingo"
}

private fun DayOfWeek.shortName() = when (this) {
	DayOfWeek.MONDAY -> "lun"
	DayOfWeek.TUESDAY -> "mar"
	DayOfWeek.WEDNESDAY -> "mié"
	DayOfWeek.THURSDAY -> "jue"
	DayOfWeek.FRIDAY -> "vie"
	DayOfWeek.SATURDAY -> "sáb"
	DayOfWeek.SUNDAY -> "dom"
}

fun Long.formatDate(format: String): String? {
	val dateTime = Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.UTC)

	val dayName = dateTime.dayOfWeek.fullName()
	val shortDayName = dateTime.dayOfWeek.shortName()
	val monthName = fullMonthNames[dateTime.month.ordinal]
	val dayOfMonth = dateTime.day.toString().padStart(2, '0')
	val monthNumber = (dateTime.month.ordinal + 1).toString().padStart(2, '0')
	val year = dateTime.year.toString()
	val shortYear = (dateTime.year % 100).toString().padStart(2, '0')
	val hour12 = ((dateTime.hour + 11) % 12 + 1).toString().padStart(2, '0')
	val minutes = dateTime.minute.toString().padStart(2, '0')
	val amPm = if (dateTime.hour < 12) "AM" else "PM"

	return when (format) {
		"'Hoy,' hh:mm aa" -> "Hoy, $hour12:$minutes $amPm"
		"'Ayer,' hh:mm aa" -> "Ayer, $hour12:$minutes $amPm"
		"EEEE',' hh:mm aa" -> "$dayName, $hour12:$minutes $amPm"
		"dd 'de' MMMM yyyy" -> "$dayOfMonth de $monthName $year"
		"EEEE 'pasado —' dd 'de' MMMM" -> "$dayName pasado — $dayOfMonth de $monthName"
		"EEEE '—' dd 'de' MMMM" -> "$dayName — $dayOfMonth de $monthName"
		"EEEE '—' dd/MM/yy" -> "$dayName — $dayOfMonth/$monthNumber/$shortYear"
		"EEE '—' dd/MM/yy" -> "$shortDayName — $dayOfMonth/$monthNumber/$shortYear"
		else -> null
	}
}

fun String.parseDate(format: String): ParsedDate? {
	if (format != "MMMM yyyy") return null

	val parts = trim().split(" ").filter { it.isNotBlank() }
	if (parts.size != 2) return null

	val monthText = parts[0].lowercase()
	val year = parts[1].toIntOrNull() ?: return null
	val month = fullMonthNames.indexOf(monthText) + 1
	if (month == 0) return null

	val localDate = runCatching { LocalDate(year, month, 1) }.getOrNull() ?: return null
	val epoch = localDate
		.atStartOfDayIn(TimeZone.UTC)
		.toEpochMilliseconds()

	return ParsedDate(epoch)
}

fun Long.daysToNow() =
	Clock.System.now().toLocalDateTime(TimeZone.UTC).date
		.daysUntil(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(TimeZone.UTC)
				.date
		)

fun Long.weeksToNow() =
	Clock.System.now().toLocalDateTime(TimeZone.UTC).date
		.until(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(TimeZone.UTC)
				.date,
			unit = DateTimeUnit.WEEK
		)

fun Long.toLocalTimeZone() =
	Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.UTC)
		.toInstant(TimeZone.currentSystemDefault())
		.toEpochMilliseconds()

fun Long.toUTCTimeZone() =
	Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.toInstant(TimeZone.UTC)
		.toEpochMilliseconds()
