package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.scrollUntilVisible
import com.gdavidpb.tuindice.scenariokit.dsl.swipeFrom
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tag
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.tapAtScreen
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitBackgrounded
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.MockState
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenariokit.model.Scroll
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.E2eInputs
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.ShareSheet
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.closeScheduleSheet
import com.gdavidpb.tuindice.scenarios.shared.returnToSummaryTab
import com.gdavidpb.tuindice.scenarios.shared.waitSummaryHub
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsTooltipUiTags
import com.gdavidpb.tuindice.summary.ui.share.ShareTermUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.time.Duration.Companion.milliseconds

private const val IOS_SHEET_SWIPE_MS = 600L

/**
 * Identifiers of the iOS system surfaces: the photo picker's "Cancelar" button and its "Ordenar y filtrar" button (only
 * there once the library has loaded), the camera's "Dismiss" button and its shutter.
 */
private const val IOS_PICKER_CANCEL = "Cancel"
private const val IOS_PICKER_LOADED = "Sort and Filter"
private const val IOS_CAMERA_DISMISS = "DismissButton"
private const val IOS_CAMERA_LOADED = "PhotoCapture"

private val summarySmoke = scenario(
	"summary-smoke",
	"summary",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	tags("smoke")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	waitVisible(SummaryUiTags.GradeText, Within.Assert)
	waitVisible(SummaryUiTags.ItemsList, Within.Assert)
	waitVisible(SummaryUiTags.StatusRow, Within.Assert)
	waitVisible(SummaryUiTags.StatusText, Within.Assert)
	// The term is in progress, with no class and no evaluation this week, and all its evaluations have gone by.
	// Its card says nothing is pending once the sync has revalidated the evaluations. Until then, it says where
	// to look for them.
	scrollUntilVisible(SummaryUiTags.WeekCard, Scroll.ContentDown, Within.Long)
	waitVisible(text(Copy.SummaryWeekIdleTitle), Within.Long)
	// The hub always has the career, whatever the term: the progress of the pensum or the invitation to open it.
	scrollUntilVisible(SummaryUiTags.CareerSection, Scroll.ContentDown, Within.Long)
	waitVisible(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection), Within.Assert)
}

private val summaryRefreshRetry = scenario(
	"summary-refresh-retry",
	"summary",
	Start.Seeded(E2eAccounts.SummaryRefreshRetry).toLaunchSpec()
) {
	covers("summary.Summary.RefreshSummary")
	account(E2eAccounts.SummaryRefreshRetry.id)

	waitVisible(BaseUiTags.ErrorViewContainer, Within.Sync)
	tap(BaseUiTags.ErrorViewRetryButton)
	waitVisible(SummaryUiTags.ContentContainer, Within.Long)
	waitVisible(SummaryUiTags.StatusRow, Within.Assert)
	waitVisible(SummaryUiTags.ItemsList, Within.Assert)
}

