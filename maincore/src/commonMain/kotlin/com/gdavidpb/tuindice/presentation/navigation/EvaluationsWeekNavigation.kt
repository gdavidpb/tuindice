package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import kotlinx.datetime.LocalDate

/**
 * The jump to a week of Evaluations and to one of its evaluations, from the Schedule sheet's marked
 * cell and from Summary: leaves the request for the tab to apply when it is ready, then closes whatever
 * entry is on top (the sheet) and goes there in one change of the stack. The request is state, so it
 * holds until the tab consumes it, and the start tab stays as it was.
 */
fun TuIndiceNavigator.showEvaluationsWeek(
	weekFocus: EvaluationsWeekFocusRepository,
	weekStart: LocalDate,
	evaluationId: String
) {
	weekFocus.request(weekStart = weekStart, evaluationId = evaluationId)
	popThenSwitchTab(MainSection.EVALUATIONS)
}
