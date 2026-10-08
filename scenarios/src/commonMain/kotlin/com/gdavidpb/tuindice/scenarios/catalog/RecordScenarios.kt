package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.enterText
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.submitTextEntry
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAt
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.confirmCoachmarks
import com.gdavidpb.tuindice.scenarios.shared.signInThroughUi
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import kotlin.time.Duration.Companion.milliseconds

private const val SHEET_SWIPE_MS = 600L

// The status and the action of a subject in the search results, as the tags of the rows name them: the
// lowercase name of the enum the rows are built from, so a rename in the product breaks the compile.
private val STATUS_AVAILABLE = SyntheticTermSubjectAvailability.AVAILABLE.name.lowercase()
private val STATUS_ALREADY_PLANNED = SyntheticTermSubjectAvailability.ALREADY_PLANNED.name.lowercase()
private val STATUS_BLOCKED = SyntheticTermSubjectAvailability.BLOCKED.name.lowercase()
private val STATUS_NOT_IN_PENSUM = SyntheticTermSubjectAvailability.NOT_IN_PENSUM.name.lowercase()
private val STATUS_APPROVED = SyntheticTermSubjectAvailability.APPROVED.name.lowercase()
private val STATUS_SLOT = SyntheticTermSubjectAvailability.COUNTS_AS_SLOT.name.lowercase()
private val ACTION_ADD = CreateTermSubjectCardAction.Add.name.lowercase()
private val ACTION_REMOVE = CreateTermSubjectCardAction.Remove.name.lowercase()
private const val CLASH_DAY = 2

/** A search text that has no results; the other ones the scenarios type are fixtures the mocks answer. */
private const val QUERY_NO_RESULTS = "zz"

private val searches = E2eFixtures.recordSearches.associateBy { it.query }

private val queryEc = searches.getValue("ec").query
private val queryMa = searches.getValue("ma").query
private val queryMa1111 = searches.getValue("ma1111").query
private val queryPr = searches.getValue("pr").query
private val querySlot = searches.getValue("slot").query

private val scheduleAction = BaseUiTags.topBarActionButton(TopBarAction.RecordScheduleAction)

private fun subjectAction(code: String, action: String) = RecordUiTags.createSyntheticTermSubjectAction(code, action)

private fun subjectStatus(code: String, status: String) = RecordUiTags.createSyntheticTermSubjectStatus(code, status)

/** From the summary the start leaves the app on, to the record tab with its content. */
private fun StepBuilder.openRecordTab() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
}

private fun StepBuilder.openCreateTermScreen() {
	tap(RecordUiTags.CreateSyntheticTermFab)
	waitVisible(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
}

/** Picks the term after the current one in the period selector, which lists its options by text. */
private fun StepBuilder.pickNextPeriod() {
	tap(RecordUiTags.CreateSyntheticTermPeriodSelector)
	waitVisible(text(Copy.TermSepDec2026), Within.Action)
	tap(text(Copy.TermSepDec2026))
}

private fun StepBuilder.openSearchTab() {
	tap(RecordUiTags.CreateSyntheticTermSearchTab)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchField, Within.Action)
}

private fun StepBuilder.searchSubjects(query: String, replace: Boolean = false) {
	tap(RecordUiTags.CreateSyntheticTermSearchField)
	enterText(RecordUiTags.CreateSyntheticTermSearchField, query, replace = replace)
	submitTextEntry()
}

private fun StepBuilder.addSubject(code: String, selectedCount: String) {
	scrollUntilVisible(subjectAction(code, ACTION_ADD), Scroll.ContentDown, Within.Wait)
	tap(subjectAction(code, ACTION_ADD))
	waitVisible(text(selectedCount), Within.Action)
}

