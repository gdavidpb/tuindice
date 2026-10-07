package com.gdavidpb.tuindice.scenarios.fixture

/**
 * A credential the WireMock login mappings accept (or, for `Invalid`, reject).
 *
 * [session] is null for an account that only signs in through the UI. [id], not the USB id, is the
 * key: the same USB id is reused with different passwords. [usbIdDigits] is what the sign-in field is
 * typed with and [usbIdFormatted] what it shows back (the two are equal for a USB email).
 */
data class E2eAccount(
	val id: String,
	val usbIdDigits: String,
	val usbIdFormatted: String,
	val password: String,
	val session: E2eSession?,
	val mockScenario: String?
) {
	/** The identifier the backend receives in `Authorization: Basic`: the app drops the `@usb.ve` suffix. */
	val backendIdentifier: String get() = usbIdFormatted.removeSuffix(USB_EMAIL_SUFFIX)

	private companion object {
		const val USB_EMAIL_SUFFIX = "@usb.ve"
	}
}
