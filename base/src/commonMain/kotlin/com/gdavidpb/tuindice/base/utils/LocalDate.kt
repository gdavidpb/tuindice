@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.utils

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * The calendar day of this instant (milliseconds since the epoch) in [timeZone], the device's by
 * default: the same instant is one day in one zone and the next in another, and a date the student
 * chose is stored as the start of that day in the zone they were in.
 */
fun Long.toLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
	return Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(timeZone)
		.date
}
