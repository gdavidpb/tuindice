package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.SwipeDirection
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.swipeScreen
import com.gdavidpb.tuindice.scenariokit.dsl.system
import com.gdavidpb.tuindice.scenariokit.dsl.tag
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitBackgrounded
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
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
	summaryRecordAccessDenied
)
