package com.gdavidpb.tuindice.scenarios.fixture

/**
 * Every fixture account; each credential is declared by a mapping under `mocks/mappings/login/`.
 *
 * The same USB id is reused with different passwords, so the account [E2eAccount.id], not the USB id,
 * is the key. An account without session signs in through the UI only. `AccountFixturesTest` checks
 * every value against the mappings.
 */
object E2eAccounts {
	/** Session values of `mocks/mappings/login/auth-exchange-success.json`. */
	val Canonical = E2eAccount(
		id = "canonical",
		usbIdDigits = "1111111",
		usbIdFormatted = "11-11111",
		password = "123456",
		session = E2eSession(
			sessionId = "auth-session-initial",
			accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.exchange.mock.access",
			refreshToken = "refresh.mock.token.value"
		),
		mockScenario = "login-token-lifecycle"
	)

	/** Signs in with the USB email; the app sends `mail` (suffix stripped), as `auth-email-success.json` expects. */
	val CanonicalEmail = E2eAccount(
		id = "canonical-email",
		usbIdDigits = "mail@usb.ve",
		usbIdFormatted = "mail@usb.ve",
		password = "123456",
		session = null,
		mockScenario = null
	)

	/** Rejected: no mapping accepts it and `auth-unauthorized.json` answers 401. */
	val Invalid = E2eAccount(
		id = "invalid",
		usbIdDigits = "0000000",
		usbIdFormatted = "00-00000",
		password = "bad-password",
		session = null,
		mockScenario = null
	)

	/** Locked account (`auth-disabled-locked.json`, 423). */
	val Disabled = E2eAccount(
		id = "auth-disabled",
		usbIdDigits = "2222222",
		usbIdFormatted = "22-22222",
		password = "disabled-pass",
		session = null,
		mockScenario = null
	)

	/** Bootstrap answers 503 once, then succeeds (`auth-retry-login`); signs in through the UI only. */
	val ServiceUnavailableThenRetry = E2eAccount(
		id = "auth-retry",
		usbIdDigits = "3333333",
		usbIdFormatted = "33-33333",
		password = "retry-pass",
		session = null,
		mockScenario = null
	)

	/** Delayed bootstrap (`auth-login-cancel-bootstrap-delayed.json`). */
	val LoginCancel = E2eAccount(
		id = "login-cancel",
		usbIdDigits = "1111111",
		usbIdFormatted = "11-11111",
		password = "login-cancel-pass",
		session = null,
		mockScenario = null
	)

	/** Bootstrap answers 426 (`auth-outdated-app-bootstrap-upgrade-required.json`). */
	val OutdatedApp = E2eAccount(
		id = "outdated-app",
		usbIdDigits = "4444444",
		usbIdFormatted = "44-44444",
		password = "upgrade-pass",
		session = null,
		mockScenario = null
	)

	/** Session values of `auth-update-password-exchange-success.json`. */
	val UpdatePassword = E2eAccount(
		id = "update-password",
		usbIdDigits = "5555555",
		usbIdFormatted = "55-55555",
		password = "outdated-pass",
		session = E2eSession(
			sessionId = "auth-update-password-session",
			accessToken = "update.password.mock.access",
			refreshToken = "update.password.mock.refresh"
		),
		mockScenario = "auth-update-password-login"
	)

	/** Session values of `auth-update-password-failure-exchange-success.json`. */
	val UpdatePasswordFailure = E2eAccount(
		id = "update-password-failure",
		usbIdDigits = "7777777",
		usbIdFormatted = "77-77777",
		password = "outdated-pass",
		session = E2eSession(
			sessionId = "auth-update-password-failure-session",
			accessToken = "update.password.failure.mock.access",
			refreshToken = "update.password.failure.mock.refresh"
		),
		mockScenario = "auth-update-password-failure-login"
	)

	/** Session values of `auth-session-invalidated-exchange-success.json`. */
	val SessionInvalidated = E2eAccount(
		id = "session-invalidated",
		usbIdDigits = "6666666",
		usbIdFormatted = "66-66666",
		password = "expired-pass",
		session = E2eSession(
			sessionId = "auth-expired-session",
			accessToken = "expired.session.mock.access",
			refreshToken = "expired.session.mock.refresh"
		),
		mockScenario = "auth-session-invalidated-login"
	)

