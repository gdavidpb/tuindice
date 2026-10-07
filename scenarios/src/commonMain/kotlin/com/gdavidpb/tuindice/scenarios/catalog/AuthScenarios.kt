package com.gdavidpb.tuindice.scenarios.catalog

import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.scenariokit.dsl.StepBuilder
import com.gdavidpb.tuindice.scenariokit.dsl.assertEnabled
import com.gdavidpb.tuindice.scenariokit.dsl.back
import com.gdavidpb.tuindice.scenariokit.dsl.enterSecureText
import com.gdavidpb.tuindice.scenariokit.dsl.expectRequest
import com.gdavidpb.tuindice.scenariokit.dsl.ifVisible
import com.gdavidpb.tuindice.scenariokit.dsl.onPlatform
import com.gdavidpb.tuindice.scenariokit.dsl.relaunch
import com.gdavidpb.tuindice.scenariokit.dsl.scenario
import com.gdavidpb.tuindice.scenariokit.dsl.tag
import com.gdavidpb.tuindice.scenariokit.dsl.tap
import com.gdavidpb.tuindice.scenariokit.dsl.text
import com.gdavidpb.tuindice.scenariokit.dsl.waitAnyVisible
import com.gdavidpb.tuindice.scenariokit.dsl.waitGone
import com.gdavidpb.tuindice.scenariokit.dsl.waitVisible
import com.gdavidpb.tuindice.scenariokit.model.Platform
import com.gdavidpb.tuindice.scenariokit.model.Scenario
import com.gdavidpb.tuindice.scenarios.fixture.Copy
import com.gdavidpb.tuindice.scenarios.fixture.E2eAccounts
import com.gdavidpb.tuindice.scenarios.fixture.E2eFixtures
import com.gdavidpb.tuindice.scenarios.fixture.Start
import com.gdavidpb.tuindice.scenarios.shared.Within
import com.gdavidpb.tuindice.scenarios.shared.signInThroughUi
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkId
import com.gdavidpb.tuindice.wizard.ui.CoachmarkUiTags

/** `EvaluationType.TEST`, as the type chip tag spells it. */
private const val EVALUATION_TYPE_TEST = "test"

/** A day of the month the date picker always offers. */
private const val EVALUATION_DAY = 15

/** The arguments of a clean launch, to start the app again the way the scenario started it. */
private val cleanArguments = Start.Clean().toLaunchSpec().arguments

/**
 * A fresh install shows exactly one coachmark over the summary the first time it opens (the summary's own,
 * see `eligibleCoachmarkIds`): wait for it, confirm it, wait for it to leave, then the summary.
 */
private fun StepBuilder.reachSummaryAfterSignIn() {
	waitVisible(SummaryUiTags.ContentContainer, Within.Long)
	waitVisible(CoachmarkUiTags.currentCoachmark(CoachmarkId.Summary), Within.Action)
	tap(CoachmarkUiTags.ConfirmButton)
	waitGone(CoachmarkUiTags.Bubble, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
}

/**
 * The summary of an account whose password the university rejects: the update dialog may open over it, and
 * "later" closes it.
 */
private fun StepBuilder.reachSummaryPastPasswordDialog() {
	waitAnyVisible(
		tag(SummaryUiTags.ContentContainer),
		tag(AuthUiTags.UpdatePasswordIdleContainer),
		timeout = Within.Sync
	)
	ifVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Assert) {
		tap(BaseUiTags.ConfirmationDialogNegativeButton)
		waitGone(AuthUiTags.UpdatePasswordIdleContainer, Within.Action)
	}
	waitVisible(SummaryUiTags.ContentContainer, Within.Long)
}

