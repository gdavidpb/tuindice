package com.gdavidpb.tuindice.summary.domain.model

import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import kotlinx.datetime.LocalDate

/**
 * The class being taught ([isInProgress]) or, when there is none, the next one, which starts on
 * [date] (today when it is later today).
 */
data class SummaryClassMoment(
	val meeting: ScheduleMeeting,
	val isInProgress: Boolean,
	val date: LocalDate,
	val isToday: Boolean
)