	/** Session values of `auth-summary-refresh-retry-exchange-success.json`. */
	val SummaryRefreshRetry = E2eAccount(
		id = "summary-refresh-retry",
		usbIdDigits = "9999999",
		usbIdFormatted = "99-99999",
		password = "summary-retry-pass",
		session = E2eSession(
			sessionId = "summary-refresh-retry-session",
			accessToken = "summary.refresh.retry.mock.access",
			refreshToken = "summary.refresh.retry.mock.refresh"
		),
		mockScenario = "auth-summary-refresh-retry-login"
	)

	/** Session values of `auth-summary-status-unavailable-exchange-success.json`. */
	val SummaryStatusUnavailable = E2eAccount(
		id = "summary-status-unavailable",
		usbIdDigits = "8888888",
		usbIdFormatted = "88-88888",
		password = "summary-unavailable-pass",
		session = E2eSession(
			sessionId = "summary-status-unavailable-session",
			accessToken = "summary.status.unavailable.mock.access",
			refreshToken = "summary.status.unavailable.mock.refresh"
		),
		mockScenario = "auth-summary-status-unavailable-login"
	)

	/** Session values of `auth-summary-outdated-credentials-exchange-success.json`. */
	val SummaryOutdatedCredentials = E2eAccount(
		id = "summary-outdated-credentials",
		usbIdDigits = "4444444",
		usbIdFormatted = "44-44444",
		password = "outdated-pass",
		session = E2eSession(
			sessionId = "summary-outdated-credentials-session",
			accessToken = "summary.outdated.credentials.mock.access",
			refreshToken = "summary.outdated.credentials.mock.refresh"
		),
		mockScenario = "auth-summary-outdated-credentials-login"
	)

	/** Session values of `auth-enrollment-unavailable-exchange-success.json`. */
	val EnrollmentUnavailable = E2eAccount(
		id = "enrollment-unavailable",
		usbIdDigits = "2222222",
		usbIdFormatted = "22-22222",
		password = "enrollment-unavailable-pass",
		session = E2eSession(
			sessionId = "enrollment-unavailable-session",
			accessToken = "enrollment.unavailable.mock.access",
			refreshToken = "enrollment.unavailable.mock.refresh"
		),
		mockScenario = "auth-enrollment-unavailable-login"
	)

	/** Session values of `auth-enrollment-fetching-cancel-exchange-success.json`. */
	val EnrollmentFetchingCancel = E2eAccount(
		id = "enrollment-fetching-cancel",
		usbIdDigits = "1111111",
		usbIdFormatted = "11-11111",
		password = "enrollment-fetching-cancel-pass",
		session = E2eSession(
			sessionId = "enrollment-fetching-cancel-session",
			accessToken = "enrollment.fetching.cancel.mock.access",
			refreshToken = "enrollment.fetching.cancel.mock.refresh"
		),
		mockScenario = "auth-enrollment-fetching-cancel-login"
	)

	/** Session values of `auth-enrollment-not-found-exchange-success.json`. */
	val EnrollmentNotFound = E2eAccount(
		id = "enrollment-not-found",
		usbIdDigits = "2345678",
		usbIdFormatted = "23-45678",
		password = "enrollment-not-found-pass",
		session = E2eSession(
			sessionId = "enrollment-not-found-session",
			accessToken = "enrollment.notfound.mock.access",
			refreshToken = "enrollment.notfound.mock.refresh"
		),
		mockScenario = "auth-enrollment-not-found-login"
	)

	/** Session values of `auth-enrollment-outdated-exchange-success.json`. */
	val EnrollmentOutdated = E2eAccount(
		id = "enrollment-outdated",
		usbIdDigits = "3333333",
		usbIdFormatted = "33-33333",
		password = "enrollment-outdated-pass",
		session = E2eSession(
			sessionId = "enrollment-outdated-session",
			accessToken = "enrollment.outdated.mock.access",
			refreshToken = "enrollment.outdated.mock.refresh"
		),
		mockScenario = "auth-enrollment-outdated-login"
	)

