package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.evaluations.domain.exception.AddEvaluationIllegalArgumentException
import com.gdavidpb.tuindice.evaluations.domain.exception.NoSubjectsException
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.EvaluationsUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.RemoveEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.GetEvaluationsExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluationsExceptionHandlerTest {
	@Test
	fun getEvaluationsExceptionHandler_mapsNoSubjects() {
		val throwable = NoSubjectsException()

		val actual = GetEvaluationsExceptionHandler().parseException(throwable)

		assertEquals(EvaluationsUseCaseError.NoSubjects, actual)
	}

	@Test
	fun addEvaluationExceptionHandler_mapsValidationErrors() {
		val throwable = AddEvaluationIllegalArgumentException(AddEvaluationUseCaseError.TypeMissed)

		val actual = AddEvaluationExceptionHandler().parseException(throwable)

		assertEquals(AddEvaluationUseCaseError.TypeMissed, actual)
	}

	@Test
	fun addEvaluationExceptionHandler_mapsAlreadyExists() {
		val throwable = clientRequestException(HttpStatusCode.PreconditionFailed, path = "/evaluations/v1")

		val actual = AddEvaluationExceptionHandler().parseException(throwable)

		assertEquals(AddEvaluationUseCaseError.AlreadyExists, actual)
	}

	@Test
	fun updateEvaluationExceptionHandler_mapsNotFound() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/evaluations/v1/eid")

		val actual = UpdateEvaluationExceptionHandler().parseException(throwable)

		assertEquals(UpdateEvaluationUseCaseError.NotFound, actual)
	}

	@Test
	fun removeEvaluationExceptionHandler_mapsNotFound() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/evaluations/v1/eid")

		val actual = RemoveEvaluationExceptionHandler().parseException(throwable)

		assertEquals(RemoveEvaluationUseCaseError.NotFound, actual)
	}
}