private val summaryStatusDialog = scenario(
	"summary-status-dialog",
	"summary",
	Start.Seeded(E2eAccounts.SummaryStatusUnavailable).toLaunchSpec()
) {
	account(E2eAccounts.SummaryStatusUnavailable.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(SummaryUiTags.StatusIconButton)
	waitVisible(SummaryUiTags.SyncStatusMessage, Within.Action)
	// The sync of this account answers that the record service is unavailable, and the dialog says so by name.
	waitVisible(text(Copy.RecordUnavailableSyncMessage), Within.Assert)
	waitVisible(BaseUiTags.ConfirmationDialogPositiveButton, Within.Assert)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitGone(SummaryUiTags.SyncStatusMessage, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Assert)
}

private val summaryPartialEnrollmentStatusDialog = scenario(
	"summary-partial-enrollment-status-dialog",
	"summary",
	Start.Seeded(E2eAccounts.EnrollmentUnavailable).toLaunchSpec()
) {
	account(E2eAccounts.EnrollmentUnavailable.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitVisible(SummaryUiTags.StatusIconButton, Within.Action)
	tap(SummaryUiTags.StatusIconButton)
	waitVisible(SummaryUiTags.SyncStatusMessage, Within.Action)
	waitVisible(text(Copy.EnrollmentUnavailableSyncMessage), Within.Assert)
	waitVisible(BaseUiTags.ConfirmationDialogPositiveButton, Within.Assert)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitGone(SummaryUiTags.SyncStatusMessage, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Assert)
}

private val summaryOutdatedCredentials = scenario(
	"summary-outdated-credentials",
	"summary",
	Start.Seeded(E2eAccounts.SummaryOutdatedCredentials).toLaunchSpec()
) {
	covers("maincore.Main.DismissUpdatePassword")
	account(E2eAccounts.SummaryOutdatedCredentials.id)

	// The sync of this account answers with outdated credentials, and the host asks for the password at once.
	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Long)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(AuthUiTags.UpdatePasswordIdleContainer, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	waitVisible(SummaryUiTags.StatusIconButton, Within.Wait)
	tap(SummaryUiTags.StatusIconButton)
	waitVisible(SummaryUiTags.SyncStatusMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Wait)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}

// Signing in always leaves a profile (name and career), so Summary shows its content. What the university does
// not have yet is the record: Record says so, and Evaluations only reads as having no current term.
private val summaryNewStudentNoRecord = scenario(
	"summary-new-student-no-record",
	"summary",
	Start.Seeded(E2eAccounts.NewStudent).toLaunchSpec()
) {
	account(E2eAccounts.NewStudent.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(BaseUiTags.ErrorViewContainer, Within.Long)
	waitVisible(text(Copy.NewStudentNoRecordTitle), Within.Assert)
	waitVisible(BaseUiTags.ErrorViewRetryButton, Within.Assert)
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(BaseUiTags.ErrorViewContainer, Within.Long)
	waitVisible(text(Copy.EvaluationsNoSubjectsTitle), Within.Assert)
	waitGone(text(Copy.NewStudentNoRecordTitle), Within.Assert)
	waitGone(BaseUiTags.ErrorViewRetryButton, Within.Assert)
	// Back on Summary, once the sync has answered (Record and Evaluations just read it): a missing record is
	// not a sync problem, so the sync row asks for nothing and its icon opens no details.
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
	assertEnabled(SummaryUiTags.StatusIconButton, false)
	tap(tag(SummaryUiTags.StatusIconButton), requireEnabled = false)
	waitGone(SummaryUiTags.SyncStatusMessage, Within.Assert)
	waitVisible(SummaryUiTags.ContentContainer, Within.Assert)
	// The profile loaded, so the hub is on the content: a new student the university has no record for yet has no
	// week and no term to show, and the term names the first one to come.
	scrollUntilVisible(SummaryUiTags.WeekCard, Scroll.ContentDown, Within.Long)
	waitVisible(text(Copy.SummaryNoTermWeekTitle), Within.Long)
	scrollUntilVisible(SummaryUiTags.TermCard, Scroll.ContentDown, Within.Long)
	waitVisible(text(Copy.SummaryFirstTermTitle), Within.Long)
}

// The account has data from before; the university just refuses the record today. The content stays and the
// sync row says why, in its own words and not as a generic outage.
private val summaryRecordAccessDenied = scenario(
	"summary-record-access-denied",
	"summary",
	Start.Seeded(E2eAccounts.RecordDenied).toLaunchSpec()
) {
	account(E2eAccounts.RecordDenied.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	waitVisible(SummaryUiTags.StatusIconButton, Within.Action)
	tap(SummaryUiTags.StatusIconButton)
	waitVisible(SummaryUiTags.SyncStatusMessage, Within.Action)
	waitVisible(text(Copy.RecordAccessDeniedTitle), Within.Assert)
	waitVisible(text(Copy.RecordAccessDeniedMessage), Within.Assert)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitGone(SummaryUiTags.SyncStatusMessage, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Assert)
}

private val summaryProfilePicture = scenario(
	"summary-profile-picture",
	"summary",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers(
		"summary.Summary.OpenProfilePictureSettings",
		"summary.Summary.RemoveProfilePicture",
		"summary.Summary.ConfirmRemoveProfilePicture"
	)
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	waitVisible(SummaryUiTags.ProfilePictureContainer, Within.Assert)
	waitVisible(SummaryUiTags.ProfilePictureEditButton, Within.Assert)
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(SummaryUiTags.ProfilePicturePickAction, Within.Action)
	waitVisible(SummaryUiTags.ProfilePictureTakeAction, Within.Assert)
	waitVisible(SummaryUiTags.ProfilePictureRemoveAction, Within.Assert)
	tap(SummaryUiTags.ProfilePictureRemoveAction)
	waitVisible(SummaryUiTags.RemoveProfilePictureMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(SummaryUiTags.ProfilePictureRemoveAction, Within.Action)
	tap(SummaryUiTags.ProfilePictureRemoveAction)
	waitVisible(SummaryUiTags.RemoveProfilePictureMessage, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(SummaryUiTags.ProfilePictureContainer, Within.Assert)
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(SummaryUiTags.ProfilePicturePickAction, Within.Action)
	waitGone(SummaryUiTags.ProfilePictureRemoveAction, Within.Assert)
	onPlatform(Platform.Android) {
		back()
	}
	onPlatform(Platform.Ios) {
		swipeScreen(SwipeDirection.Down, IOS_SHEET_SWIPE_MS.milliseconds)
	}
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}

/**
 * Opens the system surface of a picture source and leaves it without choosing: Android's gallery and camera are other
 * apps (the app leaves the foreground, the back key closes them); iOS shows its photo picker and camera over the app,
 * each with a control of its own that closes it.
 */
private fun StepBuilder.openPictureSourceAndCancel(source: String, iosReady: String, iosCancel: String) {
	tap(SummaryUiTags.ProfilePictureEditButton)
	waitVisible(source, Within.Action)
	tap(source)
	onPlatform(Platform.Android) {
		waitBackgrounded()
		back()
	}
	onPlatform(Platform.Ios) {
		// The surface shows its close control while it still loads, and a tap on it then is lost: wait for a
		// control that only a loaded one has.
		waitVisible(system(iosReady), Within.Action)
		tap(system(iosCancel))
		// The app's own elements stay in the tree under a system surface: only its going proves it closed.
		waitGone(system(iosCancel), Within.Action)
	}
	waitVisible(SummaryUiTags.ProfilePictureContainer, Within.Action)
	waitGone(SummaryUiTags.ProfilePicturePickAction, Within.Assert)
}

private val summaryProfilePictureSources = scenario(
	"summary-profile-picture-sources",
	"summary",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("summary.Summary.PickProfilePicture", "summary.Summary.TakeProfilePicture")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	openPictureSourceAndCancel(SummaryUiTags.ProfilePicturePickAction, IOS_PICKER_LOADED, IOS_PICKER_CANCEL)
	openPictureSourceAndCancel(SummaryUiTags.ProfilePictureTakeAction, IOS_CAMERA_LOADED, IOS_CAMERA_DISMISS)
}

/** Back at the top of the Summary tab, whatever the scroll it was left with: the profile is the first thing on it. */
private fun StepBuilder.scrollSummaryToTop() {
	scrollUntilVisible(SummaryUiTags.ProfilePictureContainer, Scroll.ContentUp, Within.Action)
}

private fun hubStart() = Start.Seeded(E2eAccounts.SummaryHub).toLaunchSpec()

// The term the frozen clock is in, with classes today and evaluations this week (`testkit/e2e/fixture-contract.md`,
// the hub accounts). The hub reads what the sync and the evaluations revalidation downloaded and asks for nothing.
private val summaryHubOverview = scenario("summary-hub-overview", "summary", hubStart()) {
	account(E2eAccounts.SummaryHub.id)

	val ci5437 = E2eFixtures.SubjectCi5437.value
	val today = E2eFixtures.HubEvaluationToday.value

	waitSummaryHub()
	// What needs attention: the ungraded evaluations (one line for all) and the enrollment error of a subject.
	waitVisible(SummaryUiTags.attentionItem(E2eInputs.SummaryAttentionUngradedKey), Within.Assert)
	waitVisible(SummaryUiTags.attentionItem(E2eInputs.summaryAttentionEnrollmentKey(ci5437)), Within.Assert)
	// This week: a class today (in progress or the next, whatever the zone of the device) and the evaluations that
	// have not gone by; the ones whose day went by are in the attention and not here.
	scrollUntilVisible(SummaryUiTags.WeekClassRow, Scroll.ContentDown, Within.Wait)
	waitVisible(SummaryUiTags.WeekClassRow, Within.Assert)
	scrollUntilVisible(SummaryUiTags.weekEvaluationRow(today), Scroll.ContentDown, Within.Wait)
	waitVisible(SummaryUiTags.weekEvaluationRow(E2eFixtures.HubEvaluationNoClass.value), Within.Assert)
	waitGone(SummaryUiTags.weekEvaluationRow(E2eFixtures.HubEvaluationOverdue.value), Within.Assert)
	waitGone(SummaryUiTags.weekEvaluationRow(E2eFixtures.HubEvaluationOldest.value), Within.Assert)
	// The term: which week of the twelve it is and what is left of them, the subjects with their credits, and the
		// points of each subject.
	scrollUntilVisible(SummaryUiTags.TermWeekText, Scroll.ContentDown, Within.Wait)
	waitVisible(text(Copy.SummaryTermWeek3, contains = true), Within.Assert)
		waitVisible(text(Copy.SummaryTermWeeksLeft), Within.Assert)
	waitVisible(SummaryUiTags.TermWeekTrack, Within.Assert)
		waitVisible(text(Copy.SummaryTermSubjects), Within.Assert)
	listOf(
		E2eFixtures.SubjectCi4325.value,
		E2eFixtures.SubjectCi5311.value,
		ci5437,
		E2eFixtures.SubjectEg1114.value
	).forEach { code ->
		scrollUntilVisible(SummaryUiTags.termPointsRow(code), Scroll.ContentDown, Within.Wait)
	}
	// The career needs the pensum, which the app downloads only when its tab is opened: the tab is opened here once.
	tap(MaincoreUiTags.TuIndiceBottomBarPensumItem)
	waitVisible(PensumUiTags.PensumScreen, Within.Long)
	// The first download of the pensum follows the screen, so its content gets the long wait.
	waitVisible(PensumUiTags.Canvas, Within.Long)
	returnToSummaryTab()
	// The career: the pensum stays saved once its tab has been opened, so the card shows its progress and not the
	// invitation.
	scrollUntilVisible(SummaryUiTags.CareerCard, Scroll.ContentDown, Within.Long)
	waitVisible(SummaryUiTags.CareerProgressBar, Within.Assert)
	waitVisible(SummaryUiTags.CareerPercentText, Within.Assert)
	waitGone(SummaryUiTags.CareerInviteCard, Within.Assert)
}

// The points of attention lead to the screen that owns what they say: the ungraded evaluations to the week of the
// oldest of them (week 2, not the week of the clock), the enrollment error to the attempt it names in Record.
private val summaryHubAttentionLinks = scenario("summary-hub-attention-links", "summary", hubStart()) {
	covers(
		"summary.Summary.OpenTarget",
		"evaluations.Evaluations.EvaluationArrivalTaken",
		"record.Record.AttemptArrivalTaken"
	)
	account(E2eAccounts.SummaryHub.id)

	waitSummaryHub()
	tap(SummaryUiTags.attentionItem(E2eInputs.SummaryAttentionUngradedKey))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek2), Within.Long)
	waitVisible(EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationOldest.value), Within.Assert)
	waitGone(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek3), Within.Assert)
	returnToSummaryTab()
	scrollSummaryToTop()
	tap(SummaryUiTags.attentionItem(E2eInputs.summaryAttentionEnrollmentKey(E2eFixtures.SubjectCi5437.value)))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItemSelected, Within.Wait)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	waitVisible(RecordUiTags.attemptCard(E2eFixtures.HubAttemptCi5437.value), Within.Assert)
}

// This week: its link and the class row open the Schedule sheet over Summary (the tab does not change); an evaluation
// of the week leads to that week of Evaluations.
private val summaryHubWeekLinks = scenario("summary-hub-week-links", "summary", hubStart()) {
	covers("summary.Summary.OpenTarget", "evaluations.Evaluations.EvaluationArrivalTaken")
	account(E2eAccounts.SummaryHub.id)

	val today = E2eFixtures.HubEvaluationToday.value

	waitSummaryHub()
	scrollUntilVisible(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection), Scroll.ContentDown, Within.Wait)
	tap(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection))
	waitVisible(RecordUiTags.ScheduleSheet, Within.Wait)
	waitVisible(RecordUiTags.ScheduleTitle, Within.Assert)
	waitVisible(text(Copy.ScheduleSubtitleWeek3), Within.Assert)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarSummaryItemSelected, Within.Assert)
	closeScheduleSheet()
	scrollUntilVisible(SummaryUiTags.WeekClassRow, Scroll.ContentDown, Within.Wait)
	tap(SummaryUiTags.WeekClassRow)
	waitVisible(RecordUiTags.ScheduleSheet, Within.Wait)
	waitVisible(RecordUiTags.ScheduleTitle, Within.Assert)
	closeScheduleSheet()
	scrollUntilVisible(SummaryUiTags.weekEvaluationRow(today), Scroll.ContentDown, Within.Wait)
	tap(SummaryUiTags.weekEvaluationRow(today))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek3), Within.Long)
	waitVisible(EvaluationsUiTags.evaluationItemCard(today), Within.Assert)
	returnToSummaryTab()
}

