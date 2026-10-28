package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAcademicCalendar
import kotlinx.datetime.LocalDate

// What a sheet of evaluations needs to group its rows by week: the calendar of the current term, and today.
internal class EvaluationWeeksContext(
	val calendar: AssistantAcademicCalendar,
	val today: LocalDate
)
