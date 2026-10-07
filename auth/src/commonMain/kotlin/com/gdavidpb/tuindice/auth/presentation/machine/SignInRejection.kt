package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError

/**
 * Whether the backend itself refused the attempt (wrong credentials, disabled account, untrusted
 * device). A failure on the way to the backend (no connection, timeout, a service that asked for a
 * wait, throttling) says nothing about the credentials and is not a rejection.
 */
internal fun SignInUseCaseError?.isRejectedByBackend(): Boolean {
	return when (this) {
		is SignInUseCaseError.InvalidCredentials,
		is SignInUseCaseError.AccountDisabled,
		is SignInUseCaseError.Untrusted -> true

		is SignInUseCaseError.Timeout,
		is SignInUseCaseError.AuthenticationFailed,
		is SignInUseCaseError.EmptyUsbId,
		is SignInUseCaseError.InvalidUsbId,
		is SignInUseCaseError.EmptyPassword,
		is SignInUseCaseError.Unavailable,
		is SignInUseCaseError.TooManyRequests,
		is SignInUseCaseError.OutdatedApp,
		is SignInUseCaseError.NoConnection,
		null -> false
	}
}
