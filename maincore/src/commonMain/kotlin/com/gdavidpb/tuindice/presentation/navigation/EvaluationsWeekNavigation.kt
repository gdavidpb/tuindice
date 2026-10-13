package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.repository.EvaluationsWeekFocusRepository
import kotlinx.datetime.LocalDate

/**
 * The jump to a week of Evaluations, from the Schedule sheet's marked cell and from Summary: leaves
 * the request for the tab to apply when it is ready, then closes whatever entry is on top (the
 * sheet) and goes there in one change of the stack. The request is state, so it holds until the tab
 * consumes it, and the start tab stays as it was.
 */
fun TuIndiceNavigator.showEvaluationsWeek(weekFocus: EvaluationsWeekFocusRepository, weekStart: LocalDate) {
	weekFocus.request(weekStart)
	popThenSwitchTab(MainSection.EVALUATIONS)
}
