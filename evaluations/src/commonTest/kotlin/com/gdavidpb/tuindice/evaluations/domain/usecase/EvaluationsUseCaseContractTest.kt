package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.RecordDataPrerequisiteState
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsNoAttemptsReason
import com.gdavidpb.tuindice.evaluations.domain.model.GetEvaluations
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.GetEvaluationParams
import com.gdavidpb.tuindice.evaluations.testing.*
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationsUseCaseContractTest {
	@Test
	fun getEvaluationsUseCase_emitsSortedEvaluations() = runTest {
		val currentTime = currentTimeMillis()
		val futureEvaluation = DEFAULT_PENDING_EVALUATION.copy(
			date = currentTime + ONE_DAY_MILLIS
		)
		val pastEvaluation = DEFAULT_COMPLETED_EVALUATION.copy(
			date = currentTime - ONE_DAY_MILLIS
		)
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(
					listOf(
						futureEvaluation,
						pastEvaluation
					)
				),
				initialEvaluations = listOf(
					futureEvaluation,
					pastEvaluation
				)
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			val result = awaitLoadingThenData(this) as GetEvaluations.Content
			assertEquals(
				listOf(pastEvaluation, futureEvaluation),
				result.evaluations
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_returnsNoAttemptsState_whenFeatureHasNoAttempts() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.NoCurrentTerm),
				awaitLoadingThenData(this)
			)
			assertTrue(reportingRepository.exceptions.isEmpty())
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_returnsEnrollmentUnavailableNoAttemptsState_whenEnrollmentSyncFailed() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(),
			syncStatusRepository = RecordingSyncStatusRepository(
				initialReport = SyncReport.partialEnrollmentUnavailable()
			),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(
				GetEvaluations.NoAttempts(EvaluationsNoAttemptsReason.EnrollmentUnavailable),
				awaitLoadingThenData(this)
			)
			cancelAndIgnoreRemainingEvents()
		}
	}

	@Test
	fun getEvaluationsUseCase_waitsForRecordData_beforeCheckingAttempts() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(
				evaluationsFlow = flowOf(emptyList()),
				initialEvaluations = emptyList(),
				availableSubjects = emptyList()
			),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
				states = flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = false))
			),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(GetEvaluations.WaitingForRecordData, awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationsUseCase_reportsRecordUnavailable_whenPrerequisiteFailed() = runTest {
		val useCase = GetEvaluationsUseCase(
			evaluationRepository = RecordingEvaluationRepository(),
			recordDataPrerequisiteRepository = ReadyRecordDataPrerequisiteRepository(
				states = flowOf(RecordDataPrerequisiteState(isReady = false, hasFailed = true))
			),
			syncStatusRepository = RecordingSyncStatusRepository(),
			evaluationsSelectionRepository = InMemoryEvaluationsSelectionRepository(),
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(Unit).test {
			assertEquals(GetEvaluations.RecordDataUnavailable, awaitLoadingThenData(this))
			awaitComplete()
		}
	}

	@Test
	fun getEvaluationAndAvailableAttemptsUseCase_emitsEvaluationAndAttempts() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT)
		)
		val useCase = GetEvaluationAndAvailableAttemptsUseCase(
			evaluationRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)

		useCase.execute(GetEvaluationParams(DEFAULT_PENDING_EVALUATION.id)).test {
			val result = awaitLoadingThenData(this)
			assertEquals(DEFAULT_PENDING_EVALUATION, result.evaluation)
			assertEquals(
				listOf(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
				result.availableAttempts
			)
			awaitComplete()
		}
	}
}

private const val ONE_DAY_MILLIS = 86_400_000L
