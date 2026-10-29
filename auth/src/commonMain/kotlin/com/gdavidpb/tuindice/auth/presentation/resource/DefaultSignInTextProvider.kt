package com.gdavidpb.tuindice.auth.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_usb_email_credentials
import tuindice.auth.generated.resources.error_invalid_usb_id_credentials
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.error_usb_unavailable
import tuindice.auth.generated.resources.snack_sign_in_failed
import tuindice.auth.generated.resources.snack_too_many_requests

class DefaultSignInTextProvider : SignInTextProvider {
	override suspend fun invalidUsbIdCredentials(): String = getString(Res.string.error_invalid_usb_id_credentials)

	override suspend fun invalidUsbEmailCredentials(): String =
		getString(Res.string.error_invalid_usb_email_credentials)

	override suspend fun accountDisabled(supportEmail: String): String =
		getString(Res.string.error_account_disabled, supportEmail)

	override suspend fun untrusted(supportEmail: String): String = getString(Res.string.error_untrusted, supportEmail)

	override suspend fun authenticationFailed(): String = getString(Res.string.snack_sign_in_failed)

	override suspend fun usbUnavailable(): String = getString(Res.string.error_usb_unavailable)

	override suspend fun tooManyRequests(): String = getString(Res.string.snack_too_many_requests)
}
