package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.exception.NoSubjectsException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationsExceptionHandlerTest {
	@Test
	fun getEvaluationsExceptionHandler_mapsNoSubjects_andReportsHandled() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = NoSubjectsException()

		val actual = GetEvaluationsExceptionHandler(
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(EvaluationsUseCaseError.NoSubjects, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "GetEvaluationsExceptionHandler",
			throwable = throwable,
			isHandled = true
		)
	}

	@Test
	fun addEvaluationExceptionHandler_mapsValidationErrors() {
		val reportingRepository = RecordingReportingRepository()
		val throwable = AddEvaluationIllegalArgumentException(AddEvaluationUseCaseError.TypeMissed)

		val actual = AddEvaluationExceptionHandler(
			reportingRepository = reportingRepository
		).reportException(throwable)

		assertEquals(AddEvaluationUseCaseError.TypeMissed, actual)
		assertReported(
			reportingRepository = reportingRepository,
			handlerName = "AddEvaluationExceptionHandler",
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
