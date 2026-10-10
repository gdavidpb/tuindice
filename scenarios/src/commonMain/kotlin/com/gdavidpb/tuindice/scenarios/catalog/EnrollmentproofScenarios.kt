package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.foreground
import com.gdavidpb.tuindice.scenariokit.dsl.mockState
import com.gdavidpb.tuindice.scenariokit.dsl.relaunch
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitBackgrounded
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccount
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.openCurrentEnrollmentProof
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.time.Duration

private fun seeded(account: E2eAccount) = Start.Seeded(account).toLaunchSpec()

/** From the seeded summary to the record tab, waiting up to [within] for its content. */
private fun StepBuilder.openRecord(within: Duration = Within.Long) {
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, within)
}

private val enrollmentproofSmoke = scenario(
	"enrollmentproof-smoke",
	"enrollmentproof",
	seeded(E2eAccounts.Canonical)
) {
	covers("record.Record.SelectTerm")
	account(E2eAccounts.Canonical.id)
	tags("smoke")

	openRecord(Within.Wait)
	tap(RecordUiTags.termChip(E2eFixtures.CurrentTerm.value))
	waitVisible(RecordUiTags.EnrollmentProofButton, Within.Action)
}

private val enrollmentproofFetchingCancel = scenario(
	"enrollmentproof-fetching-cancel",
	"enrollmentproof",
	seeded(E2eAccounts.EnrollmentFetchingCancel)
) {
	covers("record.Record.SelectTerm", "enrollmentproof.Enrollment.FetchEnrollmentProof")
	account(E2eAccounts.EnrollmentFetchingCancel.id)

	openRecord()
	openCurrentEnrollmentProof()
	waitVisible(EnrollmentProofUiTags.FetchingSheet, Within.Action)
	waitVisible(EnrollmentProofUiTags.FetchingLoadingContainer, Within.Assert)
	waitVisible(EnrollmentProofUiTags.FetchingMessage, Within.Assert)
	tap(EnrollmentProofUiTags.FetchingCancelButton)
	waitGone(EnrollmentProofUiTags.FetchingSheet, Within.Action)
	waitGone(EnrollmentProofUiTags.FetchingCancelButton, Within.Assert)
	waitVisible(RecordUiTags.ContentContainer, Within.Action)
}

private val enrollmentproofErrorUnavailable = scenario(
	"enrollmentproof-error-unavailable",
	"enrollmentproof",
	seeded(E2eAccounts.EnrollmentUnavailable)
) {
	covers("record.Record.SelectTerm", "enrollmentproof.Enrollment.FetchEnrollmentProof")
	account(E2eAccounts.EnrollmentUnavailable.id)

	openRecord()
	openCurrentEnrollmentProof()
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(RecordUiTags.ContentContainer, Within.Assert)
	// Retrying from the snackbar asks for the proof again from where the user is: the first snackbar goes with
	// the tap, and the service is still down, so a new one comes back (the mock holds its answer for 3 s, long
	// enough to see the first one gone); the record tab must still be the one on screen.
	tap(BaseUiTags.SnackbarActionButton)
	waitGone(BaseUiTags.SnackbarContainer, Within.Action)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(RecordUiTags.ContentContainer, Within.Assert)
}

private val enrollmentproofNotFound = scenario(
	"enrollmentproof-not-found",
	"enrollmentproof",
	seeded(E2eAccounts.EnrollmentNotFound)
) {
	covers("record.Record.SelectTerm", "enrollmentproof.Enrollment.FetchEnrollmentProof")
	account(E2eAccounts.EnrollmentNotFound.id)

	openRecord()
	openCurrentEnrollmentProof()
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	// There is nothing to retry: the university has no proof for this term.
	waitGone(BaseUiTags.SnackbarActionButton, Within.Assert)
	waitVisible(RecordUiTags.ContentContainer, Within.Assert)
}

private val enrollmentproofAnnulledNotFound = scenario(
	"enrollmentproof-annulled-not-found",
	"enrollmentproof",
	seeded(E2eAccounts.AnnulledProvisional)
) {
	covers("record.Record.SelectTerm", "enrollmentproof.Enrollment.FetchEnrollmentProof")
	account(E2eAccounts.AnnulledProvisional.id)

	openRecord()
	openCurrentEnrollmentProof()
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(text(Copy.EnrollmentProofAnnulled), Within.Assert)
	waitGone(BaseUiTags.SnackbarActionButton, Within.Assert)
	waitVisible(RecordUiTags.ContentContainer, Within.Assert)
}

