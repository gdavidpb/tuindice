package com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RecordExceptionHandlersTest {
	@Test
	fun setSubjectGradeExceptionHandler_whenValidationError_returnsMappedError() {
		val handler = SetSubjectGradeExceptionHandler(
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(
			SubjectIllegalArgumentException(SubjectUseCaseError.OutOfRangeGrade)
		)

		assertEquals(SubjectUseCaseError.OutOfRangeGrade, error)
	}

	@Test
	fun setSubjectGradeExceptionHandler_whenUnexpected_returnsNull() {
		val handler = SetSubjectGradeExceptionHandler(
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(IllegalStateException("unexpected"))

		assertNull(error)
	}

	@Test
	fun getQuartersExceptionHandler_whenTimeout_returnsTimeoutError() {
		val handler = GetQuartersExceptionHandler(
			networkRepository = FakeNetworkStatusGateway(),
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(timeoutThrowable())

		assertEquals(GetQuartersUseCaseError.Timeout, error)
	}

	@Test
	fun getQuartersExceptionHandler_whenUnexpected_returnsNull() {
		val handler = GetQuartersExceptionHandler(
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
