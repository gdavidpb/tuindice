package com.gdavidpb.tuindice.pensum.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.pensum.domain.model.PensumObservation
import com.gdavidpb.tuindice.pensum.domain.repository.PensumSelectionRepository
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.pensum.testing.sampleObservedPensum
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObservePensumUseCaseContractTest {
	@Test
	fun execute_whenRepositoryEmitsObservations_thenEmitsLoadingThenEachObservation() = runTest {
		val content = PensumObservation.Content(pensum = sampleObservedPensum())
		val repository = RecordingPensumRepository(
			observations = listOf(
				PensumObservation.WaitingForRecordData,
				content
			)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			val first = awaitLoadingThenData(this)
			assertEquals(PensumObservation.WaitingForRecordData, first)

			val second = assertIs<UseCaseState.Data<PensumObservation>>(awaitItem())
			assertEquals(content, second.value)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenRepositoryReportsMissingPensum_thenEmitsMissingObservation() = runTest {
		val repository = RecordingPensumRepository(
			observations = listOf(PensumObservation.Missing)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			val observation = awaitLoadingThenData(this)
			assertEquals(PensumObservation.Missing, observation)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenObservationFlowFails_thenEmitsUnhandledErrorAndReportsException() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val repository = RecordingPensumRepository(
			observeThrowable = IllegalStateException("boom")
		)
		val useCase = createUseCase(
			repository = repository,
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}

		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(false, reportingRepository.customKeys["is-handled"])
		assertTrue(reportingRepository.customKeys.containsKey("use-case"))
	}

	@Test
	fun execute_whenFlowFailsAfterEmitting_thenEmitsDataBeforeError() = runTest {
		val repository = RecordingPensumRepository(
			observations = listOf(PensumObservation.RecordDataUnavailable),
			observeThrowable = IllegalStateException("boom")
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			val observation = awaitLoadingThenData(this)
			assertEquals(PensumObservation.RecordDataUnavailable, observation)

			val error = assertIs<UseCaseState.Error<*>>(awaitItem())
			assertNull(error.error)

			awaitComplete()
		}
	}

	private fun createUseCase(
		repository: RecordingPensumRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): ObservePensumUseCase {
		return ObservePensumUseCase(
			pensumRepository = repository,
			pensumSelectionRepository = ExpandedPensumSelectionRepository(),
			reportingRepository = reportingRepository
		)
	}
}

private class ExpandedPensumSelectionRepository : PensumSelectionRepository {
	override fun observeSummaryCollapsed(): Flow<Boolean> = flowOf(false)

	override suspend fun setSummaryCollapsed(isCollapsed: Boolean) = Unit
}
