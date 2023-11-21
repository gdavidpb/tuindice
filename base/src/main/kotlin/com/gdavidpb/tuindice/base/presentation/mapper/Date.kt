package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.utils.DEFAULT_JAVA_TIME_ZONE
import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.base.utils.DEFAULT_TIME_ZONE
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

private val dateFormatCache = ConcurrentHashMap<String, DateFormat>()

fun Long.formatDate(format: String) = dateFormatCache.getOrPut(format) {
	SimpleDateFormat(format, DEFAULT_LOCALE).apply {
		timeZone = DEFAULT_JAVA_TIME_ZONE
	}
}.runCatching { format(Date(this@formatDate)) }.getOrNull()

fun String.parseDate(format: String) = dateFormatCache.getOrPut(format) {
	SimpleDateFormat(format, DEFAULT_LOCALE).apply {
		timeZone = DEFAULT_JAVA_TIME_ZONE
	}
}.runCatching { parse(this@parseDate) }.getOrNull()

fun Long.daysToNow() =
	Clock.System.now().toLocalDateTime(DEFAULT_TIME_ZONE).date
		.daysUntil(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(DEFAULT_TIME_ZONE)
				.date
		)

fun Long.weeksToNow() =
	Clock.System.now().toLocalDateTime(DEFAULT_TIME_ZONE).date
		.until(
			other = Instant
				.fromEpochMilliseconds(this)
				.toLocalDateTime(DEFAULT_TIME_ZONE)
				.date,
			unit = DateTimeUnit.WEEK
		)

fun Long.toLocalTimeZone() =
	Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.UTC)
		.toInstant(DEFAULT_TIME_ZONE)
		.toEpochMilliseconds()

fun Long.toUTCTimeZone() =
	Instant
		.fromEpochMilliseconds(this)
		.toLocalDateTime(DEFAULT_TIME_ZONE)
		.toInstant(TimeZone.UTC)
		.toEpochMilliseconds()
