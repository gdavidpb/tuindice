package com.gdavidpb.tuindice.evaluations.presentation.mapper

import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation

/**
 * The content with its date calendar worked out from the chosen subject and the selectable range. The producers call
 * it where either one changes, so the calendar kept on the state always belongs to the subject that it shows.
 */
fun Evaluation.State.Content.withDateCalendar(): Evaluation.State.Content {
	return copy(
		dateCalendar = getEvaluationDateCalendarItem(
			selectableRange = selectableDateRange,
			subjectCode = selectedAttempt?.code,
			meetings = selectedAttempt?.meetings.orEmpty()
		)
	)
}
