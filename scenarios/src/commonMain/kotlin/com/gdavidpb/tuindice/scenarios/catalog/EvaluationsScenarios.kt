package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.closeScheduleSheet
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

private fun seeded(account: E2eAccount) = Start.Seeded(account).toLaunchSpec()

/** From the seeded summary to the evaluations tab, waiting for [content] to show on arrival. */
private fun StepBuilder.openEvaluations(content: String) {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(content, Within.Long)
}

/** The add form, reached from the evaluations list. */
private fun StepBuilder.openAddForm() {
	tap(EvaluationsUiTags.EvaluationsAddFab)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Wait)
}

/** Scrolls to the graded evaluation of the fixtures and taps its card to reveal the swipe actions. */
private fun StepBuilder.revealGradedEvaluationActions(action: String) {
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value),
		Scroll.ContentDown,
		Within.Wait
	)
	tap(EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value))
	waitVisible(action, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer, Within.Assert)
}

/** The grade wheel dialog opened for an evaluation or for the form's grade chips. */
private fun StepBuilder.waitGradeDialog() {
	waitVisible(EvaluationsUiTags.EvaluationDialogTitle, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationDialogSubtitle, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationDialogSubjectCodeChip, Within.Assert)
}

/** Picks day 15 of the month before the one the date dialog opens on, and accepts. */
private fun StepBuilder.pickPreviousMonthDay() {
	tap(EvaluationsUiTags.EvaluationDateSelectButton)
	waitVisible(EvaluationsUiTags.EvaluationCalendarContainer, Within.Action)
	tap(EvaluationsUiTags.EvaluationCalendarPreviousMonthButton)
	tap(EvaluationsUiTags.calendarDayCell(E2eInputs.CalendarDay))
	tap(EvaluationsUiTags.EvaluationDateDialogAcceptButton)
}

/**
 * The list that cannot load offers a retry; once the service answers again, the retry shows the list. The sync that
 * opens the session already asks for the evaluations (it revalidates them, even if their tab was never opened), so
 * the list is refused from the very start: putting the mock in `Unavailable` after the summary would be too late,
 * the revalidation would have downloaded the list and the tab would open with it cached. The revalidation fails
 * without failing the sync, and opening the tab, which is never held off by it, meets the same refusal.
 */
private val evaluationsListRetry = scenario(
	"evaluations-list-retry",
	"evaluations",
	Start.Seeded(
		E2eAccounts.Canonical,
		mockStates = listOf(MockState("evaluations-list-failure", "Unavailable"))
	).toLaunchSpec()
) {
	covers("evaluations.Evaluations.RefreshEvaluations")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(BaseUiTags.ErrorViewRetryButton, Within.Long)
	mockState("evaluations-list-failure", "Recovered")
	tap(BaseUiTags.ErrorViewRetryButton)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Wait)
	waitGone(BaseUiTags.ErrorViewRetryButton, Within.Assert)
}