/** From any tab: adds an evaluation of the primary attempt, so the account has a change to flush. */
private fun StepBuilder.addPendingEvaluation(withDate: Boolean) {
	tap(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Wait)
	tap(EvaluationsUiTags.EvaluationsAddFab)
	waitVisible(EvaluationsUiTags.EvaluationContentContainer, Within.Wait)
	tap(EvaluationsUiTags.evaluationSubjectChip(E2eFixtures.PrimaryAttempt.value))
	tap(EvaluationsUiTags.evaluationTypeChip(EVALUATION_TYPE_TEST))

	if (withDate) {
		tap(EvaluationsUiTags.EvaluationDateSelectButton)
		waitVisible(EvaluationsUiTags.EvaluationCalendarContainer, Within.Action)
		tap(EvaluationsUiTags.calendarDayCell(EVALUATION_DAY))
		tap(EvaluationsUiTags.EvaluationDateDialogAcceptButton)
	}

	tap(EvaluationsUiTags.EvaluationMaxGradeChip)
	waitVisible(EvaluationsUiTags.EvaluationDialogTitle, Within.Action)
	tap(EvaluationsUiTags.EvaluationDialogConfirmButton)
	tap(EvaluationsUiTags.EvaluationDoneFab)
	waitVisible(EvaluationsUiTags.EvaluationsContentContainer, Within.Action)
}

/** From the evaluations tab: back to the summary and into the sign-out sheet, which opens with its message. */
private fun StepBuilder.openSignOutFromSummary() {
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
	waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
}

/** The password sheet that opens when sign-out finds the stored password outdated, with the password typed. */
private fun StepBuilder.typeNewPassword(password: String) {
	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Action)
	tap(AuthUiTags.PasswordTextField)
	enterSecureText(AuthUiTags.PasswordTextField, password)
}

