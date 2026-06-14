package com.gdavidpb.tuindice.pensum.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumParams
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SelectPensumUseCaseContractTest {
	@Test
	fun execute_whenSelectionSucceeds_thenPersistsYearAndEmitsData() = runTest {
		val repository = RecordingPensumRepository()
		val useCase = createUseCase(repository = repository)

		useCase.execute(SelectPensumParams(year = 2019)).test {
			awaitLoadingThenData(this)

			awaitComplete()
		}

		assertEquals(listOf(2019), repository.selectedPensumYears)
		assertEquals(emptyList(), repository.selectedModalityIds)
		assertEquals(emptyList(), repository.selectedSelections)
	}

	@Test
	fun execute_whenCalledTwice_thenPersistsEachSelectedYear() = runTest {
		val repository = RecordingPensumRepository()
		val useCase = createUseCase(repository = repository)

		useCase.execute(SelectPensumParams(year = 2016)).test {
			awaitLoadingThenData(this)
			awaitComplete()
		}
		useCase.execute(SelectPensumParams(year = 2019)).test {
			awaitLoadingThenData(this)
			awaitComplete()
		}

		assertEquals(listOf(2016, 2019), repository.selectedPensumYears)
	}

	@Test
	fun execute_whenSelectionFails_thenEmitsMappedErrorAndPersistsNothing() = runTest {
		val repository = RecordingPensumRepository(
			selectionThrowable = clientRequestException(HttpStatusCode.NotFound)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(SelectPensumParams(year = 2019)).test {
			val error = awaitLoadingThenError(this)
			assertEquals(UpdatePensumUseCaseError.NotFound, error.error)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.selectedPensumYears)
	}

	private fun createUseCase(
		repository: RecordingPensumRepository,
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): SelectPensumUseCase {
		return SelectPensumUseCase(
			pensumRepository = repository,
			reportingRepository = reportingRepository,
			exceptionHandler = UpdatePensumExceptionHandler(
				networkRepository = FakeNetworkRepository(isAvailable = true)
			)
		)
	}
}