private val evaluationsSmoke = scenario(
	"evaluations-smoke",
	"evaluations",
	seeded(E2eAccounts.Canonical)
) {
	covers("evaluations.Evaluations.AddEvaluation")
	account(E2eAccounts.Canonical.id)
	tags("smoke")

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	waitVisible(EvaluationsUiTags.EvaluationsWeekStrip, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsWeekLabel, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsList, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationStatusChip, Within.Assert)
	tap(EvaluationsUiTags.EvaluationsAddFab)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Wait)
	waitVisible(EvaluationsUiTags.EvaluationSubjectPickerRow, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationTypePickerRow, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

private val evaluationsFiltersAndForm = scenario(
	"evaluations-filters-and-form",
	"evaluations",
	seeded(E2eAccounts.Canonical)
) {
	covers(
		"evaluations.Evaluations.SelectWeek",
		"evaluations.Evaluation.SetAttempt",
		"evaluations.Evaluation.SetType",
		"evaluations.Evaluation.SetDate",
		"evaluations.Evaluation.SetGrade",
		"evaluations.Evaluation.SetMaxGrade",
		"evaluations.Evaluation.ClickGrade",
		"evaluations.Evaluation.ClickMaxGrade"
	)
	account(E2eAccounts.Canonical.id)

	val lastWeek = E2eInputs.LastAcademicWeek

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	waitVisible(EvaluationsUiTags.EvaluationsWeekStrip, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsList, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationStatusChip, Within.Assert)
	// The frozen clock is past the term's last week, which is the one the strip opens on; picking the week before it
	// moves the strip there and the last one leaves.
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(lastWeek), Within.Assert)
	tap(EvaluationsUiTags.evaluationsWeekChip(lastWeek - 1))
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(lastWeek - 1), Within.Action)
	waitGone(EvaluationsUiTags.evaluationsWeekPage(lastWeek), Within.Action)
	openAddForm()
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	waitVisible(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value), Within.Assert)
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	waitVisible(EvaluationsUiTags.EvaluationSubjectPickerRow, Within.Assert)
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	tap(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeTest))
	waitVisible(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeTest), Within.Assert)
	tap(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeTest))
	waitVisible(EvaluationsUiTags.EvaluationTypePickerRow, Within.Assert)
	tap(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeTest))
	tap(EvaluationsUiTags.EvaluationDateSelectButton)
	waitVisible(EvaluationsUiTags.EvaluationDateDialogTitle, Within.Action)
	tap(EvaluationsUiTags.EvaluationDateDialogCancelButton)
	pickPreviousMonthDay()
	waitVisible(EvaluationsUiTags.EvaluationDatePicker, Within.Assert)
	waitGone(EvaluationsUiTags.EvaluationGradeChip, Within.Assert)
	tap(EvaluationsUiTags.EvaluationMaxGradeChip)
	waitGradeDialog()
	tap(EvaluationsUiTags.EvaluationDialogConfirmButton)
	waitVisible(EvaluationsUiTags.EvaluationGradeChip, Within.Action)
	tap(EvaluationsUiTags.EvaluationGradeChip)
	waitGradeDialog()
	waitVisible(EvaluationsUiTags.EvaluationGradeWheelPicker, Within.Assert)
	tap(EvaluationsUiTags.EvaluationDialogConfirmButton)
	tap(EvaluationsUiTags.EvaluationDateNoDateButton)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(EvaluationsUiTags.DiscardEvaluationMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(EvaluationsUiTags.DiscardEvaluationMessage, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Assert)
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(EvaluationsUiTags.DiscardEvaluationMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

private val evaluationsGradeFromList = scenario(
	"evaluations-grade-from-list",
	"evaluations",
	seeded(E2eAccounts.Canonical)
) {
	covers(
		"evaluations.Evaluations.ShowEvaluationGradeDialog",
		"evaluations.Evaluations.SetEvaluationGrade"
	)
	account(E2eAccounts.Canonical.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value),
		Scroll.ContentDown,
		Within.Wait
	)
	tap(EvaluationsUiTags.evaluationGradeActionButton(E2eFixtures.GradedEvaluation.value))
	waitGradeDialog()
	waitVisible(EvaluationsUiTags.EvaluationGradeWheelPicker, Within.Assert)
	tap(EvaluationsUiTags.EvaluationDialogDismissButton)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
	tap(EvaluationsUiTags.evaluationGradeActionButton(E2eFixtures.GradedEvaluation.value))
	waitGradeDialog()
	waitVisible(EvaluationsUiTags.EvaluationGradeWheelPicker, Within.Assert)
	tap(EvaluationsUiTags.EvaluationDialogConfirmButton)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Assert)
}