// The term's link opens Record, a subject's row opens that subject's attempt on Record, and the career's link opens
// Pensum, as does its progress card, each on its own tab.
private val summaryHubTermCareerLinks = scenario("summary-hub-term-career-links", "summary", hubStart()) {
	covers("summary.Summary.OpenTarget", "record.Record.AttemptArrivalTaken")
	account(E2eAccounts.SummaryHub.id)

	val ci5437 = E2eFixtures.SubjectCi5437.value

	waitSummaryHub()
	scrollUntilVisible(SummaryUiTags.termPointsRow(ci5437), Scroll.ContentDown, Within.Wait)
	tap(SummaryUiTags.termPointsRow(ci5437))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItemSelected, Within.Wait)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	waitVisible(RecordUiTags.attemptCard(E2eFixtures.HubAttemptCi5437.value), Within.Assert)
	returnToSummaryTab()
	scrollUntilVisible(SummaryUiTags.sectionLink(SummaryUiTags.TermSection), Scroll.ContentDown, Within.Wait)
	tap(SummaryUiTags.sectionLink(SummaryUiTags.TermSection))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarRecordItemSelected, Within.Wait)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	returnToSummaryTab()
	scrollSummaryToTop()
	scrollUntilVisible(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection), Scroll.ContentDown, Within.Wait)
	tap(SummaryUiTags.sectionLink(SummaryUiTags.CareerSection))
	waitVisible(MaincoreUiTags.TuIndiceBottomBarPensumItemSelected, Within.Wait)
	waitVisible(PensumUiTags.PensumScreen, Within.Long)
	// The pensum is cached once its tab has been opened: the card then shows the progress, which leads to Pensum.
	waitVisible(PensumUiTags.Canvas, Within.Long)
	returnToSummaryTab()
	scrollUntilVisible(SummaryUiTags.CareerCard, Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.CareerCard)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarPensumItemSelected, Within.Wait)
	waitVisible(PensumUiTags.PensumScreen, Within.Long)
}

