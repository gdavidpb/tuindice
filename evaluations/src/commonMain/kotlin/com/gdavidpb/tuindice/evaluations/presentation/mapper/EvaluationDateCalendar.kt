package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationDateCalendarItem
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus

/**
 * The date dialog's item for a subject with [meetings]. The class days among the days of [selectableRange] are
 * worked out here, once, each with the place of its class, so the dialog only looks a date up. Without a range
 * there are no days to work out, and the dialog shows no class day.
 */
fun getEvaluationDateCalendarItem(
	selectableRange: ClosedRange<LocalDate>?,
	subjectCode: String?,
	meetings: List<ScheduleMeeting>
): EvaluationDateCalendarItem {
	if (selectableRange == null) return EvaluationDateCalendarItem(subjectCode = subjectCode)

	val classPlaces = buildMap<LocalDate, ClassPlaceItem> {
		val lastOffset = selectableRange.start.daysUntil(selectableRange.endInclusive)

		for (offset in 0..lastOffset) {
			val date = selectableRange.start.plus(DatePeriod(days = offset))

			meetings.classPlaceOn(date)?.let { place -> put(date, place) }
		}
	}

	return EvaluationDateCalendarItem(
		selectableRange = selectableRange,
		subjectCode = subjectCode,
		classDates = classPlaces.keys.toSet(),
		classPlaces = classPlaces
	)
}