private val enrollmentproofOutdatedCredentials = scenario(
	"enrollmentproof-outdated-credentials",
	"enrollmentproof",
	seeded(E2eAccounts.EnrollmentOutdated)
) {
	covers("record.Record.SelectTerm", "enrollmentproof.Enrollment.FetchEnrollmentProof")
	account(E2eAccounts.EnrollmentOutdated.id)

	openRecord()
	openCurrentEnrollmentProof()
	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Wait)
}

/**
 * A proof that downloaded once is kept: when the service is down the next time, the app offers that saved copy
 * and asks before opening it. Cancelling leaves the record on screen; asking again and accepting hands the saved
 * copy to the device's viewer, which takes the app out of the foreground (YE-3).
 */
private val enrollmentproofSavedCopyDialog = scenario(
	"enrollmentproof-saved-copy-dialog",
	"enrollmentproof",
	seeded(E2eAccounts.Canonical)
) {
	covers(
		"record.Record.SelectTerm",
		"enrollmentproof.Enrollment.FetchEnrollmentProof",
		"enrollmentproof.Enrollment.OpenSavedEnrollmentProof"
	)
	account(E2eAccounts.Canonical.id)

	openRecord()
	openCurrentEnrollmentProof()
	// The first download is held by the mock for its slow profile (`enrollment-proof-success.json`), so this wait is
	// the `Long` one (YE-7): the delay of the mock and the hand-off of the file must both fit in it.
	waitBackgrounded(Within.Long)
	foreground()
	waitVisible(RecordUiTags.ContentContainer, Within.Action)
	mockState("enrollment-proof-saved-copy", "Unavailable")
	openCurrentEnrollmentProof()
	waitVisible(EnrollmentProofUiTags.SavedCopyMessage, Within.Wait)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(EnrollmentProofUiTags.SavedCopyMessage, Within.Action)
	waitVisible(RecordUiTags.ContentContainer, Within.Assert)
	openCurrentEnrollmentProof()
	waitVisible(EnrollmentProofUiTags.SavedCopyMessage, Within.Wait)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitBackgrounded(Within.Action)
	foreground()
	waitVisible(RecordUiTags.ContentContainer, Within.Action)
}

/**
 * The proofs the app saved are wiped when the session ends (YE-10): after signing out and in again as the same account,
 * with the service down, the app offers no saved copy and says the service is unavailable, as when nothing was
 * ever saved (`enrollmentproof-error-unavailable`). A copy left behind would show the dialog of the saved copy, which
 * stays on screen until it is answered, so its absence is read with a wait that fails when it is there.
 */
private val enrollmentproofSavedCopyGoneAfterSignOut = scenario(
	"enrollmentproof-saved-copy-gone-after-sign-out",
	"enrollmentproof",
	seeded(E2eAccounts.Canonical)
) {
	covers("record.Record.SelectTerm", "enrollmentproof.Enrollment.FetchEnrollmentProof", "auth.SignOut.ClickSignOut")
	account(E2eAccounts.Canonical.id)

	openRecord()
	openCurrentEnrollmentProof()
	waitBackgrounded(Within.Long)
	foreground()
	waitVisible(RecordUiTags.ContentContainer, Within.Action)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
	waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Wait)
	relaunch(seeded(E2eAccounts.Canonical).arguments)
	mockState("enrollment-proof-saved-copy", "Unavailable")
	openRecord()
	openCurrentEnrollmentProof()
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(RecordUiTags.ContentContainer, Within.Assert)
	waitGone(EnrollmentProofUiTags.SavedCopyMessage, Within.Assert)
}

/** The scenarios of this module; list every new one here. */
val enrollmentproofScenarios: List<Scenario> = listOf(
	enrollmentproofSmoke,
	enrollmentproofFetchingCancel,
	enrollmentproofErrorUnavailable,
	enrollmentproofNotFound,
	enrollmentproofAnnulledNotFound,
	enrollmentproofOutdatedCredentials,
	enrollmentproofSavedCopyDialog,
	enrollmentproofSavedCopyGoneAfterSignOut
)
