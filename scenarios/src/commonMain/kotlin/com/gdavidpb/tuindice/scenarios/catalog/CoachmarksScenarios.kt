package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Coachmarks
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags

private val pendingCoachmarksStart = Start.Seeded(E2eAccounts.Canonical, coachmarks = Coachmarks.Pending)

/** The seeded summary with its coachmark up: the bubble anchored to the summary. */
private fun StepBuilder.awaitSummaryCoachmark() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitVisible(CoachmarkUiTags.currentCoachmark(CoachmarkId.Summary), Within.Action)
	waitVisible(CoachmarkUiTags.Bubble, Within.Assert)
}

private val coachmarksContextualSummary = scenario(
	"coachmarks-contextual-summary",
	"coachmarks",
	pendingCoachmarksStart.toLaunchSpec()
) {
	tags("smoke")
	covers("wizard.CoachmarkOverlay.PrimaryActionClick")
	account(E2eAccounts.Canonical.id)

	awaitSummaryCoachmark()
	waitVisible(CoachmarkUiTags.Host, Within.Assert)
	waitVisible(CoachmarkUiTags.anchor(CoachmarkId.Summary), Within.Assert)
	tap(CoachmarkUiTags.ConfirmButton)
	waitGone(CoachmarkUiTags.Bubble, Within.Action)
}

/** Confirming the summary coachmark leads to the record's, which has a second step that can go back. */
private val coachmarksProgressiveRecord = scenario(
	"coachmarks-progressive-record",
	"coachmarks",
	pendingCoachmarksStart.toLaunchSpec()
) {
	covers(
		"wizard.CoachmarkOverlay.PrimaryActionClick",
		"wizard.CoachmarkOverlay.PreviousActionClick"
	)
	account(E2eAccounts.Canonical.id)

	awaitSummaryCoachmark()
	tap(CoachmarkUiTags.ConfirmButton)
	waitGone(CoachmarkUiTags.Bubble, Within.Action)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	waitVisible(CoachmarkUiTags.currentCoachmark(CoachmarkId.Record), Within.Wait)
	waitVisible(CoachmarkUiTags.Bubble, Within.Assert)
	waitVisible(CoachmarkUiTags.anchor(CoachmarkId.Record), Within.Assert)
	tap(CoachmarkUiTags.ConfirmButton)
	waitVisible(CoachmarkUiTags.currentCoachmark(CoachmarkId.RecordControls), Within.Action)
	waitVisible(CoachmarkUiTags.BackButton, Within.Assert)
	tap(CoachmarkUiTags.BackButton)
	waitVisible(CoachmarkUiTags.currentCoachmark(CoachmarkId.Record), Within.Action)
	tap(CoachmarkUiTags.ConfirmButton)
	waitVisible(CoachmarkUiTags.currentCoachmark(CoachmarkId.RecordControls), Within.Action)
	tap(CoachmarkUiTags.ConfirmButton)
	waitGone(CoachmarkUiTags.Bubble, Within.Action)
}

/** The scenarios of the `coachmarks` module; list every new one here. */
val coachmarksScenarios: List<Scenario> = listOf(
	coachmarksContextualSummary,
	coachmarksProgressiveRecord
)
