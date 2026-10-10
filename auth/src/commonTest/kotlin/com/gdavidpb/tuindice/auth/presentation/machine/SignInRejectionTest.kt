package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.presentation.contract.SignIn
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInRejectionTest {
	private val message = "the text for it"

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
	fun eachBackendVerdictOnTheAccount_becomesItsOwnRejection_withTheMessage() {
		assertEquals(
			SignIn.Rejection.InvalidCredentials(message),
			SignInUseCaseError.InvalidCredentials.toRejection(message)
		)
		assertEquals(
			SignIn.Rejection.AccountDisabled(message),
			SignInUseCaseError.AccountDisabled.toRejection(message)
		)
		assertEquals(
			SignIn.Rejection.Untrusted(message),
			SignInUseCaseError.Untrusted.toRejection(message)
		)
	}

	@Test
	fun failuresOnTheWayToTheBackend_haveNoVerdict() {
		notRejected.forEach { error ->
			assertEquals(null, error.toRejection(message), "$error")
		}

		assertEquals(null, (null as SignInUseCaseError?).toRejection(message))
	}
}
