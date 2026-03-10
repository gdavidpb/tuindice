package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.record.domain.exception.SubjectIllegalArgumentException
import com.gdavidpb.tuindice.record.domain.usecase.error.GetQuartersUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.GetQuartersExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RecordExceptionHandlerTest {
	@Test
	fun setSubjectGradeExceptionHandler_mapsValidationErrors_andReportsHandled() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = SubjectIllegalArgumentException(SubjectUseCaseError.OutOfRangeGrade)

		val actual = SetSubjectGradeExceptionHandler(
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(SubjectUseCaseError.OutOfRangeGrade, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "SetSubjectGradeExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun getQuartersExceptionHandler_mapsConflictToOutdatedPassword() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = clientRequestException(HttpStatusCode.Conflict, path = "/quarters/v1")

		val actual = GetQuartersExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = true),
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(GetQuartersUseCaseError.OutdatedPassword, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "GetQuartersExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun getQuartersExceptionHandler_mapsConnectionToNoConnection() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = IllegalStateException("network is unreachable")

		val actual = GetQuartersExceptionHandler(
			networkRepository = FakeNetworkRepository(isAvailable = false),
			reportingRepository = reportingRepository
		).reportException(throwable)

		val error = assertIs<GetQuartersUseCaseError.NoConnection>(actual)
		assertEquals(false, error.isNetworkAvailable)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "GetQuartersExceptionHandler",
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
