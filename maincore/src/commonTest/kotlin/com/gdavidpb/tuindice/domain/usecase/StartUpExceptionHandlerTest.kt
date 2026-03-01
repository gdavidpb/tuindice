package com.gdavidpb.tuindice.domain.usecase

import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class StartUpExceptionHandlerTest {
	@Test
	fun startupExceptionHandler_returnsNoServices_forGooglePlayServicesErrors() {
		val applicationRepository = RecordingApplicationRepository()
		val reportingRepository = RecordingReportingRepository()
		val throwable = GooglePlayServicesNotAvailableException()

		val actual = StartUpExceptionHandler(
			applicationRepository = applicationRepository,
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(StartUpUseCaseError.NoServices, actual)
		assertFalse(applicationRepository.cleared)
		assertEquals(0, applicationRepository.clearCalls)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "StartUpExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun startupExceptionHandler_reportsUnhandledExceptions_withoutMaskingThem() {
		val applicationRepository = RecordingApplicationRepository()
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("boom")

		val actual = StartUpExceptionHandler(
			applicationRepository = applicationRepository,
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertNull(actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "StartUpExceptionHandler",
			throwable = throwable,
			isHandled = false
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

	private class GooglePlayServicesNotAvailableException : RuntimeException()
}