private fun StepBuilder.submitTerm() {
	tap(RecordUiTags.CreateSyntheticTermSubmitButton)
	waitGone(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
}

private val recordSmoke = scenario(
	"record-smoke",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("record.Record.SetViewMode")
	tags("smoke")
	account(E2eAccounts.Canonical.id)

	openRecordTab()
	waitVisible(RecordUiTags.TermSelectorRow, Within.Assert)
	waitVisible(RecordUiTags.AttemptsList, Within.Assert)
	tap(RecordUiTags.TopBarViewModeSwitch)
	waitVisible(RecordUiTags.TopBarViewModeBanner, Within.Assert)
	tap(RecordUiTags.TopBarViewModeSwitch)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(RecordUiTags.EnrollmentProofButton, Within.Action)
}

private val recordRefreshRetry = scenario(
	"record-refresh-retry",
	"record",
	Start.Seeded(E2eAccounts.RecordRefreshRetry).toLaunchSpec()
) {
	covers("record.Record.RefreshRecord")
	account(E2eAccounts.RecordRefreshRetry.id)

	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItem, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(BaseUiTags.ErrorViewContainer, Within.Long)
	tap(BaseUiTags.ErrorViewRetryButton)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	waitVisible(RecordUiTags.TermSelectorRow, Within.Assert)
	waitVisible(RecordUiTags.AttemptsList, Within.Assert)
}

private val recordTermSelection = scenario(
	"record-term-selection",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"record.Record.SelectTerm",
		"record.Record.SetViewMode"
	)
	account(E2eAccounts.Canonical.id)

	openRecordTab()
	waitVisible(RecordUiTags.TermSelectorRow, Within.Assert)
	waitVisible(RecordUiTags.attemptItem(E2eFixtures.SyntheticEp1308Attempt.value), Within.Assert)
	waitVisible(RecordUiTags.EditSyntheticTermButton, Within.Assert)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(RecordUiTags.attemptItem(E2eFixtures.PrimaryAttempt.value), Within.Action)
	waitVisible(RecordUiTags.EnrollmentProofButton, Within.Assert)
	tap(RecordUiTags.TopBarViewModeSwitch)
	waitVisible(RecordUiTags.TopBarViewModeBanner, Within.Action)
	waitVisible(RecordUiTags.attemptItem(E2eFixtures.SecondaryAttempt.value), Within.Action)
	tap(RecordUiTags.TopBarViewModeSwitch)
	waitVisible(RecordUiTags.attemptItem(E2eFixtures.PrimaryAttempt.value), Within.Action)
	waitVisible(RecordUiTags.EnrollmentProofButton, Within.Assert)
}

private val recordAttemptOverrides = scenario(
	"record-attempt-overrides",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"record.Record.SelectTerm",
		"record.Record.UpsertAttemptSelection"
	)
	account(E2eAccounts.Canonical.id)

	val primary = E2eFixtures.PrimaryAttempt.value
	val synthetic = E2eFixtures.SyntheticEp1308Attempt.value

	openRecordTab()
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(RecordUiTags.attemptGradeValue(primary, E2eInputs.GradeBefore), Within.Action)
	tapAt(RecordUiTags.attemptGradeSlider(primary), E2eInputs.SliderHighX, E2eInputs.SliderMiddleY)
	waitVisible(RecordUiTags.attemptGradeValue(primary, E2eInputs.GradeAfter), Within.Action)
	tapAt(RecordUiTags.attemptGradeSlider(primary), E2eInputs.SliderLowX, E2eInputs.SliderMiddleY)
	waitVisible(RecordUiTags.attemptGradeValue(primary, E2eInputs.GradeBefore), Within.Action)
	tap(RecordUiTags.termChip(E2eFixtures.SyntheticDegreeProjectTerm.value))
	waitVisible(RecordUiTags.attemptStatusSelector(synthetic), Within.Action)
	tap(RecordUiTags.attemptStatusSelector(synthetic))
	tap(RecordUiTags.attemptStatusOption(synthetic, E2eInputs.AttemptApproved))
	waitVisible(RecordUiTags.attemptStatusValue(synthetic, E2eInputs.AttemptApproved), Within.Action)
}

