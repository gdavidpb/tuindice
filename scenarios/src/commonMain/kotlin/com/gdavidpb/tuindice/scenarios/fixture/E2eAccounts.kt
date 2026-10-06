package com.gdavidpb.tuindice.scenarios.fixture

/** Every fixture account; each credential is declared by a mapping under `mocks/mappings/login/`. */
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

	/** Delayed bootstrap (`auth-login-cancel-bootstrap-delayed.json`); signs in through the UI only. */
	val LoginCancel = E2eAccount(
		id = "login-cancel",
		usbIdDigits = "1111111",
		usbIdFormatted = "11-11111",
		password = "login-cancel-pass",
		session = null,
		mockScenario = null
	)

	val all: List<E2eAccount> = listOf(Canonical, LoginCancel)
}
