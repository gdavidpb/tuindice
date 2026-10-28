package com.gdavidpb.tuindice.evaluations.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.evaluations.domain.model.SelectedWeekPick
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationsSelectionRepository
import com.gdavidpb.tuindice.evaluations.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SetSelectedWeekUseCaseTest {
	private val pick = SelectedWeekPick(order = 1L, weekKey = "academic:2026-09-28:5")

	@Test
	fun setSelectedWeekUseCase_whenCollectorIsCancelledDuringTheSave_stillSavesThePick() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val repository = GatedSelectionRepository()
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationsSelectionRepository = repository,
			reportingRepository = reportingRepository,
			sessionCoroutineScope = testSessionCoroutineScope(dispatcher)
		)

		val collector = launch(dispatcher) {
			useCase.execute(pick).collect()
		}

		repository.saveStarted.await()
		collector.cancelAndJoin()
		repository.releaseSave.complete(Unit)
		advanceUntilIdle()

		assertEquals(listOf(pick), repository.savedPicks)
		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun setSelectedWeekUseCase_whenCollectorIsCancelledAndTheSaveFails_reportsTheFailure() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val throwable = IllegalStateException("storage is full")
		val repository = GatedSelectionRepository(failure = throwable)
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationsSelectionRepository = repository,
			reportingRepository = reportingRepository,
			sessionCoroutineScope = testSessionCoroutineScope(dispatcher)
		)

		val collector = launch(dispatcher) {
			useCase.execute(pick).collect()
		}

		repository.saveStarted.await()
		collector.cancelAndJoin()
		repository.releaseSave.complete(Unit)
		advanceUntilIdle()

		assertEquals(listOf(throwable.message), reportingRepository.exceptions.map { it.message })
	}

	@Test
	fun setSelectedWeekUseCase_whenTheSessionEndsDuringTheSave_reportsNothing() = runTest {
		val dispatcher = StandardTestDispatcher(testScheduler)
		val repository = GatedSelectionRepository()
		val reportingRepository = RecordingReportingRepository()
		val sessionCoroutineScope = testSessionCoroutineScope(dispatcher)
		val useCase = createUseCase(
			evaluationsSelectionRepository = repository,
			reportingRepository = reportingRepository,
			sessionCoroutineScope = sessionCoroutineScope
		)

		launch(dispatcher) {
			useCase.execute(pick).collect()
		}

		repository.saveStarted.await()
		sessionCoroutineScope.cancelActiveWork()
		advanceUntilIdle()

		assertTrue(repository.savedPicks.isEmpty())
		assertTrue(reportingRepository.exceptions.isEmpty())
	}

	@Test
	fun setSelectedWeekUseCase_whenTheSaveFails_emitsErrorAndReportsIt() = runTest {
		val throwable = IllegalStateException("storage is full")
		val reportingRepository = RecordingReportingRepository()
		val useCase = createUseCase(
			evaluationsSelectionRepository = FailingSelectionRepository(throwable),
			reportingRepository = reportingRepository,
			sessionCoroutineScope = testSessionCoroutineScope()
		)

		useCase.execute(pick).test {
			assertNull(awaitLoadingThenError(this).error)
			awaitComplete()
		}

		assertEquals(throwable.message, reportingRepository.exceptions.single().message)
	}

	private fun createUseCase(
		evaluationsSelectionRepository: EvaluationsSelectionRepository,
		reportingRepository: RecordingReportingRepository,
		sessionCoroutineScope: SessionCoroutineScope
	) = SetSelectedWeekUseCase(
		evaluationsSelectionRepository = evaluationsSelectionRepository,
		reportingRepository = reportingRepository,
		sessionCoroutineScope = sessionCoroutineScope
	)
}

// The save waits until the test releases it, so the collector can leave while the save is in flight.
private class GatedSelectionRepository(
	private val failure: Throwable? = null
) : EvaluationsSelectionRepository {
	val saveStarted = CompletableDeferred<Unit>()
	val releaseSave = CompletableDeferred<Unit>()
	val savedPicks = mutableListOf<SelectedWeekPick>()

	override fun observeSelectedWeekKey(): Flow<String?> = emptyFlow()

	override suspend fun saveSelectedWeekPick(pick: SelectedWeekPick) {
		saveStarted.complete(Unit)
		releaseSave.await()
		if (failure != null) throw failure
		savedPicks += pick
	}
}

private class FailingSelectionRepository(
	private val throwable: Throwable
) : EvaluationsSelectionRepository {
	override fun observeSelectedWeekKey(): Flow<String?> = emptyFlow()

	override suspend fun saveSelectedWeekPick(pick: SelectedWeekPick) {
		throw throwable
	}
}
