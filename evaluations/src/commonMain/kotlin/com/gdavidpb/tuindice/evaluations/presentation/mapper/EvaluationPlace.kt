package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleBlockClock
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationPlaceItem
import kotlinx.datetime.LocalDate
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_place_block
import tuindice.evaluations.generated.resources.evaluation_place_blocks
import tuindice.evaluations.generated.resources.evaluation_place_in_class
import tuindice.evaluations.generated.resources.evaluation_place_with_room

private const val MINUTES_PER_HOUR = 60
private const val EN_DASH = '–'

/**
 * The place an evaluation of [scheduleMode] on [date] inherits from [attemptMeetings], or null when
 * it inherits none (see [AcademicScheduleEngine.inheritedMeeting]).
 */
fun inheritedEvaluationPlace(
	attemptMeetings: List<ScheduleMeeting>,
	scheduleMode: EvaluationScheduleMode,
	date: LocalDate?
): EvaluationPlaceItem? {
	return AcademicScheduleEngine
		.inheritedMeeting(attemptMeetings = attemptMeetings, scheduleMode = scheduleMode, date = date)
		?.toEvaluationPlaceItem()
}

/** Whether the subject of [this] meetings has class on [date]: a dot under that day of the calendar. */
fun List<ScheduleMeeting>.hasClassOn(date: LocalDate): Boolean {
	return AcademicScheduleEngine.meetingFor(attemptMeetings = this, date = date) != null
}

/**
 * The place of the class on [date] for the day chosen in the date editor, said as "En clase · Bloques
 * 1-2 · MYS-116", or null when there is no class that day.
 */
fun List<ScheduleMeeting>.classPlaceOn(date: LocalDate): EvaluationPlaceItem? {
	return AcademicScheduleEngine.meetingFor(attemptMeetings = this, date = date)?.let { meeting ->
		val place = meeting.toEvaluationPlaceItem()

		place.copy(placeText = UiText.Resource(Res.string.evaluation_place_in_class, listOf(place.placeText)))
	}
}

/** "Bloques 1-2 · MYS-116" (or "Bloque 3", with no room when the meeting has none) and its hours. */
fun ScheduleMeeting.toEvaluationPlaceItem(): EvaluationPlaceItem {
	val blocks = if (startBlock == endBlock) {
		UiText.Resource(Res.string.evaluation_place_block, listOf(startBlock))
	} else {
		UiText.Resource(Res.string.evaluation_place_blocks, listOf(startBlock, endBlock))
	}
	val placeText = if (classroom.isBlank()) {
		blocks
	} else {
		UiText.Resource(Res.string.evaluation_place_with_room, listOf(blocks, classroom))
	}

	return EvaluationPlaceItem(placeText = placeText, timeText = clockTimeText())
}

// "7:30–9:30": from the start of the first block to the end of the last. Null when either falls
// outside the day (block 17 on), where an hour would be wrong rather than late.
private fun ScheduleMeeting.clockTimeText(): String? {
	if (!ScheduleBlockClock.isWithinDay(startBlock) || !ScheduleBlockClock.isWithinDay(endBlock)) return null

	val start = ScheduleBlockClock.blockStartMinute(startBlock).toClockText()
	val end = ScheduleBlockClock.blockEndMinute(endBlock).toClockText()

	return "$start$EN_DASH$end"
}

private fun Int.toClockText(): String {
	return "${this / MINUTES_PER_HOUR}:${(this % MINUTES_PER_HOUR).toString().padStart(2, '0')}"
}
