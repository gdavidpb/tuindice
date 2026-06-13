package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
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

class GetAvailableAttemptsUseCaseTest {
	@Test
	fun getAvailableAttemptsUseCase_whenAttemptsExist_emitsThemInRepositoryOrder() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetAvailableAttemptsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				availableSubjects = listOf(
					DEFAULT_EVALUATION_SUBJECT,
					SECOND_EVALUATION_SUBJECT
				)
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				awaitLoadingThenData(this)
			)
			awaitComplete()
		}

		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun getAvailableAttemptsUseCase_whenNoAttemptsExist_emitsEmptyList() = runTest {
		val useCase = GetAvailableAttemptsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				availableSubjects = emptyList()
			),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertTrue(awaitLoadingThenData(this).isEmpty())
			awaitComplete()
		}
	}

	@Test
	fun getAvailableAttemptsUseCase_whenRepositoryFails_emitsNullErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage corrupted")
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetAvailableAttemptsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				availableAttemptsThrowable = throwable
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable, reportingRepository.exceptions.single())
	}
}