private val recordSyntheticTermSearchEmpty = scenario(
	"record-synthetic-term-search-empty",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"record.CreateSyntheticTerm.SelectAddSubjectTab",
		"record.CreateSyntheticTerm.UpdateQuery"
	)
	account(E2eAccounts.Canonical.id)

	// The suggestions come from the pensum the app has cached, which opening its tab fills.
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.Canvas, Within.Long)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	openCreateTermScreen()
	openSearchTab()
	searchSubjects(QUERY_NO_RESULTS)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchField, Within.Assert)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchResultsTitle, Within.Wait)
	waitVisible(text(Copy.RecordSearchNoResults), Within.Assert)
	waitGone(RecordUiTags.createSyntheticTermSubject(E2eFixtures.SubjectEc5201.value), Within.Assert)
	waitVisible(text(Copy.SearchSuggestedTitle), Within.Assert)
	waitVisible(RecordUiTags.createSyntheticTermSubject(E2eFixtures.SubjectMa1112.value), Within.Assert)
}

private val recordSyntheticTermSearchStates = scenario(
	"record-synthetic-term-search-states",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"record.CreateSyntheticTerm.SelectAddSubjectTab",
		"record.CreateSyntheticTerm.UpdateQuery"
	)
	account(E2eAccounts.Canonical.id)

	val ma1112 = E2eFixtures.SubjectMa1112.value
	val ma1121 = E2eFixtures.SubjectMa1121.value
	val ma1111 = E2eFixtures.SubjectMa1111.value
	val ep1308 = E2eFixtures.SubjectEp1308.value
	val ep2308 = E2eFixtures.SubjectEp2308.value
	val aa1001 = E2eFixtures.SubjectAa1001.value
	val ab1001 = E2eFixtures.SubjectAb1001.value
	val eg1511 = E2eFixtures.SubjectEg1511.value
	val ci5312 = E2eFixtures.SubjectCi5312.value

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.Canvas, Within.Long)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	openCreateTermScreen()
	waitVisible(subjectStatus(ma1121, STATUS_AVAILABLE), Within.Wait)
	waitVisible(subjectAction(ma1121, ACTION_ADD), Within.Assert)
	openSearchTab()

	// "pr": the pensum's priority subjects first, with what each one lets the student do.
	searchSubjects(queryPr, replace = true)
	waitVisible(RecordUiTags.createSyntheticTermSearchResult(0, ep1308), Within.Wait)
	waitVisible(subjectStatus(ep1308, STATUS_ALREADY_PLANNED), Within.Assert)
	tap(subjectStatus(ep1308, STATUS_ALREADY_PLANNED))
	waitVisible(text(Copy.TooltipPlannedIn), Within.Assert)
	tap(subjectStatus(ep1308, STATUS_ALREADY_PLANNED))
	waitVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(ep1308), Within.Assert)
	waitGone(subjectAction(ep1308, ACTION_ADD), Within.Assert)
	scrollUntilVisible(RecordUiTags.createSyntheticTermSearchResult(1, ep2308), Scroll.ContentDown, Within.Wait)
	waitVisible(subjectStatus(ep2308, STATUS_BLOCKED), Within.Assert)
	waitVisible(text(Copy.SubjectStatusBlocked), Within.Assert)
	tap(subjectStatus(ep2308, STATUS_BLOCKED))
	waitVisible(text(Copy.TooltipMissingRequirements), Within.Assert)
	tap(subjectStatus(ep2308, STATUS_BLOCKED))
	waitVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(ep2308), Within.Assert)
	waitVisible(subjectAction(ep2308, ACTION_ADD), Within.Assert)
	scrollUntilVisible(subjectStatus(aa1001, STATUS_NOT_IN_PENSUM), Scroll.ContentDown, Within.Wait)
	waitVisible(subjectStatus(aa1001, STATUS_NOT_IN_PENSUM), Within.Assert)
	waitVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(aa1001), Within.Assert)
	scrollUntilVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(aa1001), Scroll.ContentDown, Within.Wait)
	tap(RecordUiTags.createSyntheticTermSubjectStatsButton(aa1001))
	waitVisible(SubjectsUiTags.Content, Within.Long)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	// Coming back gives the search field its focus again (the screen asks for it whenever the search tab is
	// selected): the keyboard covers half the list, and the scroll below swipes over the bar and the keyboard
	// instead of over the results. The field is on screen once that request has been made.
	waitVisible(RecordUiTags.CreateSyntheticTermSearchField, Within.Action)
	submitTextEntry()
	scrollUntilVisible(subjectStatus(aa1001, STATUS_NOT_IN_PENSUM), Scroll.ContentDown, Within.Wait)
	waitVisible(subjectStatus(aa1001, STATUS_NOT_IN_PENSUM), Within.Assert)
	waitVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(aa1001), Within.Assert)
	scrollUntilVisible(subjectStatus(ab1001, STATUS_NOT_IN_PENSUM), Scroll.ContentDown, Within.Wait)
	waitVisible(subjectStatus(ab1001, STATUS_NOT_IN_PENSUM), Within.Assert)
	scrollUntilVisible(RecordUiTags.CreateSyntheticTermSearchTab, Scroll.ContentUp, Within.Wait)

	// "ma": the open subjects of the pensum.
	searchSubjects(queryMa, replace = true)
	waitVisible(RecordUiTags.createSyntheticTermSearchResult(0, ma1112), Within.Wait)
	waitVisible(subjectStatus(ma1112, STATUS_AVAILABLE), Within.Assert)
	waitVisible(text(Copy.SubjectStatusAvailable), Within.Assert)
	waitVisible(subjectAction(ma1112, ACTION_ADD), Within.Assert)
	waitVisible(RecordUiTags.createSyntheticTermSearchResult(1, ma1121), Within.Assert)
	waitVisible(subjectStatus(ma1121, STATUS_AVAILABLE), Within.Assert)
	waitVisible(text(Copy.SubjectStatusAvailable), Within.Assert)
	waitVisible(subjectAction(ma1121, ACTION_ADD), Within.Assert)
	scrollUntilVisible(RecordUiTags.CreateSyntheticTermSearchField, Scroll.ContentUp, Within.Wait)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchClearButton, Within.Action)
	tap(RecordUiTags.CreateSyntheticTermSearchClearButton)

	// "ma1111": a subject already approved hides behind the toggle of the taken subjects.
	searchSubjects(queryMa1111)
	waitVisible(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle, Within.Wait)
	tap(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle)
	scrollUntilVisible(subjectStatus(ma1111, STATUS_APPROVED), Scroll.ContentDown, Within.Wait)
	waitVisible(subjectStatus(ma1111, STATUS_APPROVED), Within.Assert)
	waitVisible(text(Copy.SubjectStatusApproved), Within.Assert)
	tap(subjectStatus(ma1111, STATUS_APPROVED))
	waitVisible(text(Copy.TooltipApprovedIn), Within.Assert)
	waitVisible(RecordUiTags.createSyntheticTermSubjectStatsButton(ma1111), Within.Assert)
	waitGone(subjectAction(ma1111, ACTION_ADD), Within.Assert)

	// Not fixed courses of the fixture pensum, but its open slots take them: EG1511 an Estudios Generales
	// slot (EG prefix, the fourth one is only current) and CI5312 an area elective (4 UC, one still open).
	scrollUntilVisible(RecordUiTags.CreateSyntheticTermSearchField, Scroll.ContentUp, Within.Wait)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchClearButton, Within.Action)
	tap(RecordUiTags.CreateSyntheticTermSearchClearButton)
	searchSubjects(querySlot)
	waitVisible(subjectStatus(eg1511, STATUS_SLOT), Within.Wait)
	waitVisible(text(Copy.SubjectCountsAsGeneralStudies), Within.Assert)
	waitVisible(subjectAction(eg1511, ACTION_ADD), Within.Assert)
	scrollUntilVisible(subjectStatus(ci5312, STATUS_SLOT), Scroll.ContentDown, Within.Wait)
	waitVisible(text(Copy.SubjectCountsAsElective), Within.Assert)
	waitVisible(subjectAction(ci5312, ACTION_ADD), Within.Assert)
}

