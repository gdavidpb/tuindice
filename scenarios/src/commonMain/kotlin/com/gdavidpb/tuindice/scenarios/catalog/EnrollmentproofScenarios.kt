package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
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

/** From the seeded summary to the record tab; [within] is how long the flow waited for its content. */
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
	// Retrying from the snackbar asks for the proof again from where the user is: the service is still
	// down, so the snackbar comes back, and the record tab must still be the one on screen.
	tap(BaseUiTags.SnackbarActionButton)
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

/** The scenarios of this module; list every new one here. */
val enrollmentproofScenarios: List<Scenario> = listOf(
	enrollmentproofSmoke,
	enrollmentproofFetchingCancel,
	enrollmentproofErrorUnavailable,
	enrollmentproofNotFound,
	enrollmentproofAnnulledNotFound,
	enrollmentproofOutdatedCredentials
)
