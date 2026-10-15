package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicScheduleEngine
import com.gdavidpb.tuindice.academiccore.domain.engine.ScheduleBlockClock
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.mapper.classPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import kotlinx.datetime.LocalDate
import tuindice.evaluations.generated.resources.Res
import tuindice.evaluations.generated.resources.evaluation_place_in_class

/**
 * The place an evaluation of [scheduleMode] on [date] inherits from [attemptMeetings], or null when
 * it inherits none (see [AcademicScheduleEngine.inheritedMeeting]).
 */
fun inheritedEvaluationPlace(
	attemptMeetings: List<ScheduleMeeting>,
	scheduleMode: EvaluationScheduleMode,
	date: LocalDate?
): ClassPlaceItem? {
	return AcademicScheduleEngine
		.inheritedMeeting(attemptMeetings = attemptMeetings, scheduleMode = scheduleMode, date = date)
		?.toClassPlaceItem()
}

/** Whether the subject of [this] meetings has class on [date]: a dot under that day of the calendar. */
fun List<ScheduleMeeting>.hasClassOn(date: LocalDate): Boolean {
	return AcademicScheduleEngine.meetingFor(attemptMeetings = this, date = date) != null
}

/**
 * The place of the class on [date] for the day chosen in the date editor, said as "En clase · 1-2 ·
 * MYS-116", or null when there is no class that day.
 */
fun List<ScheduleMeeting>.classPlaceOn(date: LocalDate): ClassPlaceItem? {
	return AcademicScheduleEngine.meetingFor(attemptMeetings = this, date = date)?.let { meeting ->
		val place = meeting.toClassPlaceItem()

		place.copy(placeText = UiText.Resource(Res.string.evaluation_place_in_class, listOf(place.placeText)))
	}
}

/** "1-2 · MYS-116" (or "Bloques 1-2", named only when the meeting has no room) and its hours. */
fun ScheduleMeeting.toClassPlaceItem(): ClassPlaceItem {
	return classPlaceItem(
		startBlock = startBlock,
		endBlock = endBlock,
		classroom = classroom,
		clockRange = ScheduleBlockClock.clockRangeOrNull(startBlock = startBlock, endBlock = endBlock)
	)
}
