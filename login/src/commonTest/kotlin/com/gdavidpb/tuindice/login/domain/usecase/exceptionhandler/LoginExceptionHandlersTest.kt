package com.gdavidpb.tuindice.login.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.login.domain.exception.SignInIllegalArgumentException
import com.gdavidpb.tuindice.login.domain.usecase.error.SignInUseCaseError
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoginExceptionHandlersTest {
	@Test
	fun signInExceptionHandler_whenValidationError_returnsMappedError() {
		val handler = SignInExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(
			SignInIllegalArgumentException(SignInUseCaseError.InvalidUsbId)
		)

		assertEquals(SignInUseCaseError.InvalidUsbId, error)
	}

	@Test
	fun signInExceptionHandler_whenTimeout_returnsTimeoutError() {
		val handler = SignInExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(SignInUseCaseError.Timeout, error)
	}

	@Test
	fun signInExceptionHandler_whenUnexpected_returnsNull() {
		val handler = SignInExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(IllegalStateException("unexpected"))

		assertNull(error)
	}

	@Test
	fun updatePasswordExceptionHandler_whenTimeout_returnsTimeoutError() {
		val handler = UpdatePasswordExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(SignInUseCaseError.Timeout, error)
	}

	@Test
	fun updatePasswordExceptionHandler_whenUnexpected_returnsNull() {
		val handler = UpdatePasswordExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(IllegalStateException("unexpected"))

		assertNull(error)
	}
}

private class FakeNetworkStatusGateway : NetworkRepository {
	override fun isAvailable(): Boolean = true
}

private class FakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun timeoutThrowable(): Throwable = runBlocking {
	val throwable = runCatching {
		withTimeout(1) {
			delay(5)
		}
	}.exceptionOrNull()

	checkNotNull(throwable)
}