private val evaluationsEditSubmit = scenario(
	"evaluations-edit-submit",
	"evaluations",
	seeded(E2eAccounts.Canonical)
) {
	covers(
		"evaluations.Evaluations.EditEvaluation",
		"evaluations.Evaluation.SetType",
		"evaluations.Evaluation.ClickSubmitEvaluation"
	)
	account(E2eAccounts.Canonical.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	revealGradedEvaluationActions(EvaluationsUiTags.EvaluationSwipeEditAction)
	tap(EvaluationsUiTags.EvaluationSwipeEditAction)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.ClashingAttempt.value), Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationSubjectPickerRow, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationTypePickerRow, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationDatePicker, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationMaxGradeChip, Within.Assert)
	assertEnabled(EvaluationsUiTags.EvaluationDoneFab, false)
	tap(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeWrittenWork))
	tap(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeTest))
	assertEnabled(EvaluationsUiTags.EvaluationDoneFab, true)
	tap(EvaluationsUiTags.EvaluationDoneFab)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Wait)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
}

private val evaluationsSwipeDelete = scenario(
	"evaluations-swipe-delete",
	"evaluations",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("evaluations.Evaluations.RemoveEvaluation")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Long)
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value),
		Scroll.ContentDown,
		Within.Wait
	)
	tap(EvaluationsUiTags.evaluationItemCard(E2eFixtures.GradedEvaluation.value))
	waitVisible(EvaluationsUiTags.EvaluationSwipeDeleteAction, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationSwipeToDismissContainer, Within.Assert)
	tap(EvaluationsUiTags.EvaluationSwipeDeleteAction)
	waitVisible(EvaluationsUiTags.DeleteEvaluationMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Assert)
}

private val evaluationsAddSubmit = scenario(
	"evaluations-add-submit",
	"evaluations",
	seeded(E2eAccounts.Canonical)
) {
	covers(
		"evaluations.Evaluations.AddEvaluation",
		"evaluations.Evaluation.SetAttempt",
		"evaluations.Evaluation.SetType",
		"evaluations.Evaluation.SetDate",
		"evaluations.Evaluation.SetMaxGrade",
		"evaluations.Evaluation.SetGrade",
		"evaluations.Evaluation.ClickGrade",
		"evaluations.Evaluation.ClickMaxGrade",
		"evaluations.Evaluation.ClickSubmitEvaluation"
	)
	account(E2eAccounts.Canonical.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	openAddForm()
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	tap(EvaluationsUiTags.evaluationTypeChip(E2eInputs.EvaluationTypeTest))
	pickPreviousMonthDay()
	waitGone(EvaluationsUiTags.EvaluationGradeChip, Within.Assert)
	tap(EvaluationsUiTags.EvaluationMaxGradeChip)
	waitGradeDialog()
	waitVisible(EvaluationsUiTags.EvaluationGradeWheelPicker, Within.Assert)
	tap(EvaluationsUiTags.EvaluationDialogConfirmButton)
	waitVisible(EvaluationsUiTags.EvaluationGradeChip, Within.Action)
	tap(EvaluationsUiTags.EvaluationGradeChip)
	waitGradeDialog()
	tap(EvaluationsUiTags.EvaluationDialogConfirmButton)
	tap(EvaluationsUiTags.EvaluationDoneFab)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Wait)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
}

