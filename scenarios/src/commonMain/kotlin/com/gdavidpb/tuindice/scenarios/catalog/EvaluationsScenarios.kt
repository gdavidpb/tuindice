package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags

private const val CALENDAR_DAY = 15

private fun seeded(account: E2eAccount) = Start.Seeded(account).toLaunchSpec()

/** From the seeded summary to the evaluations tab, waiting for [content] the way each flow did. */
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
	tap(EvaluationsUiTags.calendarDayCell(CALENDAR_DAY))
	tap(EvaluationsUiTags.EvaluationDateDialogAcceptButton)
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
		"evaluations.Evaluation.SetAttempt",
		"evaluations.Evaluation.SetType",
		"evaluations.Evaluation.SetDate",
		"evaluations.Evaluation.SetGrade",
		"evaluations.Evaluation.SetMaxGrade",
		"evaluations.Evaluation.ClickGrade",
		"evaluations.Evaluation.ClickMaxGrade"
	)
	account(E2eAccounts.Canonical.id)

	openEvaluations(EvaluationsUiTags.EvaluationsContentContainer)
	waitVisible(EvaluationsUiTags.EvaluationsWeekStrip, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationsList, Within.Assert)
	waitVisible(EvaluationsUiTags.EvaluationStatusChip, Within.Assert)
	openAddForm()
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	waitVisible(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value), Within.Assert)
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	waitVisible(EvaluationsUiTags.EvaluationSubjectPickerRow, Within.Assert)
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	tap(EvaluationsUiTags.evaluationTypeChip("test"))
	waitVisible(EvaluationsUiTags.evaluationTypeChip("test"), Within.Assert)
	tap(EvaluationsUiTags.evaluationTypeChip("test"))
	waitVisible(EvaluationsUiTags.EvaluationTypePickerRow, Within.Assert)
	tap(EvaluationsUiTags.evaluationTypeChip("test"))
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
	tap(EvaluationsUiTags.evaluationTypeChip("written_work"))
	tap(EvaluationsUiTags.evaluationTypeChip("test"))
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
	tap(EvaluationsUiTags.evaluationTypeChip("test"))
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

/** The scenarios of this module; list every new one here. */
val evaluationsScenarios: List<Scenario> = listOf(
	evaluationsSmoke,
	evaluationsFiltersAndForm,
	evaluationsGradeFromList,
	evaluationsEditSubmit,
	evaluationsSwipeDelete,
	evaluationsAddSubmit,
	evaluationsAddValidation,
	evaluationsEnrollmentUnavailable,
	evaluationsAnnulledNoAttempts,
	evaluationsNotEnrolled,
	evaluationsAnnulledProvisionalNotice
)
