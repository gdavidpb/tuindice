@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.utils.DEFAULT_LOCALE
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone as JavaTimeZone
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val dateFormatCache = ConcurrentHashMap<String, DateFormat>()

fun Long.formatDate(format: String) = dateFormatCache.getOrPut(format) {
	SimpleDateFormat(format, DEFAULT_LOCALE).apply {
		timeZone = JavaTimeZone.getTimeZone("UTC")
	}
}.runCatching { format(Date(this@formatDate)) }.getOrNull()

fun String.parseDate(format: String) = dateFormatCache.getOrPut(format) {
	SimpleDateFormat(format, DEFAULT_LOCALE).apply {
		timeZone = JavaTimeZone.getTimeZone("UTC")
	}
}.runCatching { parse(this@parseDate) }.getOrNull()

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
