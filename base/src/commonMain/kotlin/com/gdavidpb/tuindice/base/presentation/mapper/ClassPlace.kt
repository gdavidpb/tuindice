package com.gdavidpb.tuindice.base.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.base.generated.resources.Res
import tuindice.base.generated.resources.class_place_with_time
import tuindice.base.generated.resources.class_time_range
import tuindice.base.generated.resources.class_time_range_same_period
import tuindice.base.generated.resources.date_time_am
import tuindice.base.generated.resources.date_time_pm

private const val MINUTES_PER_HOUR = 60
private const val NOON_HOUR = 12
private const val HOURS_PER_HALF_DAY = 12

/**
 * The place of a class from [startBlock] to [endBlock] in [classroom]. The room is [classroom] itself, or none
 * when it is blank. The schedule is the blocks ("1-2", or "3" for a single block) and, when [clockRange] says
 * where those blocks fall in the day, their hours after them ("1-2 · 7:30–9:30 a. m."). The blocks are always
 * there, so a class is never left without a time, and a block is never named with a word: the line is the
 * number, or the number and the hours.
 */
fun classPlaceItem(
	startBlock: Int,
	endBlock: Int,
	classroom: String,
	clockRange: IntRange?
): ClassPlaceItem {
	val blocks = if (startBlock == endBlock) "$startBlock" else "$startBlock-$endBlock"
	val scheduleText = clockRange?.let { range ->
		UiText.Resource(Res.string.class_place_with_time, listOf(blocks, range.toClockRangeText()))
	} ?: UiText.Raw(blocks)

	return ClassPlaceItem(
		room = classroom.takeIf { room -> room.isNotBlank() },
		scheduleText = scheduleText
	)
}

/**
 * The hours of a minutes-of-the-day range on a twelve-hour clock with no zero before the hour: the
 * suffix is said once when both ends fall on the same side of noon ("7:30–9:30 a. m.") and on each
 * end when they do not ("11:30 a. m.–1:30 p. m."). Noon itself is "12:30 p. m.".
 */
internal fun IntRange.toClockRangeText(): UiText {
	val startPeriod = first.toPeriodText()
	val endPeriod = last.toPeriodText()

	return if (startPeriod == endPeriod) {
		UiText.Resource(
			Res.string.class_time_range_same_period,
			listOf(first.toClockText(), last.toClockText(), startPeriod)
		)
	} else {
		UiText.Resource(
			Res.string.class_time_range,
			listOf(first.toClockText(), startPeriod, last.toClockText(), endPeriod)
		)
	}
}

// "7:30", "12:30", "1:30": the hour of a twelve-hour clock without padding, the minutes with it.
private fun Int.toClockText(): String {
	val hour12 = ((this / MINUTES_PER_HOUR) + HOURS_PER_HALF_DAY - 1) % HOURS_PER_HALF_DAY + 1
	val minutes = (this % MINUTES_PER_HOUR).toString().padStart(2, '0')

	return "$hour12:$minutes"
}

private fun Int.toPeriodText(): UiText {
	val period = if (this / MINUTES_PER_HOUR < NOON_HOUR) Res.string.date_time_am else Res.string.date_time_pm

	return UiText.Resource(period)
}
