package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchEnrollmentProofExceptionHandlerTest {
	@Test
	fun fetchEnrollmentProofExceptionHandler_mapsUnsupportedFiles() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = UnsupportedOperationException("Unsupported file")

		val actual = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(FetchEnrollmentProofUseCaseError.UnsupportedFile, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "FetchEnrollmentProofExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun fetchEnrollmentProofExceptionHandler_mapsNotFoundStatus() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/enrollment-proof")

		val actual = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(FetchEnrollmentProofUseCaseError.NotFound, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "FetchEnrollmentProofExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun fetchEnrollmentProofExceptionHandler_mapsConnectionToNoConnection() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("network is unreachable")

		val actual = FetchEnrollmentProofExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false),
			reportingRepository = reportingRepository
		).reportException(throwable)

		val error = assertIs<FetchEnrollmentProofUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "FetchEnrollmentProofExceptionHandler",
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