private val evaluationsAddValidation = scenario(
	"evaluations-add-validation",
	"evaluations",
	seeded(E2eAccounts.Canonical)
) {
	covers(
		"evaluations.Evaluations.AddEvaluation",
		"evaluations.Evaluation.ClickSubmitEvaluation",
		"evaluations.Evaluation.SetAttempt"
	)
	account(E2eAccounts.Canonical.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	openAddForm()
	tap(EvaluationsUiTags.EvaluationDoneFab)
	waitVisible(EvaluationsUiTags.EvaluationSubjectRequiredError, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationTypeRequiredError, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationMaxGradeRequiredError, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Assert)
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	waitGone(EvaluationsUiTags.EvaluationSubjectRequiredError, Within.Action)
}

private val evaluationsEnrollmentUnavailable = scenario(
	"evaluations-enrollment-unavailable",
	"evaluations",
	seeded(E2eAccounts.EvaluationsEnrollmentUnavailable)
) {
	covers("evaluations.Evaluations.EnsureEvaluationsLoaded")
	account(E2eAccounts.EvaluationsEnrollmentUnavailable.id)

	openEvaluations(BaseUiTags.EmptyViewContainer)
	waitVisible(BaseUiTags.EmptyStateAnimation, Within.Assert)
	waitVisible(text(Copy.EvaluationsEnrollmentUnavailableTitle), Within.Assert)
	waitVisible(text(Copy.EvaluationsEnrollmentUnavailableMessage), Within.Assert)
	waitGone(BaseUiTags.EmptyViewActionButton, Within.Assert)
}

private val evaluationsAnnulledNoAttempts = scenario(
	"evaluations-annulled-no-attempts",
	"evaluations",
	seeded(E2eAccounts.AnnulledFinal)
) {
	covers("evaluations.Evaluations.EnsureEvaluationsLoaded")
	account(E2eAccounts.AnnulledFinal.id)

	openEvaluations(BaseUiTags.EmptyViewContainer)
	waitVisible(BaseUiTags.EmptyStateAnimation, Within.Assert)
	// The record explains the final annulment; here it only reads as a term that is not there.
	waitVisible(text(Copy.EvaluationsNoSubjectsTitle), Within.Assert)
	waitGone(text(Copy.EnrollmentAnnulledFinalTitle), Within.Assert)
	waitGone(BaseUiTags.EmptyViewActionButton, Within.Assert)
}

private val evaluationsNotEnrolled = scenario(
	"evaluations-not-enrolled",
	"evaluations",
	seeded(E2eAccounts.NotEnrolled)
) {
	covers("evaluations.Evaluations.EnsureEvaluationsLoaded")
	account(E2eAccounts.NotEnrolled.id)

	openEvaluations(BaseUiTags.EmptyViewContainer)
	waitVisible(BaseUiTags.EmptyStateAnimation, Within.Assert)
	waitVisible(text(Copy.EvaluationsNotEnrolledTitle), Within.Assert)
	waitGone(BaseUiTags.EmptyViewActionButton, Within.Assert)
}

private val evaluationsAnnulledProvisionalNotice = scenario(
	"evaluations-annulled-provisional-notice",
	"evaluations",
	seeded(E2eAccounts.AnnulledProvisional)
) {
	covers("evaluations.Evaluations.EnsureEvaluationsLoaded")
	account(E2eAccounts.AnnulledProvisional.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	// During the corrections window the term is still there: the evaluations stay usable and look as
	// they would without the annulment. The record is the one that explains it, not this screen.
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsWeekStrip, Within.Assert)
	waitGone(BaseUiTags.NoticeView, Within.Assert)
	waitGone(text(Copy.EnrollmentAnnulledProvisionalTitle), Within.Assert)
}

// An evaluation inherits where its subject has class that day: the room on a line of its own, and the blocks with the
// hours under them, in twelve hours; one on a day without class says nothing of the sort.
// The week of the clock is the one the strip opens on.
private val evaluationsInheritedPlace = scenario(
	"evaluations-inherited-place",
	"evaluations",
	seeded(E2eAccounts.SummaryHub)
) {
	account(E2eAccounts.SummaryHub.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek3), Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsWeekCurrentIndicator, Within.Assert)
	// Monday: overdue and ungraded, and CI5311 meets that day.
	waitVisible(EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationOverdue.value), Within.Assert)
	waitVisible(EvaluationsUiTags.evaluationPlace(E2eFixtures.HubEvaluationOverdue.value), Within.Assert)
	waitVisible(text(Copy.RoomMys116), Within.Assert)
	waitVisible(text(Copy.ScheduleBlocks12), Within.Assert)
	waitVisible(text(Copy.EvaluationNoGrade), Within.Assert)
	// Today: CI5437 meets at blocks 3 and 4.
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationToday.value),
		Scroll.ContentDown,
		Within.Wait
	)
	waitVisible(EvaluationsUiTags.evaluationPlace(E2eFixtures.HubEvaluationToday.value), Within.Assert)
	waitVisible(text(Copy.RoomMys210), Within.Assert)
	waitVisible(text(Copy.ScheduleBlocks34), Within.Assert)
	// Friday: EG1114 does not meet, so the row has no place line at all.
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationNoClass.value),
		Scroll.ContentDown,
		Within.Wait
	)
	waitGone(EvaluationsUiTags.evaluationPlace(E2eFixtures.HubEvaluationNoClass.value), Within.Assert)
}