private val authLoginSuccess = scenario("auth-login-success", "auth", Start.Clean().toLaunchSpec()) {
	tags("smoke")
	covers("auth.SignIn.SetUsbId", "auth.SignIn.SetPassword", "auth.SignIn.ClickSignIn")
	account(E2eAccounts.Canonical.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	signInThroughUi(E2eAccounts.Canonical)
	reachSummaryAfterSignIn()
}

private val authLoginUsbEmail = scenario("auth-login-usb-email", "auth", Start.Clean().toLaunchSpec()) {
	covers(
		"auth.SignIn.ToggleIdentifierMode",
		"auth.SignIn.SetUsbId",
		"auth.SignIn.SetPassword",
		"auth.SignIn.ClickSignIn"
	)
	account(E2eAccounts.CanonicalEmail.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	waitVisible(AuthUiTags.IdentifierModeToggle, Within.Assert)
	tap(AuthUiTags.IdentifierModeToggle)
	waitVisible(text(Copy.UsbEmailHint), Within.Assert)
	signInThroughUi(E2eAccounts.CanonicalEmail)
	reachSummaryAfterSignIn()
}

private val authLoginUsbEmailUsbid = scenario("auth-login-usb-email-usbid", "auth", Start.Clean().toLaunchSpec()) {
	covers(
		"auth.SignIn.ToggleIdentifierMode",
		"auth.SignIn.SetUsbId",
		"auth.SignIn.SetPassword",
		"auth.SignIn.ClickSignIn"
	)
	account(E2eAccounts.Canonical.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	waitVisible(AuthUiTags.IdentifierModeToggle, Within.Assert)
	tap(AuthUiTags.IdentifierModeToggle)
	waitVisible(text(Copy.UsbEmailHint), Within.Assert)
	signInThroughUi(
		E2eAccounts.Canonical,
		typed = E2eAccounts.Canonical.usbIdDigits,
		shown = E2eAccounts.Canonical.usbIdDigits
	)
	reachSummaryAfterSignIn()
}

private val authUsageDataConsent = scenario("auth-usage-data-consent", "auth", Start.Clean().toLaunchSpec()) {
	covers(
		"auth.SignIn.SetUsageDataCollectionEnabled",
		"auth.SignIn.SetUsbId",
		"auth.SignIn.SetPassword",
		"auth.SignIn.ClickSignIn"
	)
	account(E2eAccounts.Canonical.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	waitVisible(AuthUiTags.UsageDataConsentCheckbox, Within.Assert)
	tap(AuthUiTags.UsageDataConsentCheckbox)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
	signInThroughUi(E2eAccounts.Canonical)
	reachSummaryAfterSignIn()
}

private val authLoginInvalid = scenario("auth-login-invalid", "auth", Start.Clean().toLaunchSpec()) {
	covers("auth.SignIn.SetUsbId", "auth.SignIn.SetPassword", "auth.SignIn.ClickSignIn")
	account(E2eAccounts.Invalid.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	// The helper has already seen 00-00000:bad-password reach the backend; the rejection must show.
	signInThroughUi(E2eAccounts.Invalid)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Action)
	waitVisible(BaseUiTags.SnackbarMessage, Within.Assert)
	waitVisible(text(Copy.InvalidUsbIdCredentials), Within.Assert)
	waitVisible(AuthUiTags.SignInRejectedMarker, Within.Assert)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
}

private val authLoginDisabled = scenario("auth-login-disabled", "auth", Start.Clean().toLaunchSpec()) {
	covers("auth.SignIn.SetUsbId", "auth.SignIn.SetPassword", "auth.SignIn.ClickSignIn")
	account(E2eAccounts.Disabled.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	signInThroughUi(E2eAccounts.Disabled)
	waitVisible(BaseUiTags.SnackbarContainer, Within.Wait)
	waitVisible(BaseUiTags.SnackbarMessage, Within.Assert)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
}

/**
 * The university answers 503 with `Retry-After: 10`: sign-in waits instead of offering an immediate retry,
 * and comes back when the wait is over.
 */
private val authLoginRetryAfterUnavailable = scenario(
	"auth-login-retry-after-unavailable",
	"auth",
	Start.Clean().toLaunchSpec()
) {
	covers("auth.SignIn.SetUsbId", "auth.SignIn.SetPassword", "auth.SignIn.ClickSignIn")
	account(E2eAccounts.ServiceUnavailableThenRetry.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	signInThroughUi(E2eAccounts.ServiceUnavailableThenRetry)
	waitVisible(AuthUiTags.ServiceUnavailableMessage, Within.Wait)
	waitVisible(text(Copy.SignInServiceUnavailable), Within.Assert)
	assertEnabled(AuthUiTags.SignInButton, false)
	waitGone(BaseUiTags.SnackbarActionButton, Within.Assert)
	waitGone(AuthUiTags.ServiceUnavailableMessage, Within.Long)
	assertEnabled(AuthUiTags.SignInButton, true)
	tap(AuthUiTags.SignInButton)
	waitVisible(AuthUiTags.ServiceUnavailableMessage, Within.Wait)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
}

private val authLoginCancel = scenario("auth-login-cancel", "auth", Start.Clean().toLaunchSpec()) {
	covers("auth.SignIn.ClickCancelSignIn")
	account(E2eAccounts.LoginCancel.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	signInThroughUi(E2eAccounts.LoginCancel)
	waitVisible(AuthUiTags.SignInCancelButton, Within.Action)
	tap(AuthUiTags.SignInCancelButton)
	waitVisible(AuthUiTags.SignInButton, Within.Action)
	waitGone(AuthUiTags.SignInLoggingInContainer, Within.Assert)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Assert)
}

private val authLoginOutdatedApp = scenario("auth-login-outdated-app", "auth", Start.Clean().toLaunchSpec()) {
	covers("auth.SignIn.SetUsbId", "auth.SignIn.SetPassword", "auth.SignIn.ClickSignIn")
	account(E2eAccounts.OutdatedApp.id)
	signsIn()

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	signInThroughUi(E2eAccounts.OutdatedApp)
	waitVisible(BaseUiTags.OutdatedAppScreen, Within.Sync)
	waitVisible(BaseUiTags.OutdatedAppTitle, Within.Assert)
	waitVisible(BaseUiTags.OutdatedAppMessage, Within.Assert)
	waitVisible(BaseUiTags.OutdatedAppAnimation, Within.Assert)
	waitVisible(BaseUiTags.OutdatedAppUpdateButton, Within.Assert)
	waitGone(AuthUiTags.SignInIdleContainer, Within.Assert)
	waitGone(MaincoreUiTags.TuIndiceNavHost, Within.Assert)
	// The outdated state outlives the process: starting again, without clearing anything, lands on it.
	relaunch(cleanArguments)
	waitVisible(BaseUiTags.OutdatedAppScreen, Within.Sync)
	waitVisible(BaseUiTags.OutdatedAppUpdateButton, Within.Assert)
	waitGone(MaincoreUiTags.TuIndiceNavHost, Within.Assert)
}

private val authLoginPasswordToggle = scenario("auth-login-password-toggle", "auth", Start.Clean().toLaunchSpec()) {
	covers("auth.SignIn.SetPassword", "auth.SignIn.TogglePasswordVisibility")

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	waitVisible(AuthUiTags.PasswordTextField, Within.Assert)
	tap(AuthUiTags.PasswordTextField)
	enterSecureText(AuthUiTags.PasswordTextField, E2eAccounts.Canonical.password)
	tap(AuthUiTags.KeyboardDismissArea)
	tap(AuthUiTags.PasswordToggle)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
	tap(AuthUiTags.PasswordToggle)
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Assert)
}

private val authTermsPrivacyFromLogin = scenario(
	"auth-terms-privacy-from-login",
	"auth",
	Start.Clean().toLaunchSpec()
) {
	covers("auth.SignIn.ClickTermsAndConditions", "auth.SignIn.ClickPrivacyPolicy")

	waitVisible(AuthUiTags.SignInIdleContainer, Within.Sync)
	tap(AuthUiTags.TermsAndConditionsLink)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Action)
	onPlatform(Platform.Android) {
		back()
	}
	onPlatform(Platform.Ios) {
		tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	}
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Action)
	tap(AuthUiTags.PrivacyPolicyLink)
	waitVisible(MaincoreUiTags.BrowserContainer, Within.Action)
	onPlatform(Platform.Android) {
		back()
	}
	onPlatform(Platform.Ios) {
		tap(MaincoreUiTags.TuIndiceTopBarBackButton)
	}
	waitVisible(AuthUiTags.SignInIdleContainer, Within.Action)
}

private val authSessionInvalidated = scenario(
	"auth-session-invalidated",
	"auth",
	Start.Seeded(E2eAccounts.SessionInvalidated).toLaunchSpec()
) {
	account(E2eAccounts.SessionInvalidated.id)

	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Sync)
}

private val authUpdatePassword = scenario(
	"auth-update-password",
	"auth",
	Start.Seeded(E2eAccounts.UpdatePassword).toLaunchSpec()
) {
	covers(
		"auth.UpdatePassword.SetPassword",
		"auth.UpdatePassword.TogglePasswordVisibility",
		"auth.UpdatePassword.ClickSignIn",
		"auth.SignOut.ClickSignOut",
		"auth.SignOut.RetryFlushAndSignOut"
	)
	account(E2eAccounts.UpdatePassword.id)

	reachSummaryPastPasswordDialog()
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	addPendingEvaluation(withDate = false)
	openSignOutFromSummary()
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.SignOutSecondaryButton, Within.Wait)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	typeNewPassword(E2eFixtures.UpdatedPassword)
	tap(AuthUiTags.PasswordToggle)
	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Assert)
	tap(AuthUiTags.PasswordToggle)
	tap(AuthUiTags.UpdatePasswordConfirmButton)
	// The sheet shows its loading state only while the reissue is in flight, so look at it first.
	onPlatform(Platform.Android) {
		waitVisible(AuthUiTags.UpdatePasswordConfirmLoading, Within.Assert)
		assertEnabled(AuthUiTags.PasswordTextField, false)
		assertEnabled(AuthUiTags.PasswordToggle, false)
		assertEnabled(AuthUiTags.UpdatePasswordConfirmButton, false)
	}
	expectRequest(
		"POST",
		"/auth/v1/token",
		basicAuth = "${E2eAccounts.UpdatePassword.backendIdentifier}:${E2eFixtures.UpdatedPassword}"
	)
	waitGone(AuthUiTags.UpdatePasswordIdleContainer, Within.Wait)
	waitAnyVisible(
		tag(AuthUiTags.SignOutSecondaryButton),
		tag(AuthUiTags.UsbIdTextField),
		timeout = Within.Sync
	)
	ifVisible(AuthUiTags.SignOutSecondaryButton) {
		tap(AuthUiTags.SignOutSecondaryButton)
	}
	waitVisible(AuthUiTags.UsbIdTextField, Within.Sync)
}

