package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.mapper.DateCellStyle
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.formatDateCell
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

// A moment the chat tells the student about, read in their time zone against the moment the card is read:
// "hoy a las 3:05 p. m." on that same day, "el viernes 23 de octubre, 6:53 a. m." on another day of this
// year, and the year after the day on another year. A sentence of the chat reads it.
internal fun Long.toChatDateText(nowMillis: Long): UiText =
	formatDate(
		styleAgainst(
			nowMillis = nowMillis,
			sameDay = DateTextStyle.TODAY_AT_CLOCK,
			thisYear = DateTextStyle.WEEKDAY_DAY_MONTH_AT_CLOCK,
			otherYear = DateTextStyle.WEEKDAY_DAY_MONTH_YEAR_AT_CLOCK
		)
	)

// The same moment as a cell of a card reads it, where a value stands on its own and no article leads the day: "hoy,
// 3:05 p. m." on that same day, "lunes 19 de octubre, 11:22 p. m." on another day of this year, and the year after the
// day on another year.
internal fun Long.toChatCellDateText(nowMillis: Long): UiText =
	formatDateCell(
		styleAgainst(
			nowMillis = nowMillis,
			sameDay = DateCellStyle.TODAY_CLOCK,
			thisYear = DateCellStyle.WEEKDAY_DAY_MONTH_CLOCK,
			otherYear = DateCellStyle.WEEKDAY_DAY_MONTH_YEAR_CLOCK
		)
	)

// The style of a moment against the moment it is read: the same day, another day of this year, or another year.
private fun <T> Long.styleAgainst(nowMillis: Long, sameDay: T, thisYear: T, otherYear: T): T {
	val date = toLocalDate()
	val today = nowMillis.toLocalDate()

	return when {
		date == today -> sameDay
		date.year == today.year -> thisYear
		else -> otherYear
	}
}

internal fun Long.toLocalDate(): LocalDate =
	Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date
