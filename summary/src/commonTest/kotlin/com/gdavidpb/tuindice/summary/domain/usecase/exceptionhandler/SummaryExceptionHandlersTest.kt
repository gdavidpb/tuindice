package com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.summary.domain.exception.ProfilePictureIllegalArgumentException
import com.gdavidpb.tuindice.summary.domain.usecase.error.GetUserUseCaseError
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SummaryExceptionHandlersTest {
	@Test
	fun uploadProfilePictureExceptionHandler_whenValidationError_returnsMappedError() {
		val handler = UploadProfilePictureExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(
			ProfilePictureIllegalArgumentException(ProfilePictureUseCaseError.InvalidSource)
		)

		assertEquals(ProfilePictureUseCaseError.InvalidSource, error)
	}

	@Test
	fun uploadProfilePictureExceptionHandler_whenTimeout_returnsTimeoutError() {
		val handler = UploadProfilePictureExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(ProfilePictureUseCaseError.Timeout, error)
	}

	@Test
	fun removeProfilePictureExceptionHandler_whenTimeout_returnsTimeoutError() {
		val handler = RemoveProfilePictureExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(ProfilePictureUseCaseError.Timeout, error)
	}

	@Test
	fun getUserExceptionHandler_whenTimeout_returnsTimeoutError() {
		val handler = GetUserExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(GetUserUseCaseError.Timeout, error)
	}

	@Test
	fun getUserExceptionHandler_whenUnexpected_returnsNull() {
		val handler = GetUserExceptionHandler(
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
