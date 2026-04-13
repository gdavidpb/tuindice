package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthExceptionHandlerTest {
	@Test
	fun signInExceptionHandler_mapsUnauthorized() {
		val throwable = clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v1/token")

		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(SignInUseCaseError.AuthenticationFailed, actual)
	}

	@Test
	fun signInExceptionHandler_mapsAccountDisabled_fromLocked() {
		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(
			clientRequestException(HttpStatusCode.Locked, path = "/auth/v1/token")
		)

		assertEquals(SignInUseCaseError.AccountDisabled, actual)
	}

	@Test
	fun signInExceptionHandler_mapsUntrusted_fromForbidden() {
		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(
			clientRequestException(HttpStatusCode.Forbidden, path = "/auth/v1/token")
		)

		assertEquals(SignInUseCaseError.Untrusted, actual)
	}

	@Test
	fun signInExceptionHandler_mapsUnavailable_fromTooManyRequests() {
		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(
			clientRequestException(HttpStatusCode.TooManyRequests, path = "/auth/v1/token")
		)

		assertEquals(SignInUseCaseError.Unavailable, actual)
	}

	@Test
	fun signInExceptionHandler_mapsConnectionState_usingNetworkAvailability() {
		val throwable = IllegalStateException("network is unreachable")

		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false)
		).parseException(throwable)

		val error = assertIs<SignInUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
	}

	@Test
	fun updatePasswordExceptionHandler_mapsAccountDisabled_fromLocked() {
		val actual = UpdatePasswordExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(
			clientRequestException(HttpStatusCode.Locked, path = "/auth/v1/token")
		)

		assertEquals(SignInUseCaseError.AccountDisabled, actual)
	}

	@Test
	fun updatePasswordExceptionHandler_mapsUnavailable_fromTooManyRequests() {
		val actual = UpdatePasswordExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(
			clientRequestException(HttpStatusCode.TooManyRequests, path = "/auth/v1/token")
		)

		assertEquals(SignInUseCaseError.Unavailable, actual)
	}

	@Test
	fun updatePasswordExceptionHandler_mapsValidationErrors() {
		val throwable = SignInIllegalArgumentException(SignInUseCaseError.EmptyPassword)

		val actual = UpdatePasswordExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true)
		).parseException(throwable)

		assertEquals(SignInUseCaseError.EmptyPassword, actual)
	}
}