private val authUpdatePasswordFailure = scenario(
	"auth-update-password-failure",
	"auth",
	Start.Seeded(E2eAccounts.UpdatePasswordFailure).toLaunchSpec()
) {
	covers(
		"auth.UpdatePassword.SetPassword",
		"auth.UpdatePassword.ClickSignIn",
		"auth.SignOut.ClickSignOut"
	)
	account(E2eAccounts.UpdatePasswordFailure.id)

	reachSummaryPastPasswordDialog()
	tap(MaincoreUiTags.TuIndiceBottomBarRecordItem)
	waitVisible(RecordUiTags.ContentContainer, Within.Long)
	addPendingEvaluation(withDate = false)
	openSignOutFromSummary()
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.SignOutSecondaryButton, Within.Wait)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	typeNewPassword(E2eFixtures.RejectedPassword)
	tap(AuthUiTags.UpdatePasswordConfirmButton)
	expectRequest(
		"POST",
		"/auth/v1/token",
		basicAuth = "${E2eAccounts.UpdatePasswordFailure.backendIdentifier}:${E2eFixtures.RejectedPassword}"
	)
	// The update answered 401: the sheet leaves its loading state and stays open.
	assertEnabled(AuthUiTags.UpdatePasswordConfirmButton, true, Within.Wait)
	waitVisible(AuthUiTags.UpdatePasswordIdleContainer, Within.Assert)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitVisible(AuthUiTags.SignOutSecondaryButton, Within.Wait)
	waitVisible(AuthUiTags.SignOutMessageText, Within.Assert)
}