// Editing a saved evaluation does not let its subject change, and the date picker marks the days the subject has class
// and says where it meets when one is chosen: Thursday the 22nd has class of CI5437, Wednesday the 21st does not.
private val evaluationsEditKeepsSubject = scenario(
	"evaluations-edit-keeps-subject",
	"evaluations",
	seeded(E2eAccounts.SummaryHub)
) {
	account(E2eAccounts.SummaryHub.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	scrollUntilVisible(
		EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationToday.value),
		Scroll.ContentDown,
		Within.Wait
	)
	tap(EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationToday.value))
	waitVisible(EvaluationsUiTags.EvaluationSwipeEditAction, Within.Action)
	tap(EvaluationsUiTags.EvaluationSwipeEditAction)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.HubAttemptCi5437.value), Within.Assert)
	assertEnabled(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.HubAttemptCi5437.value), false)
	tap(EvaluationsUiTags.EvaluationDateSelectButton)
	waitVisible(EvaluationsUiTags.EvaluationCalendarContainer, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationDateDialogSubjectChip, Within.Assert)
	waitVisible(EvaluationsUiTags.calendarDayClassDot(E2eInputs.HubClassDay), Within.Assert)
	waitGone(EvaluationsUiTags.calendarDayClassDot(E2eInputs.HubNoClassDay), Within.Assert)
	// The saved date, Thursday the 15th, has class, so its line is up before anything is touched: Wednesday goes first
	// and must take the line away, then Thursday must bring it back.
	tap(EvaluationsUiTags.calendarDayCell(E2eInputs.HubNoClassDay))
	waitGone(text(Copy.InClassBlocks34), Within.Action)
	waitGone(EvaluationsUiTags.EvaluationDateDialogSelectionPlace, Within.Action)
	waitVisible(EvaluationsUiTags.EvaluationDateDialogSelection, Within.Assert)
	tap(EvaluationsUiTags.calendarDayCell(E2eInputs.HubClassDay))
	waitVisible(EvaluationsUiTags.EvaluationDateDialogSelectionPlace, Within.Action)
	waitVisible(text(Copy.InClassBlocks34), Within.Assert)
	tap(EvaluationsUiTags.EvaluationDateDialogCancelButton)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Action)
}

// The bar of Evaluations opens the Schedule sheet over the tab, which stays the selected one.
private val evaluationsOpenSchedule = scenario(
	"evaluations-open-schedule",
	"evaluations",
	seeded(E2eAccounts.SummaryHub)
) {
	account(E2eAccounts.SummaryHub.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	tap(BaseUiTags.topBarActionButton(TopBarAction.EvaluationsScheduleAction))
	waitVisible(RecordUiTags.ScheduleSheet, Within.Wait)
	waitVisible(RecordUiTags.ScheduleTitle, Within.Assert)
	waitVisible(text(Copy.ScheduleSubtitleWeek3), Within.Assert)
	closeScheduleSheet()
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

/** The scenarios of this module; list every new one here. */
val evaluationsScenarios: List<Scenario> = listOf(
	evaluationsSmoke,
	evaluationsListRetry,
	evaluationsFiltersAndForm,
	evaluationsGradeFromList,
	evaluationsEditSubmit,
	evaluationsSwipeDelete,
	evaluationsAddSubmit,
	evaluationsAddValidation,
	evaluationsEnrollmentUnavailable,
	evaluationsAnnulledNoAttempts,
	evaluationsNotEnrolled,
	evaluationsAnnulledProvisionalNotice,
	evaluationsInheritedPlace,
	evaluationsEditKeepsSubject,
	evaluationsOpenSchedule
)
