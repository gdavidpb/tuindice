package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdateEvaluationsUseCaseTest {
	@Test
	fun updateEvaluationsUseCase_whenRefreshSucceeds_requestsSingleRepositoryRefresh() = runTest {
		val evaluationRepository = RecordingEvaluationRepository()
		val reportingRepository = RecordingReportingRepository()
		val useCase = UpdateEvaluationsUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertEquals(Unit, awaitLoadingThenData(this))
			awaitComplete()
		}

		assertEquals(1, evaluationRepository.updateEvaluationsCalls)
		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun updateEvaluationsUseCase_whenRefreshFails_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("refresh failed")
		val evaluationRepository = RecordingEvaluationRepository(
			refreshThrowable = throwable
		)
		val reportingRepository = RecordingReportingRepository()
		val useCase = UpdateEvaluationsUseCase(
			evaluationRepository = evaluationRepository,
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(1, evaluationRepository.updateEvaluationsCalls)
		assertEquals(throwable, reportingRepository.exceptions.single())
	}
}
