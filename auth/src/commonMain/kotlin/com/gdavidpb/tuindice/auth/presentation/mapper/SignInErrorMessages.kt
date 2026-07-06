package com.gdavidpb.tuindice.auth.presentation.mapper

import com.gdavidpb.tuindice.auth.domain.model.SignInIdentifierMode
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_usb_email_credentials
import tuindice.auth.generated.resources.error_invalid_usb_id_credentials
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.snack_sign_in_failed
import tuindice.auth.generated.resources.snack_too_many_requests

internal suspend fun SignInUseCaseError?.toErrorMessage(
	identifierMode: SignInIdentifierMode,
	supportEmail: String
): String {
	return when (this) {
		is SignInUseCaseError.InvalidCredentials ->
			when (identifierMode) {
				SignInIdentifierMode.UsbId ->
					getString(Res.string.error_invalid_usb_id_credentials)

				SignInIdentifierMode.UsbEmail ->
					getString(Res.string.error_invalid_usb_email_credentials)
			}

		// Terminal outcomes the user cannot solve alone: both point at support.
		is SignInUseCaseError.AccountDisabled ->
			getString(Res.string.error_account_disabled, supportEmail)

		is SignInUseCaseError.Untrusted ->
			getString(Res.string.error_untrusted, supportEmail)

		is SignInUseCaseError.AuthenticationFailed ->
			getString(Res.string.snack_sign_in_failed)

		is SignInUseCaseError.NoConnection ->
			if (isNetworkAvailable) {
				commonServiceUnavailableMessage()
			} else {
				commonNetworkUnavailableMessage()
			}

		is SignInUseCaseError.Timeout ->
			commonTimeoutMessage()

		is SignInUseCaseError.Unavailable ->
			commonServiceUnavailableMessage()

		is SignInUseCaseError.TooManyRequests ->
			getString(Res.string.snack_too_many_requests)

		else ->
			commonUnexpectedErrorMessage()
	}
}
