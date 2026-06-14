package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.RemoveEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.RemoveEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RemoveEvaluationUseCaseTest {
	@Test
	fun removeEvaluationUseCase_whenEvaluationExists_removesOnlyThatEvaluation() = runTest {
		val evaluationRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(
				DEFAULT_PENDING_EVALUATION,
				DEFAULT_COMPLETED_EVALUATION
			)
		)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(DEFAULT_PENDING_EVALUATION.id).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(
			EvaluationRemove(id = DEFAULT_PENDING_EVALUATION.id),
			evaluationRepository.removeCalls.single()
		)
		assertNull(evaluationRepository.getEvaluation(DEFAULT_PENDING_EVALUATION.id))
		assertNotNull(evaluationRepository.getEvaluation(DEFAULT_COMPLETED_EVALUATION.id))
		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun removeEvaluationUseCase_whenEvaluationIsMissingRemotely_emitsNotFoundError() = runTest {
		val throwable = clientRequestException(
			statusCode = HttpStatusCode.NotFound,
			path = "/evaluations/v1/${DEFAULT_PENDING_EVALUATION.id}"
		)
		val evaluationRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			removeThrowable = throwable
		)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(DEFAULT_PENDING_EVALUATION.id).test {
			assertEquals(RemoveEvaluationUseCaseError.NotFound, awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(1, evaluationRepository.removeCalls.size)
		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	@Test
	fun removeEvaluationUseCase_whenFailureIsUnhandled_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage corrupted")
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				removeThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(DEFAULT_PENDING_EVALUATION.id).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	private fun createUseCase(
		evaluationRepository: RecordingEvaluationRepository,
		reportingRepository: RecordingReportingRepository
	) = RemoveEvaluationUseCase(
		evaluationRepository = evaluationRepository,
		reportingRepository = reportingRepository,
		exceptionHandler = RemoveEvaluationExceptionHandler()
	)
}