	/** Session values of `auth-new-student-exchange-success.json`. */
	val NewStudent = E2eAccount(
		id = "new-student",
		usbIdDigits = "3333344",
		usbIdFormatted = "33-33344",
		password = "new-student-pass",
		session = E2eSession(
			sessionId = "new-student-session",
			accessToken = "new-student.mock.access",
			refreshToken = "new-student.mock.refresh"
		),
		mockScenario = "auth-new-student-login"
	)

	/** Session values of `auth-record-denied-exchange-success.json`. */
	val RecordDenied = E2eAccount(
		id = "record-denied",
		usbIdDigits = "3434343",
		usbIdFormatted = "34-34343",
		password = "record-denied-pass",
		session = E2eSession(
			sessionId = "record-denied-session",
			accessToken = "record-denied.mock.access",
			refreshToken = "record-denied.mock.refresh"
		),
		mockScenario = "auth-record-denied-login"
	)

	/** Session values of `auth-record-refresh-retry-exchange-success.json`. */
	val RecordRefreshRetry = E2eAccount(
		id = "record-refresh-retry",
		usbIdDigits = "8888888",
		usbIdFormatted = "88-88888",
		password = "record-retry-pass",
		session = E2eSession(
			sessionId = "record-refresh-retry-session",
			accessToken = "record.refresh.retry.mock.access",
			refreshToken = "record.refresh.retry.mock.refresh"
		),
		mockScenario = "auth-record-refresh-retry-login"
	)

	/** Session values of `auth-record-term-rejected-exchange-success.json`. */
	val RecordTermRejected = E2eAccount(
		id = "record-term-rejected",
		usbIdDigits = "7777777",
		usbIdFormatted = "77-77777",
		password = "record-rejected-pass",
		session = E2eSession(
			sessionId = "record-term-rejected-session",
			accessToken = "record.term.rejected.mock.access",
			refreshToken = "record.term.rejected.mock.refresh"
		),
		mockScenario = "auth-record-term-rejected-login"
	)

	/** Session values of `auth-annulled-provisional-exchange-success.json`. */
	val AnnulledProvisional = E2eAccount(
		id = "annulled-provisional",
		usbIdDigits = "3131313",
		usbIdFormatted = "31-31313",
		password = "annulled-provisional-pass",
		session = E2eSession(
			sessionId = "auth-annulled-provisional-session",
			accessToken = "annulled-provisional.mock.access",
			refreshToken = "annulled-provisional.mock.refresh"
		),
		mockScenario = "auth-annulled-provisional-login"
	)

	/** Session values of `auth-annulled-final-exchange-success.json`. */
	val AnnulledFinal = E2eAccount(
		id = "annulled-final",
		usbIdDigits = "3232323",
		usbIdFormatted = "32-32323",
		password = "annulled-final-pass",
		session = E2eSession(
			sessionId = "annulled-final-session",
			accessToken = "annulled-final.mock.access",
			refreshToken = "annulled-final.mock.refresh"
		),
		mockScenario = "auth-annulled-final-login"
	)

	/** Session values of `auth-not-enrolled-exchange-success.json`. */
	val NotEnrolled = E2eAccount(
		id = "not-enrolled",
		usbIdDigits = "3030303",
		usbIdFormatted = "30-30303",
		password = "not-enrolled-pass",
		session = E2eSession(
			sessionId = "not-enrolled-session",
			accessToken = "not-enrolled.mock.access",
			refreshToken = "not-enrolled.mock.refresh"
		),
		mockScenario = "auth-not-enrolled-login"
	)

	/** Session values of `auth-evaluations-enrollment-unavailable-exchange-success.json`. */
	val EvaluationsEnrollmentUnavailable = E2eAccount(
		id = "evaluations-enrollment-unavailable",
		usbIdDigits = "1010101",
		usbIdFormatted = "10-10101",
		password = "evaluations-enrollment-unavailable-pass",
		session = E2eSession(
			sessionId = "evaluations-enrollment-unavailable-session",
			accessToken = "evaluations.enrollment.unavailable.mock.access",
			refreshToken = "evaluations.enrollment.unavailable.mock.refresh"
		),
		mockScenario = "auth-evaluations-enrollment-unavailable-login"
	)

