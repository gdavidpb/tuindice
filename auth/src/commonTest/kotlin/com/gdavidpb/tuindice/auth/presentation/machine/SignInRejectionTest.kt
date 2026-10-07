package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInRejectionTest {
	private val rejected = listOf(
		SignInUseCaseError.InvalidCredentials,
		SignInUseCaseError.AccountDisabled,
		SignInUseCaseError.Untrusted
	)

	private val notRejected = listOf(
		SignInUseCaseError.Timeout,
		SignInUseCaseError.AuthenticationFailed,
		SignInUseCaseError.EmptyUsbId,
		SignInUseCaseError.InvalidUsbId,
		SignInUseCaseError.EmptyPassword,
		SignInUseCaseError.Unavailable(),
		SignInUseCaseError.Unavailable(retryAfterMillis = 30_000L),
		SignInUseCaseError.TooManyRequests,
		SignInUseCaseError.OutdatedApp,
		SignInUseCaseError.NoConnection(isNetworkAvailable = true),
		SignInUseCaseError.NoConnection(isNetworkAvailable = false)
	)

	@Test
	fun theBackendVerdictsOnTheAccount_areRejections() {
		rejected.forEach { error ->
			assertEquals(true, error.isRejectedByBackend(), "$error")
		}
	}

	@Test
	fun failuresOnTheWayToTheBackend_areNotRejections() {
		notRejected.forEach { error ->
			assertEquals(false, error.isRejectedByBackend(), "$error")
		}
	}
}