private val recordSyntheticTermDiscard = scenario(
	"record-synthetic-term-discard",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("record.CreateSyntheticTerm.SelectPeriod")
	account(E2eAccounts.Canonical.id)

	openRecordTab()
	openCreateTermScreen()
	pickNextPeriod()
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(RecordUiTags.DiscardSyntheticTermMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(RecordUiTags.DiscardSyntheticTermMessage, Within.Action)
	waitVisible(RecordUiTags.CreateSyntheticTermScreen, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(RecordUiTags.DiscardSyntheticTermMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(RecordUiTags.ContentContainer, Within.Action)
}

private val recordSyntheticTermLifecycle = scenario(
	"record-synthetic-term-lifecycle",
	"record",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"record.CreateSyntheticTerm.AddSubject",
		"record.CreateSyntheticTerm.ConfigureTerm",
		"record.CreateSyntheticTerm.CreateTerm",
		"record.CreateSyntheticTerm.RemoveSubject",
		"record.CreateSyntheticTerm.SelectAddSubjectTab",
		"record.CreateSyntheticTerm.SelectPeriod",
		"record.CreateSyntheticTerm.UpdateQuery",
		"record.Record.DeleteSyntheticTerm",
		"record.Record.SelectTerm"
	)
	account(E2eAccounts.Canonical.id)

	val ec5333 = E2eFixtures.SubjectEc5333.value
	val ma1121 = E2eFixtures.SubjectMa1121.value
	val nextTerm = E2eFixtures.NextTermKey.value

	openRecordTab()
	openCreateTermScreen()
	waitVisible(RecordUiTags.CreateSyntheticTermPeriodSelector, Within.Assert)
	pickNextPeriod()
	assertEnabled(RecordUiTags.CreateSyntheticTermSubmitButton, false)
	openSearchTab()
	tap(RecordUiTags.CreateSyntheticTermSearchField)
	enterText(RecordUiTags.CreateSyntheticTermSearchField, queryEc)
	tap(RecordUiTags.CreateSyntheticTermSearchClearButton)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchField, Within.Assert)
	searchSubjects(queryEc)
	addSubject(ec5333, Copy.TermSelectedOne)
	assertEnabled(RecordUiTags.CreateSyntheticTermSubmitButton, true)
	// Taking the subject off the selection leaves a term with none, which cannot be saved; adding it again can.
	scrollUntilVisible(subjectAction(ec5333, ACTION_REMOVE), Scroll.ContentUp, Within.Wait)
	tap(subjectAction(ec5333, ACTION_REMOVE))
	waitGone(subjectAction(ec5333, ACTION_REMOVE), Within.Action)
	assertEnabled(RecordUiTags.CreateSyntheticTermSubmitButton, false, Within.Action)
	addSubject(ec5333, Copy.TermSelectedOne)
	assertEnabled(RecordUiTags.CreateSyntheticTermSubmitButton, true)
	submitTerm()
	waitVisible(text(ec5333), Within.Wait)

	// The same term, edited: a second subject joins it.
	tap(RecordUiTags.EditSyntheticTermButton)
	waitVisible(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
	waitVisible(text(Copy.EditTermTitle), Within.Action)
	waitVisible(text(Copy.EditTermButton), Within.Action)
	assertEnabled(RecordUiTags.CreateSyntheticTermSubmitButton, false)
	waitVisible(subjectAction(ec5333, ACTION_REMOVE), Within.Assert)
	tap(RecordUiTags.CreateSyntheticTermSearchTab)
	submitTextEntry()
	scrollUntilVisible(RecordUiTags.CreateSyntheticTermSearchField, Scroll.ContentDown, Within.Action)
	searchSubjects(queryMa, replace = true)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchResultsTitle, Within.Wait)
	addSubject(ma1121, Copy.TermSelectedTwo)
	waitVisible(text(Copy.EditTermButton), Within.Assert)
	submitTerm()
	waitVisible(text(ma1121), Within.Wait)
	waitVisible(text(ec5333), Within.Assert)

	// And deleted.
	tap(RecordUiTags.termChip(nextTerm))
	waitVisible(RecordUiTags.DeleteSyntheticTermButton, Within.Action)
	tap(RecordUiTags.DeleteSyntheticTermButton)
	waitVisible(BaseUiTags.ConfirmationDialogSheet, Within.Action)
	waitVisible(RecordUiTags.DeleteSyntheticTermMessage, Within.Assert)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitGone(RecordUiTags.termChip(nextTerm), Within.Wait)
}

// Server-side terminal rejection of a synthetic term edit: the account earns an access token whose
// PATCH /record/v5/overlay/terms/* always returns 400, so the optimistic edit must revert and the
// rejection snackbar must surface.
private val recordSyntheticTermRejected = scenario(
	"record-synthetic-term-rejected",
	"record",
	Start.Seeded(E2eAccounts.RecordTermRejected).toLaunchSpec()
) {
	covers(
		"record.CreateSyntheticTerm.AddSubject",
		"record.CreateSyntheticTerm.ConfigureTerm",
		"record.CreateSyntheticTerm.CreateTerm",
		"record.CreateSyntheticTerm.SelectAddSubjectTab",
		"record.CreateSyntheticTerm.SelectPeriod",
		"record.CreateSyntheticTerm.UpdateQuery"
	)
	account(E2eAccounts.RecordTermRejected.id)

	val ec5333 = E2eFixtures.SubjectEc5333.value
	val ma1121 = E2eFixtures.SubjectMa1121.value

	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItem, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	openCreateTermScreen()
	pickNextPeriod()
	openSearchTab()
	searchSubjects(queryEc)
	addSubject(ec5333, Copy.TermSelectedOne)
	assertEnabled(RecordUiTags.CreateSyntheticTermSubmitButton, true)
	submitTerm()
	waitVisible(text(ec5333), Within.Wait)

	// The edit the server rejects.
	tap(RecordUiTags.EditSyntheticTermButton)
	waitVisible(RecordUiTags.CreateSyntheticTermScreen, Within.Action)
	waitVisible(text(Copy.EditTermButton), Within.Action)
	tap(RecordUiTags.CreateSyntheticTermSearchTab)
	submitTextEntry()
	scrollUntilVisible(RecordUiTags.CreateSyntheticTermSearchField, Scroll.ContentDown, Within.Action)
	searchSubjects(queryMa, replace = true)
	waitVisible(RecordUiTags.CreateSyntheticTermSearchResultsTitle, Within.Wait)
	addSubject(ma1121, Copy.TermSelectedTwo)
	submitTerm()
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitGone(text(ma1121), Within.Wait)
	waitVisible(text(ec5333), Within.Assert)
	waitVisible(RecordUiTags.termChip(E2eFixtures.NextTermKey.value), Within.Assert)
}

private val recordAnnulledProvisionalSchedule = scenario(
	"record-annulled-provisional-schedule",
	"record",
	Start.Seeded(E2eAccounts.AnnulledProvisional).toLaunchSpec()
) {
	covers(
		"record.Record.SelectTerm",
		"record.Schedule.SelectScheduleView"
	)
	account(E2eAccounts.AnnulledProvisional.id)

	val primary = E2eFixtures.PrimaryAttempt.value
	val clashing = E2eFixtures.ClashingAttempt.value

	openRecordTab()
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(BaseUiTags.NoticeView, Within.Action)
	waitVisible(text(Copy.EnrollmentAnnulledProvisionalTitle), Within.Assert)
	waitVisible(RecordUiTags.attemptDetail(primary), Within.Assert)
	waitVisible(RecordUiTags.attemptEnrollmentError(clashing), Within.Assert)
	waitGone(RecordUiTags.ScheduleViewSwitch, Within.Assert)
	tap(scheduleAction)
	waitVisible(RecordUiTags.ScheduleTable, Within.Action)
	waitVisible(RecordUiTags.ScheduleTitle, Within.Assert)
	waitVisible(RecordUiTags.scheduleTableRow(primary), Within.Assert)
	waitVisible(text(Copy.ScheduleSectionMys116), Within.Assert)
	waitVisible(RecordUiTags.scheduleTableClash(primary, CLASH_DAY), Within.Assert)
	waitVisible(RecordUiTags.scheduleTableClash(clashing, CLASH_DAY), Within.Assert)
	waitVisible(RecordUiTags.scheduleTableUnscheduled(E2eFixtures.UnscheduledAttempt.value), Within.Assert)
	waitVisible(text(Copy.ScheduleUnscheduled), Within.Assert)
	tap(RecordUiTags.ScheduleViewWeekTab)
	waitVisible(RecordUiTags.ScheduleGrid, Within.Action)
	waitGone(RecordUiTags.ScheduleTable, Within.Assert)
	waitVisible(RecordUiTags.ScheduleUnscheduled, Within.Assert)
	waitVisible(text(Copy.ScheduleUnscheduledEg1114), Within.Assert)
	// The schedule is a sheet over the record: dragging it down by its title closes it.
	swipeFrom(RecordUiTags.ScheduleTitle, SwipeDirection.Down, SHEET_SWIPE_MS.milliseconds)
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
	waitVisible(RecordUiTags.AttemptsList, Within.Assert)
	waitVisible(BaseUiTags.NoticeView, Within.Assert)
	waitGone(RecordUiTags.ScheduleViewSwitch, Within.Assert)
	waitGone(RecordUiTags.ScheduleGrid, Within.Assert)
}

private val recordScheduleViewRemembered = scenario(
	"record-schedule-view-remembered",
	"record",
	Start.Seeded(E2eAccounts.AnnulledProvisional).toLaunchSpec()
) {
	covers(
		"maincore.Main.RequestSignOut",
		"record.Record.SelectTerm",
		"record.Schedule.SelectScheduleView"
	)
	account(E2eAccounts.AnnulledProvisional.id)
	signsIn()

	openRecordTab()
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	tap(scheduleAction)
	waitVisible(RecordUiTags.ScheduleTable, Within.Action)
	// A first visit opens on the table, with the switch next to the title.
	waitVisible(RecordUiTags.ScheduleViewSwitch, Within.Assert)
	waitGone(RecordUiTags.ScheduleGrid, Within.Assert)
	tap(RecordUiTags.ScheduleViewWeekTab)
	waitVisible(RecordUiTags.ScheduleGrid, Within.Action)
	waitGone(RecordUiTags.ScheduleTable, Within.Assert)
	tap(RecordUiTags.ScheduleViewTableTab)
	waitVisible(RecordUiTags.ScheduleTable, Within.Action)
	waitGone(RecordUiTags.ScheduleGrid, Within.Assert)
	tap(RecordUiTags.ScheduleViewWeekTab)
	waitVisible(RecordUiTags.ScheduleGrid, Within.Action)
	swipeFrom(RecordUiTags.ScheduleTitle, SwipeDirection.Down, SHEET_SWIPE_MS.milliseconds)
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)
	// The view chosen is remembered: the sheet opens again on the week.
	tap(scheduleAction)
	waitVisible(RecordUiTags.ScheduleGrid, Within.Action)
	waitGone(RecordUiTags.ScheduleTable, Within.Assert)
	swipeFrom(RecordUiTags.ScheduleTitle, SwipeDirection.Down, SHEET_SWIPE_MS.milliseconds)
	waitGone(RecordUiTags.ScheduleSheet, Within.Action)

	// Signing out forgets it, in the same process: the next session starts on the table again.
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
	waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Wait)
	signInThroughUi(E2eAccounts.AnnulledProvisional)
	waitVisible(SummaryUiTags.ContentContainer, Within.Long)
	confirmCoachmarks(CoachmarkId.Summary)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	confirmCoachmarks(CoachmarkId.Record, CoachmarkId.RecordControls)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	tap(scheduleAction)
	waitVisible(RecordUiTags.ScheduleTable, Within.Action)
	waitGone(RecordUiTags.ScheduleGrid, Within.Assert)
}

