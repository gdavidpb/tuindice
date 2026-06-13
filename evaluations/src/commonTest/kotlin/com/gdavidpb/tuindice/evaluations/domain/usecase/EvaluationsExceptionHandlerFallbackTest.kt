package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.UpdateEvaluationExceptionHandler
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertNull

class EvaluationsExceptionHandlerFallbackTest {
	@Test
	fun addEvaluationExceptionHandler_whenThrowableIsUnrelated_returnsNull() {
		assertNull(AddEvaluationExceptionHandler().parseException(IllegalStateException("boom")))
	}

	@Test
	fun addEvaluationExceptionHandler_whenStatusIsNotPreconditionFailed_returnsNull() {
		val throwable = clientRequestException(HttpStatusCode.NotFound, path = "/evaluations/v1")

		assertNull(AddEvaluationExceptionHandler().parseException(throwable))
	}

	@Test
	fun updateEvaluationExceptionHandler_whenThrowableIsUnrelated_returnsNull() {
		assertNull(UpdateEvaluationExceptionHandler().parseException(IllegalStateException("boom")))
	}

	@Test
	fun updateEvaluationExceptionHandler_whenStatusIsNotNotFound_returnsNull() {
		val throwable = clientRequestException(
			HttpStatusCode.PreconditionFailed,
			path = "/evaluations/v1/eid"
		)

		assertNull(UpdateEvaluationExceptionHandler().parseException(throwable))
	}

	@Test
	fun removeEvaluationExceptionHandler_whenThrowableIsUnrelated_returnsNull() {
		assertNull(RemoveEvaluationExceptionHandler().parseException(IllegalStateException("boom")))
	}

	@Test
	fun removeEvaluationExceptionHandler_whenStatusIsNotNotFound_returnsNull() {
		val throwable = clientRequestException(
			HttpStatusCode.PreconditionFailed,
			path = "/evaluations/v1/eid"
		)

		assertNull(RemoveEvaluationExceptionHandler().parseException(throwable))
	}
}
