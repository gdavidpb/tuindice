package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Coachmarks
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.confirmCoachmarks
import com.gdavidpb.tuindice.scenarios.shared.openSubjectSearch
import com.gdavidpb.tuindice.scenarios.shared.searchSubjectsFor
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags

private val pendingCoachmarksStart = Start.Seeded(E2eAccounts.Canonical, coachmarks = Coachmarks.Pending)

/** Starts on [section] with every coachmark pending, so the first screen the person sees is not the summary. */
private fun pendingCoachmarksOn(section: MainSection) =
	Start.Seeded(E2eAccounts.Canonical, section = section, coachmarks = Coachmarks.Pending).toLaunchSpec()

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

/** The record's two coachmarks, then the create-term screen the record opens, which has its own. */
private val coachmarksSyntheticTerm = scenario(
	"coachmarks-synthetic-term",
	"coachmarks",
	pendingCoachmarksOn(MainSection.RECORD)
) {
	covers("wizard.CoachmarkOverlay.PrimaryActionClick")
	account(E2eAccounts.Canonical.id)

	waitVisible(RecordUiTags.ContentContainer, Within.Sync)
	confirmCoachmarks(CoachmarkId.Record, CoachmarkId.RecordControls)
	tap(RecordUiTags.CreateSyntheticTermFab)
	waitVisible(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
	confirmCoachmarks(CoachmarkId.SyntheticTerm)
	// Nothing was touched: the form opens with its period preselected, and leaving it asks nothing.
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(RecordUiTags.ContentContainer, Within.Action)
	waitGone(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
	waitGone(RecordUiTags.DiscardSyntheticTermMessage, Within.Assert)
}

/** The evaluations list's two coachmarks, then the one of the editor the add button opens. */
private val coachmarksEvaluations = scenario(
	"coachmarks-evaluations",
	"coachmarks",
	pendingCoachmarksOn(MainSection.EVALUATIONS)
) {
	covers("wizard.CoachmarkOverlay.PrimaryActionClick")
	account(E2eAccounts.Canonical.id)

	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Sync)
	confirmCoachmarks(CoachmarkId.Evaluations, CoachmarkId.EvaluationsTools)
	tap(EvaluationsUiTags.EvaluationsAddFab)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Wait)
	confirmCoachmarks(CoachmarkId.EvaluationEditor)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

/** The pensum's two coachmarks, the subject search's, and the one of a subject's statistics. */
private val coachmarksPensumAndSubjects = scenario(
	"coachmarks-pensum-subjects",
	"coachmarks",
	pendingCoachmarksOn(MainSection.PENSUM)
) {
	covers("wizard.CoachmarkOverlay.PrimaryActionClick")
	account(E2eAccounts.Canonical.id)

	waitVisible(PensumUiTags.PensumScreen, Within.Sync)
	confirmCoachmarks(CoachmarkId.Pensum, CoachmarkId.PensumTools)
	openSubjectSearch()
	confirmCoachmarks(CoachmarkId.SubjectSearch)
	searchSubjectsFor(E2eFixtures.SubjectSearchCi.query)
	waitVisible(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value), Within.Wait)
	tap(SubjectsUiTags.searchResult(E2eFixtures.SubjectCi2511.value))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	confirmCoachmarks(CoachmarkId.SubjectDetail)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SubjectsUiTags.SearchScreen, Within.Action)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(PensumUiTags.PensumScreen, Within.Action)
}

/** The About screen's two coachmarks. */
private val coachmarksAbout = scenario(
	"coachmarks-about",
	"coachmarks",
	pendingCoachmarksOn(MainSection.ABOUT)
) {
	covers("wizard.CoachmarkOverlay.PrimaryActionClick")
	account(E2eAccounts.Canonical.id)

	waitVisible(AboutUiTags.ContentContainer, Within.Sync)
	confirmCoachmarks(CoachmarkId.About, CoachmarkId.AboutActions)
	waitVisible(AboutUiTags.ContentContainer, Within.Assert)
}

/** The scenarios of the `coachmarks` module; list every new one here. */
val coachmarksScenarios: List<Scenario> = listOf(
	coachmarksContextualSummary,
	coachmarksProgressiveRecord,
	coachmarksSyntheticTerm,
	coachmarksEvaluations,
	coachmarksPensumAndSubjects,
	coachmarksAbout
)
