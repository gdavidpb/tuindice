package com.gdavidpb.tuindice.testkit.e2e

/**
 * Kotlin mirror of `testkit/e2e/fixture-contract.env` for platform tests that need the
 * canonical local-backend fixture values (future Espresso/UI Automator/XCUITest edges).
 *
 * `verifyE2eContract` fails when these constants drift from the env file — edit both
 * together.
 */
object E2eFixtureContract {
	const val CANONICAL_USBID_RAW = "1111111"
	const val CANONICAL_USBID_FORMATTED = "11-11111"
	const val USB_EMAIL_LOCAL = "mail"
	const val USB_EMAIL_FULL = "mail@usb.ve"
	const val CANONICAL_PASSWORD = "123456"
	const val INVALID_USBID_RAW = "0000000"

	object RecordSearch {
		const val PRIORITY_PLANNED = "EP1308"
		const val PRIORITY_UNAVAILABLE = "EP2308"
		const val HISTORICAL_RETIRED = "MA1112"
		const val HISTORICAL_FAILED = "MA1121"
		const val HISTORICAL_APPROVED = "MA1111"
	}
}