// A pensum that was never downloaded (the service answers 404 for this account) leaves the career as an invitation,
// and the whole card leads to Pensum.
private val summaryHubCareerInvite = scenario(
	"summary-hub-career-invite",
	"summary",
	Start.Seeded(E2eAccounts.SummaryHubNoPensum).toLaunchSpec()
) {
	covers("summary.Summary.OpenTarget")
	account(E2eAccounts.SummaryHubNoPensum.id)

	waitSummaryHub()
	scrollUntilVisible(SummaryUiTags.CareerInviteCard, Scroll.ContentDown, Within.Long)
	waitGone(SummaryUiTags.CareerCard, Within.Assert)
	tap(SummaryUiTags.CareerInviteCard)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarPensumItemSelected, Within.Wait)
	waitVisible(BaseUiTags.EmptyViewContainer, Within.Long)
}

/** From the hub, opens the Statistics screen through the card of its section, the way a student does. */
private fun StepBuilder.openStatistics() {
	scrollUntilVisible(SummaryUiTags.StatisticsCard, Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.StatisticsCard)
	waitVisible(SummaryUiTags.StatisticsContent, Within.Wait)
}

// The card of the statistics under the career opens the Statistics screen: its six blocks hold the figures in order.
private val summaryStatisticsOverview = scenario("summary-statistics-overview", "summary", hubStart()) {
	covers("summary.Summary.OpenTarget")
	account(E2eAccounts.SummaryHub.id)

	waitSummaryHub()
	openStatistics()
	listOf(
		SummaryUiTags.StatisticsCurrentTermBlock,
		SummaryUiTags.StatisticsIndexBlock,
		SummaryUiTags.StatisticsSubjectsBlock,
		SummaryUiTags.StatisticsCreditsBlock,
		SummaryUiTags.StatisticsGradesBlock,
		SummaryUiTags.StatisticsPaceBlock
	).forEach { block ->
		scrollUntilVisible(block, Scroll.ContentDown, Within.Long)
		waitVisible(block, Within.Assert)
	}
	// The screen has no bottom bar: its top bar's back button takes the student back to the hub.
	tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	waitVisible(SummaryUiTags.ContentContainer, Within.Action)
}

