package com.gdavidpb.tuindice.auth.presentation.mapper

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.error_account_disabled
import tuindice.auth.generated.resources.error_invalid_password
import tuindice.auth.generated.resources.error_untrusted
import tuindice.auth.generated.resources.snack_default_error
import tuindice.auth.generated.resources.snack_network_unavailable
import tuindice.auth.generated.resources.snack_service_unavailable
import tuindice.auth.generated.resources.snack_timeout
import tuindice.auth.generated.resources.snack_update_password_failed

internal suspend fun SignInUseCaseError?.toUpdatePasswordErrorMessage(): String {
	return when (this) {
		is SignInUseCaseError.InvalidCredentials ->
			getString(Res.string.error_invalid_password)

		is SignInUseCaseError.AccountDisabled ->
			getString(Res.string.error_account_disabled)

		is SignInUseCaseError.Untrusted ->
			getString(Res.string.error_untrusted)

		is SignInUseCaseError.AuthenticationFailed ->
			getString(Res.string.snack_update_password_failed)

		is SignInUseCaseError.NoConnection ->
			if (isNetworkAvailable)
				getString(Res.string.snack_service_unavailable)
			else
				getString(Res.string.snack_network_unavailable)

		is SignInUseCaseError.Timeout ->
			getString(Res.string.snack_timeout)

		is SignInUseCaseError.Unavailable ->
			getString(Res.string.snack_service_unavailable)

		else ->
			getString(Res.string.snack_default_error)
	}
}