	/** Session values of `auth-pensum-cache-exchange-success.json`. */
	val PensumCache = E2eAccount(
		id = "pensum-cache",
		usbIdDigits = "9999999",
		usbIdFormatted = "99-99999",
		password = "pensum-cache-pass",
		session = E2eSession(
			sessionId = "pensum-cache-session",
			accessToken = "pensum.cache.mock.access",
			refreshToken = "pensum.cache.mock.refresh"
		),
		mockScenario = "auth-pensum-cache-login"
	)

	/** Session values of `auth-pensum-no-current-exchange-success.json`. */
	val PensumNoCurrent = E2eAccount(
		id = "pensum-no-current",
		usbIdDigits = "5555555",
		usbIdFormatted = "55-55555",
		password = "pensum-no-current-pass",
		session = E2eSession(
			sessionId = "pensum-no-current-session",
			accessToken = "pensum.no.current.mock.access",
			refreshToken = "pensum.no.current.mock.refresh"
		),
		mockScenario = "auth-pensum-no-current-login"
	)

	/** Session values of `auth-pensum-equivalence-exchange-success.json`. */
	val PensumEquivalence = E2eAccount(
		id = "pensum-equivalence",
		usbIdDigits = "6666666",
		usbIdFormatted = "66-66666",
		password = "pensum-equivalence-pass",
		session = E2eSession(
			sessionId = "pensum-equivalence-session",
			accessToken = "pensum.equivalence.mock.access",
			refreshToken = "pensum.equivalence.mock.refresh"
		),
		mockScenario = "auth-pensum-equivalence-login"
	)

	/** Session values of `auth-pensum-record-unavailable-exchange-success.json`. */
	val PensumRecordUnavailable = E2eAccount(
		id = "pensum-record-unavailable",
		usbIdDigits = "4444444",
		usbIdFormatted = "44-44444",
		password = "pensum-record-unavailable-pass",
		session = E2eSession(
			sessionId = "pensum-record-unavailable-session",
			accessToken = "pensum.record.unavailable.mock.access",
			refreshToken = "pensum.record.unavailable.mock.refresh"
		),
		mockScenario = "auth-pensum-record-unavailable-login"
	)

	/** Session values of `auth-pensum-retry-exchange-success.json`. */
	val PensumRetry = E2eAccount(
		id = "pensum-retry",
		usbIdDigits = "7777777",
		usbIdFormatted = "77-77777",
		password = "pensum-retry-pass",
		session = E2eSession(
			sessionId = "pensum-retry-session",
			accessToken = "pensum.retry.mock.access",
			refreshToken = "pensum.retry.mock.refresh"
		),
		mockScenario = "auth-pensum-retry-login"
	)

	/** Session values of `auth-pensum-not-found-exchange-success.json`. */
	val PensumNotFound = E2eAccount(
		id = "pensum-not-found",
		usbIdDigits = "6666666",
		usbIdFormatted = "66-66666",
		password = "pensum-not-found-pass",
		session = E2eSession(
			sessionId = "pensum-not-found-session",
			accessToken = "pensum.notfound.mock.access",
			refreshToken = "pensum.notfound.mock.refresh"
		),
		mockScenario = "auth-pensum-not-found-login"
	)

	val all: List<E2eAccount> = listOf(
		Canonical,
		CanonicalEmail,
		Invalid,
		Disabled,
		ServiceUnavailableThenRetry,
		LoginCancel,
		OutdatedApp,
		UpdatePassword,
		UpdatePasswordFailure,
		SessionInvalidated,
		SummaryRefreshRetry,
		SummaryStatusUnavailable,
		SummaryOutdatedCredentials,
		EnrollmentUnavailable,
		EnrollmentFetchingCancel,
		EnrollmentNotFound,
		EnrollmentOutdated,
		NewStudent,
		RecordDenied,
		RecordRefreshRetry,
		RecordTermRejected,
		AnnulledProvisional,
		AnnulledFinal,
		NotEnrolled,
		EvaluationsEnrollmentUnavailable,
		PensumCache,
		PensumNoCurrent,
		PensumEquivalence,
		PensumRecordUnavailable,
		PensumRetry,
		PensumNotFound
	)
}