// Each chart opens the tooltip of a point with a tap, and a tap on another point of that chart closes the first one.
private val summaryStatisticsTooltips = scenario("summary-statistics-tooltips", "summary", hubStart()) {
	account(E2eAccounts.SummaryHub.id)

	val index = E2eInputs.StatisticsIndexChartKey
	val newest = E2eInputs.StatisticsNewestIndexPoint
	val earlier = E2eInputs.StatisticsEarlierIndexPoint

	waitSummaryHub()
	openStatistics()
	scrollUntilVisible(SummaryUiTags.statisticsTarget(index, newest), Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.statisticsTarget(index, newest))
	waitVisible(StatisticsTooltipUiTags.point(index, newest), Within.Action)
	tap(SummaryUiTags.statisticsTarget(index, earlier))
	waitVisible(StatisticsTooltipUiTags.point(index, earlier), Within.Action)
	waitGone(StatisticsTooltipUiTags.point(index, newest), Within.Action)

	val subjects = E2eInputs.StatisticsSubjectsChartKey
	val approved = E2eInputs.StatisticsApprovedSegment
	scrollUntilVisible(SummaryUiTags.statisticsTarget(subjects, approved), Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.statisticsTarget(subjects, approved))
	waitVisible(StatisticsTooltipUiTags.point(subjects, approved), Within.Action)

	val grades = E2eInputs.StatisticsGradesChartKey
	val gradeFour = E2eInputs.StatisticsGradeFourBar
	scrollUntilVisible(SummaryUiTags.statisticsTarget(grades, gradeFour), Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.statisticsTarget(grades, gradeFour))
	waitVisible(StatisticsTooltipUiTags.point(grades, gradeFour), Within.Action)
}

