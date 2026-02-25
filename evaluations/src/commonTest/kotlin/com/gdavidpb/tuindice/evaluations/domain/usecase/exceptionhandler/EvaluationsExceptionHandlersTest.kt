package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.exception.NoSubjectsException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EvaluationsExceptionHandlersTest {
	@Test
	fun addEvaluationExceptionHandler_whenValidationError_returnsMappedError() {
		val handler = AddEvaluationExceptionHandler(
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(
			AddEvaluationIllegalArgumentException(AddEvaluationUseCaseError.SubjectMissed)
		)

		assertEquals(AddEvaluationUseCaseError.SubjectMissed, error)
	}

	@Test
	fun addEvaluationExceptionHandler_whenUnexpected_returnsNull() {
		val handler = AddEvaluationExceptionHandler(
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(IllegalStateException("unexpected"))

		assertNull(error)
	}

	@Test
	fun getEvaluationsExceptionHandler_whenNoSubjects_returnsNoSubjectsError() {
		val handler = GetEvaluationsExceptionHandler(
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(NoSubjectsException())

		assertEquals(EvaluationsUseCaseError.NoSubjects, error)
	}

	@Test
	fun getEvaluationsExceptionHandler_whenUnexpected_returnsNull() {
		val handler = GetEvaluationsExceptionHandler(
			reportingRepository = FakeReportingGateway()
		)

		val error = handler.reportException(IllegalStateException("unexpected"))

		assertNull(error)
	}
}

private class FakeReportingGateway : ReportingRepository {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
