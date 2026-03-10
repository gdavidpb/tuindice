package com.gdavidpb.tuindice.auth.domain.usecase

import com.gdavidpb.tuindice.auth.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.auth.domain.usecase.error.SignInUseCaseError
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.SignInExceptionHandler
import com.gdavidpb.tuindice.auth.domain.usecase.exceptionhandler.UpdatePasswordExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AuthExceptionHandlerTest {
	@Test
	fun signInExceptionHandler_mapsUnauthorized_andReportsHandled() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = clientRequestException(HttpStatusCode.Unauthorized, path = "/auth/v1/token")

		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(SignInUseCaseError.InvalidCredentials, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "SignInExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun signInExceptionHandler_mapsConnectionState_usingNetworkAvailability() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("network is unreachable")

		val actual = SignInExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false),
			reportingRepository = reportingRepository
		).reportException(throwable)

		val error = assertIs<SignInUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "SignInExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun updatePasswordExceptionHandler_mapsValidationErrors() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = SignInIllegalArgumentException(SignInUseCaseError.EmptyPassword)

		val actual = UpdatePasswordExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(SignInUseCaseError.EmptyPassword, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "UpdatePasswordExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	private fun assertReported(
		reportingRepository: RecordingReportingRepository,
		handlerName: String,
		throwable: Throwable,
		isHandled: Boolean
	) {
		assertEquals(listOf(throwable), reportingRepository.loggedExceptions)
		assertEquals(handlerName, reportingRepository.customKeys["useCase"])
		assertEquals(isHandled, reportingRepository.customKeys["isHandled"])
	}
}
