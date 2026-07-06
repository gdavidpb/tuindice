package com.gdavidpb.tuindice.auth.presentation.mapper

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.base.presentation.mapper.commonNetworkUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonServiceUnavailableMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonTimeoutMessage
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_password
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.snack_update_password_failed

internal suspend fun SignInUseCaseError?.toUpdatePasswordErrorMessage(
	supportEmail: String
): String {
	return when (this) {
		is SignInUseCaseError.InvalidCredentials ->
			getString(Res.string.error_invalid_password)

		// Terminal outcomes the user cannot solve alone: both point at support.
		is SignInUseCaseError.AccountDisabled ->
			getString(Res.string.error_account_disabled, supportEmail)

		is SignInUseCaseError.Untrusted ->
			getString(Res.string.error_untrusted, supportEmail)

		is SignInUseCaseError.AuthenticationFailed ->
			getString(Res.string.snack_update_password_failed)

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

		else ->
			commonUnexpectedErrorMessage()
	}
}
