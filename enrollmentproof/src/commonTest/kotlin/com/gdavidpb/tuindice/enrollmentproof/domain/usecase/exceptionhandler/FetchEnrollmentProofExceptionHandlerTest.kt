package com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FetchEnrollmentProofExceptionHandlerTest {
	@Test
	fun reportException_whenNotFoundException_returnsNotFoundError() {
		val handler = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(EnrollmentProofNotFoundException())

		assertEquals(FetchEnrollmentProofUseCaseError.NotFound, error)
	}

	@Test
	fun reportException_whenUnsupportedOperation_returnsUnsupportedFileError() {
		val handler = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(UnsupportedOperationException("unsupported"))

		assertEquals(FetchEnrollmentProofUseCaseError.UnsupportedFile, error)
	}

	@Test
	fun reportException_whenTimeout_returnsTimeoutError() {
		val handler = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(FetchEnrollmentProofUseCaseError.Timeout, error)
	}

	@Test
	fun reportException_whenUnexpected_returnsNull() {
		val handler = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(IllegalStateException("unexpected"))

		assertNull(error)
	}
}

private class FakeNetworkStatusGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class FakeReportingGateway : ReportingGateway {
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
