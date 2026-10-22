package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/**
 * After a marked cell of the Schedule sheet: the sheet is gone and the Evaluations tab is the selected one, on the
 * week of the frozen clock (week 3 of the hub term) with the evaluation the cell marks in view: the jump brings it
 * into the list, so its card is there when the tab settles.
 */
fun StepBuilder.waitEvaluationsOnTheWeekOfTheClock(evaluationId: String) {
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek3), Within.Long)
	waitVisible(EvaluationsUiTags.evaluationItemCard(evaluationId), Within.Assert)
}

/**
 * The graded evaluation of the fixtures is on the week before the last one, and the strip opens on the last one: the
 * frozen clock is past the term's last week. Picking the week before it brings the evaluation's list in: the card
 * of the evaluation is in view before the step goes on, so the gesture that follows starts on that week.
 */
fun StepBuilder.showWeekOfGradedEvaluation() {
	val week = E2eInputs.LastAcademicWeek - 1

	tap(EvaluationsUiTags.evaluationsWeekChip(week))
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(week), Within.Action)
	waitVisible(EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value), Within.Action)
}
