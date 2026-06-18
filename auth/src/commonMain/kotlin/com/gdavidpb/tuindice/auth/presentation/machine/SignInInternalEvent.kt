package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError

/**
 * Internal machine inputs: results of the sign-in use case re-entering the loop.
 * User inputs are the contract [com.gdavidpb.tuindice.auth.presentation.contract.SignIn.Action]
 * classes themselves.
 */
sealed interface SignInInternalEvent {
	data object SignInSucceeded : SignInInternalEvent

	data object OutdatedAppResolved : SignInInternalEvent

	data class SignInFailed(
		val error: SignInUseCaseError?
	) : SignInInternalEvent
}
