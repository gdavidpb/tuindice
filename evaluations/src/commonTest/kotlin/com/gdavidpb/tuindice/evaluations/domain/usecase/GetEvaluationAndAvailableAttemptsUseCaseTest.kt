package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetEvaluationAndAvailableAttemptsUseCaseTest {
	@Test
	fun getEvaluationAndAvailableAttemptsUseCase_whenEvaluationIsUnknown_emitsNullEvaluationWithAttempts() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationAndAvailableAttemptsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				availableSubjects = listOf(
					DEFAULT_EVALUATION_SUBJECT,
					SECOND_EVALUATION_SUBJECT
				)
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(GetEvaluationParams(evaluationId = "unknown-evaluation")).test {
			val result = awaitLoadingThenData(this)
			assertNull(result.evaluation)
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				result.availableAttempts
			)
			awaitComplete()
		}

		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun getEvaluationAndAvailableAttemptsUseCase_whenEvaluationLookupFails_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage corrupted")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationAndAvailableAttemptsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				getEvaluationThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(GetEvaluationParams(evaluationId = DEFAULT_PENDING_EVALUATION.id)).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}

	@Test
	fun getEvaluationAndAvailableAttemptsUseCase_whenAttemptsLookupFails_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("attempts unavailable")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationAndAvailableAttemptsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
				availableAttemptsThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(GetEvaluationParams(evaluationId = DEFAULT_PENDING_EVALUATION.id)).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}
}
