package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlinx.datetime.LocalDate

/**
 * What the date dialog knows about the evaluation it picks a date for: the days it allows (null is
 * unbounded: no current term) and, when a subject is chosen, its code and weekly meetings, which put
 * a dot under its class days and say whether the chosen day has class.
 */
data class EvaluationDateCalendarItem(
	val selectableRange: ClosedRange<LocalDate>? = null,
	val subjectCode: String? = null,
	val meetings: List<ScheduleMeeting> = emptyList()
)
