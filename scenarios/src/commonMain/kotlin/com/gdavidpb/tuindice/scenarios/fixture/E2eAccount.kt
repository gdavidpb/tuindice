package com.gdavidpb.tuindice.scenarios.fixture

/**
 * A credential the WireMock login mappings accept.
 *
 * [session] is null for an account that only signs in through the UI. [id], not the USB id, is the
 * key: the same USB id is reused with different passwords.
 */
data class E2eAccount(
	val id: String,
	val usbIdDigits: String,
	val usbIdFormatted: String,
	val password: String,
	val session: E2eSession?,
	val mockScenario: String?
)