private val recordStaleEnrollmentNotice = scenario(
	"record-stale-enrollment-notice",
	"record",
	Start.Seeded(E2eAccounts.EnrollmentUnavailable).toLaunchSpec()
) {
	covers("record.Record.SelectTerm")
	account(E2eAccounts.EnrollmentUnavailable.id)

	openRecordTab()
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	// The enrollment service did not answer: the term on screen is the last one read, and the notice says so.
	waitVisible(BaseUiTags.NoticeView, Within.Action)
	waitVisible(BaseUiTags.NoticeMessage, Within.Assert)
	waitVisible(text(Copy.StaleEnrollmentMessage), Within.Assert)
	waitVisible(RecordUiTags.AttemptsList, Within.Assert)
}

private val recordWithdrawnSubject = scenario(
	"record-withdrawn-subject",
	"record",
	Start.Seeded(E2eAccounts.AnnulledProvisional).toLaunchSpec()
) {
	covers("record.Record.SelectTerm")
	account(E2eAccounts.AnnulledProvisional.id)

	openRecordTab()
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	// A withdrawn subject stays in the term's list, readable, and says so.
	scrollUntilVisible(
		RecordUiTags.attemptStatusChip(E2eFixtures.WithdrawnAttempt.value),
		Scroll.ContentDown,
		Within.Wait
	)
	waitVisible(text(Copy.AttemptRetired), Within.Assert)
	// It carries a schedule in the fixture, and still has no row in the schedule: only what is being taken is
	// drawn there.
	tap(scheduleAction)
	waitVisible(RecordUiTags.ScheduleTable, Within.Action)
	waitVisible(RecordUiTags.scheduleTableRow(E2eFixtures.PrimaryAttempt.value), Within.Assert)
	waitGone(RecordUiTags.scheduleTableRow(E2eFixtures.WithdrawnAttempt.value), Within.Assert)
}

private val recordAnnulledFinalNotice = scenario(
	"record-annulled-final-notice",
	"record",
	Start.Seeded(E2eAccounts.AnnulledFinal).toLaunchSpec()
) {
	account(E2eAccounts.AnnulledFinal.id)

	openRecordTab()
	waitVisible(BaseUiTags.NoticeView, Within.Action)
	waitVisible(text(Copy.EnrollmentAnnulledFinalTitle), Within.Assert)
	waitVisible(RecordUiTags.TermSelectorRow, Within.Assert)
	waitGone(scheduleAction, Within.Assert)
}

/** The scenarios of this module; list every new one here. */
val recordScenarios: List<Scenario> = listOf(
	recordSmoke,
	recordRefreshRetry,
	recordTermSelection,
	recordAttemptOverrides,
	recordSyntheticTermSearchEmpty,
	recordSyntheticTermSearchStates,
	recordSyntheticTermDiscard,
	recordSyntheticTermLifecycle,
	recordSyntheticTermRejected,
	recordAnnulledProvisionalSchedule,
	recordScheduleViewRemembered,
	recordStaleEnrollmentNotice,
	recordWithdrawnSubject,
	recordAnnulledFinalNotice
)
