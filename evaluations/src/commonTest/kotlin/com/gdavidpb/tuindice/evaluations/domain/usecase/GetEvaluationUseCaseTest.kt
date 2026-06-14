package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GetEvaluationUseCaseTest {
	@Test
	fun getEvaluationUseCase_whenEvaluationExists_emitsIt() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = listOf(
					DEFAULT_PENDING_EVALUATION,
					DEFAULT_COMPLETED_EVALUATION
				)
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(DEFAULT_COMPLETED_EVALUATION.id).test {
			assertEquals(DEFAULT_COMPLETED_EVALUATION, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun getEvaluationUseCase_whenEvaluationIsUnknown_emitsNullData() = runTest {
		val useCase = GetEvaluationUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION)
			),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute("unknown-evaluation").test {
			assertNull(awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationUseCase_whenRepositoryFails_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage corrupted")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				getEvaluationThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(DEFAULT_PENDING_EVALUATION.id).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}
}