private val authSignOutCancel = scenario(
	"auth-sign-out-cancel",
	"auth",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
	waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
	tap(BaseUiTags.ConfirmationDialogNegativeButton)
	waitGone(BaseUiTags.ConfirmationDialogSheet, Within.Action)
	waitVisible(SummaryUiTags.ContentContainer, Within.Assert)
}

private val authSignOut = scenario(
	"auth-sign-out",
	"auth",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	tags("smoke")
	covers("auth.SignOut.ClickSignOut")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	tap(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
	waitVisible(SummaryUiTags.ContentContainer, Within.Wait)
	tap(BaseUiTags.topBarActionButton(TopBarAction.SignOutAction))
	waitVisible(AuthUiTags.SignOutMessageText, Within.Action)
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Wait)
}

/** Sign-out with a change the backend refuses: flushing fails, and signing out anyway is offered. */
private val authPendingSignOut = scenario(
	"auth-pending-sign-out",
	"auth",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("auth.SignOut.ClickSignOut", "auth.SignOut.ForceSignOut")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	addPendingEvaluation(withDate = false)
	openSignOutFromSummary()
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.SignOutSecondaryButton, Within.Long)
	tap(AuthUiTags.SignOutSecondaryButton)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Long)
}

/** Sign-out with a change the backend refuses once and then accepts: the flush succeeds and signs out. */
private val authPendingSignOutFlushSuccess = scenario(
	"auth-pending-sign-out-flush-success",
	"auth",
	Start.Seeded(E2eAccounts.Canonical).toLaunchSpec()
) {
	covers("auth.SignOut.ClickSignOut")
	account(E2eAccounts.Canonical.id)

	waitVisible(SummaryUiTags.ContentContainer, Within.Sync)
	addPendingEvaluation(withDate = true)
	openSignOutFromSummary()
	tap(BaseUiTags.ConfirmationDialogPositiveButton)
	waitVisible(AuthUiTags.UsbIdTextField, Within.Sync)
}

/** The scenarios of this module; list every new one here. */
val authScenarios: List<Scenario> = listOf(
	authLoginSuccess,
	authLoginUsbEmail,
	authLoginUsbEmailUsbid,
	authUsageDataConsent,
	authLoginInvalid,
	authLoginDisabled,
	authLoginRetryAfterUnavailable,
	authLoginCancel,
	authLoginOutdatedApp,
	authLoginPasswordToggle,
	authTermsPrivacyFromLogin,
	authSessionInvalidated,
	authUpdatePassword,
	authUpdatePasswordFailure,
	authSignOutCancel,
	authSignOut,
	authPendingSignOut,
	authPendingSignOutFlushSuccess
)
