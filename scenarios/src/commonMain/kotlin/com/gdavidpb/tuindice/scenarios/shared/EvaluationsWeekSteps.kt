package com.gdavidpb.tuindice.scenarios.shared

import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.ui.MaincoreUiTags

/**
 * After a marked cell of the Schedule sheet: the sheet is gone and the Evaluations tab is the selected one, on the
 * week of the frozen clock (week 3 of the hub term) with today's evaluation of that week in the list.
 */
fun StepBuilder.waitEvaluationsOnTheWeekOfTheClock() {
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek3), Within.Long)
	waitVisible(EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationToday.value), Within.Assert)
}