// The overdue evaluations of the term lead to the week of the oldest one, with that evaluation brought into view.
private val summaryStatisticsLinks = scenario("summary-statistics-links", "summary", hubStart()) {
	covers("summary.SummaryStatistics.OpenTarget")
	account(E2eAccounts.SummaryHub.id)

	waitSummaryHub()
	openStatistics()
	scrollUntilVisible(SummaryUiTags.StatisticsOverdue, Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.StatisticsOverdue)
	waitVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItemSelected, Within.Wait)
	waitVisible(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek2), Within.Long)
	waitVisible(EvaluationsUiTags.evaluationItemCard(E2eFixtures.HubEvaluationOldest.value), Within.Assert)
	waitGone(EvaluationsUiTags.evaluationsWeekPage(E2eInputs.HubWeek3), Within.Assert)
}

// The Statistics screen shares a closed term as an image: the sheet opens on the newest term, the student picks the
// previous one and the second background, and the share button hands the picture to the system's sheet, which the app
// gets back from. The sheet of the terms is still open afterwards, and it is closed by dragging its title.
private val summaryStatisticsShare = scenario("summary-statistics-share", "summary", hubStart()) {
	covers("summary.ShareTerm.SelectTerm", "summary.ShareTerm.SelectBackground", "summary.ShareTerm.ShareImage")
	account(E2eAccounts.SummaryHub.id)

	val newest = E2eFixtures.HubNewestClosedTermKey.value
	val previous = E2eFixtures.HubPreviousClosedTermKey.value
	val second = E2eFixtures.ShareBackgroundSecond.value

	waitSummaryHub()
	openStatistics()
	tap(BaseUiTags.topBarActionButton(TopBarAction.StatisticsShareAction))
	waitVisible(SummaryUiTags.ShareTermSheet, Within.Wait)
	waitVisible(ShareTermUiTags.termSelected(newest), Within.Assert)
	tap(ShareTermUiTags.term(previous))
	waitVisible(ShareTermUiTags.termSelected(previous), Within.Action)
	tap(ShareTermUiTags.background(second))
	waitVisible(ShareTermUiTags.backgroundSelected(second), Within.Action)
	tap(SummaryUiTags.ShareTermShareButton)
	onPlatform(Platform.Android) {
		waitVisible(system(ShareSheet.ANDROID_ELEMENT), Within.Action)
		// The chooser is in front of the app: the exit is announced before the app is brought back.
		waitBackgrounded()
	}
	onPlatform(Platform.Ios) {
		waitVisible(system(ShareSheet.IOS_ELEMENT), Within.Action)
		tapAtScreen(ShareSheet.DISMISS_X, ShareSheet.DISMISS_Y)
		waitGone(system(ShareSheet.IOS_ELEMENT), Within.Action)
	}
	foreground()
	waitVisible(SummaryUiTags.ShareTermSheet, Within.Action)
	swipeFrom(BaseUiTags.ConfirmationDialogTitle, SwipeDirection.Down)
	waitGone(SummaryUiTags.ShareTermSheet, Within.Action)
	waitVisible(SummaryUiTags.StatisticsContent, Within.Assert)
}

