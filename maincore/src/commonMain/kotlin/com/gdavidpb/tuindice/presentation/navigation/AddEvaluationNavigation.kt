package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination

/**
 * The form that adds an evaluation, from the invitation of Summary: opened over the Evaluations
 * tab in one move, the same form the tab's own button opens. Like the other jumps of the hub it
 * does not write the section the app opens on.
 */
fun TuIndiceNavigator.showAddEvaluation() {
	showOverTab(
		section = MainSection.EVALUATIONS,
		key = EvaluationsDestination.Evaluation(evaluationId = null)
	)
}
