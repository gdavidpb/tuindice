package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationsRefreshResult
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class EnsureEvaluationsLoadedUseCaseTest {
	@Test
	fun execute_whenLocalContentExists_emitsCachedThenRefreshLifecycle() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			refreshedEvaluations = listOf(DEFAULT_PENDING_EVALUATION),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			assertEquals(
				EnsureEvaluationsLoadedUseCase.Result.Cached,
				awaitLoadingThenData(this)
			)
			assertEquals(
				EnsureEvaluationsLoadedUseCase.Result.RefreshStarted,
				assertIs<UseCaseState.Data<EnsureEvaluationsLoadedUseCase.Result>>(
					awaitItem()
				).value
			)
			val succeeded = assertIs<EnsureEvaluationsLoadedUseCase.Result.RefreshSucceeded>(
				assertIs<UseCaseState.Data<EnsureEvaluationsLoadedUseCase.Result>>(
					awaitItem()
				).value
			)
			assertTrue(succeeded.refreshResult.hasEvaluations)
			assertTrue(succeeded.refreshResult.hasAvailableAttempts)
			awaitComplete()
		}

		assertEquals(1, repository.updateEvaluationsCalls)
		assertEquals(listOf(false), repository.updateEvaluationsForceRemoteCalls)
	}

	@Test
	fun execute_whenLocalContentIsMissing_forcesRemoteRefreshWithoutCachedEmission() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = emptyList(),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			assertEquals(
				EnsureEvaluationsLoadedUseCase.Result.RefreshStarted,
				awaitLoadingThenData(this)
			)
			val succeeded = assertIs<EnsureEvaluationsLoadedUseCase.Result.RefreshSucceeded>(
				assertIs<UseCaseState.Data<EnsureEvaluationsLoadedUseCase.Result>>(
					awaitItem()
				).value
			)
			assertEquals(false, succeeded.refreshResult.hasEvaluations)
			assertTrue(succeeded.refreshResult.hasAvailableAttempts)
			awaitComplete()
		}

		assertEquals(1, repository.updateEvaluationsCalls)
		assertEquals(listOf(true), repository.updateEvaluationsForceRemoteCalls)
	}

	@Test
	fun execute_whenRefreshFindsRemoteContentBeforeLocalObservation_emitsRefreshResultWithContent() = runTest {
		val repository = RecordingEvaluationRepository(
			initialEvaluations = emptyList(),
			refreshedEvaluations = null,
			refreshResult = EvaluationsRefreshResult(
				hasEvaluations = true,
				hasAvailableAttempts = true
			),
			availableSubjects = listOf(DEFAULT_EVALUATION_SUBJECT)
		)
		val useCase = createUseCase(repository)

		useCase.execute(Unit).test {
			assertEquals(
				EnsureEvaluationsLoadedUseCase.Result.RefreshStarted,
				awaitLoadingThenData(this)
			)
			val succeeded = assertIs<EnsureEvaluationsLoadedUseCase.Result.RefreshSucceeded>(
				assertIs<UseCaseState.Data<EnsureEvaluationsLoadedUseCase.Result>>(
					awaitItem()
				).value
			)
			assertTrue(succeeded.refreshResult.hasEvaluations)
			assertTrue(succeeded.refreshResult.hasAvailableAttempts)
			awaitComplete()
		}

		assertEquals(1, repository.updateEvaluationsCalls)
		assertEquals(listOf(true), repository.updateEvaluationsForceRemoteCalls)
	}

	private fun createUseCase(
		repository: RecordingEvaluationRepository
	): EnsureEvaluationsLoadedUseCase {
		return EnsureEvaluationsLoadedUseCase(
			evaluationRepository = repository,
			reportingRepository = RecordingReportingRepository()
		)
	}
}