// The finished term of the hub shares that term: its sheet opens with the term chosen, and the student closes it. The
// hub shows its finished term only between terms, so the record is served without its term in progress.
private val summaryHubShareFinishedTerm = scenario(
	"summary-hub-share-finished-term",
	"summary",
	Start.Seeded(
		E2eAccounts.SummaryHub,
		mockStates = listOf(MockState("summary-hub-record", "NoCurrentTerm"))
	).toLaunchSpec()
) {
	account(E2eAccounts.SummaryHub.id)

	waitSummaryHub()
	scrollUntilVisible(SummaryUiTags.TermFinishedShare, Scroll.ContentDown, Within.Long)
	tap(SummaryUiTags.TermFinishedShare)
	waitVisible(SummaryUiTags.ShareTermSheet, Within.Wait)
	waitVisible(ShareTermUiTags.termSelected(E2eFixtures.HubNewestClosedTermKey.value), Within.Assert)
	swipeFrom(BaseUiTags.ConfirmationDialogTitle, SwipeDirection.Down)
	waitGone(SummaryUiTags.ShareTermSheet, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Assert)
}

// With the catalog of backgrounds unavailable, the sheet still works, and the included background is its only one.
private val summaryStatisticsShareWithoutCatalog = scenario(
	"summary-statistics-share-no-catalog",
	"summary",
	Start.Seeded(
		E2eAccounts.SummaryHub,
		mockStates = listOf(MockState("share-backgrounds", "Unavailable"))
	).toLaunchSpec()
) {
	account(E2eAccounts.SummaryHub.id)

	waitSummaryHub()
	openStatistics()
	tap(BaseUiTags.topBarActionButton(TopBarAction.StatisticsShareAction))
	waitVisible(SummaryUiTags.ShareTermSheet, Within.Wait)
	waitVisible(ShareTermUiTags.backgroundSelected(E2eFixtures.ShareBackgroundIncluded.value), Within.Long)
	waitGone(ShareTermUiTags.background(E2eFixtures.ShareBackgroundSecond.value), Within.Assert)
	assertEnabled(SummaryUiTags.ShareTermShareButton, true)
	swipeFrom(BaseUiTags.ConfirmationDialogTitle, SwipeDirection.Down)
	waitGone(SummaryUiTags.ShareTermSheet, Within.Action)
}

/** The scenarios of this module; list every new one here. */
val summaryScenarios: List<Scenario> = listOf(
	summarySmoke,
	summaryProfilePicture,
	summaryProfilePictureSources,
	summaryRefreshRetry,
	summaryStatusDialog,
	summaryPartialEnrollmentStatusDialog,
	summaryOutdatedCredentials,
	summaryNewStudentNoRecord,
	summaryRecordAccessDenied,
	summaryHubOverview,
	summaryHubAttentionLinks,
	summaryHubWeekLinks,
	summaryHubTermCareerLinks,
	summaryHubCareerInvite,
	summaryStatisticsOverview,
	summaryStatisticsTooltips,
	summaryStatisticsLinks,
	summaryStatisticsShare,
	summaryHubShareFinishedTerm,
	summaryStatisticsShareWithoutCatalog
)
