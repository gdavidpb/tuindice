package com.gdavidpb.tuindice.pensum.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumSelectionParams
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectPensumSelectionUseCaseContractTest {
	@Test
	fun execute_whenSelectionSucceeds_thenPersistsYearAndModalityTogether() = runTest {
		val repository = RecordingPensumRepository()
		val useCase = createUseCase(repository = repository)

		useCase.execute(
			SelectPensumSelectionParams(
				year = 2019,
				modalityId = "degree_project"
			)
		).test {
			awaitLoadingThenData(this)

			awaitComplete()
		}

		assertEquals(listOf(2019 to "degree_project"), repository.selectedSelections)
		assertEquals(emptyList(), repository.selectedPensumYears)
		assertEquals(emptyList(), repository.selectedModalityIds)
	}

	@Test
	fun execute_whenSelectionTimesOut_thenEmitsTimeoutErrorAndPersistsNothing() = runTest {
		val repository = RecordingPensumRepository(
			selectionThrowable = IllegalStateException("Request timed out")
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(
			SelectPensumSelectionParams(
				year = 2019,
				modalityId = "degree_project"
			)
		).test {
			val error = awaitLoadingThenError(this)
			assertEquals(UpdatePensumUseCaseError.Timeout, error.error)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.selectedSelections)
	}

	private fun createUseCase(
		repository: RecordingPensumRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): SelectPensumSelectionUseCase {
		return SelectPensumSelectionUseCase(
			pensumRepository = repository,
			reportingRepository = reportingRepository,
			exceptionHandler = UpdatePensumExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)
	}
}
