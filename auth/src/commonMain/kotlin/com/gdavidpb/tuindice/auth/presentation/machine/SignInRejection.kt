package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn

/**
 * The verdict the screen keeps after the backend itself refused the attempt (wrong credentials,
 * disabled account, untrusted device), with [message] as the text to show for it. A failure on the
 * way to the backend (no connection, timeout, a service that asked for a wait, throttling) says
 * nothing about the credentials and is not a rejection: it has no verdict.
 */
internal fun SignInUseCaseError?.toRejection(message: String): SignIn.Rejection? {
	return when (this) {
		is SignInUseCaseError.InvalidCredentials -> SignIn.Rejection.InvalidCredentials(message)
		is SignInUseCaseError.AccountDisabled -> SignIn.Rejection.AccountDisabled(message)
		is SignInUseCaseError.Untrusted -> SignIn.Rejection.Untrusted(message)

		is SignInUseCaseError.Timeout,
		is SignInUseCaseError.AuthenticationFailed,
		is SignInUseCaseError.EmptyUsbId,
		is SignInUseCaseError.InvalidUsbId,
		is SignInUseCaseError.EmptyPassword,
		is SignInUseCaseError.Unavailable,
		is SignInUseCaseError.TooManyRequests,
		is SignInUseCaseError.OutdatedApp,
		is SignInUseCaseError.NoConnection,
		null -> null
	}
}
