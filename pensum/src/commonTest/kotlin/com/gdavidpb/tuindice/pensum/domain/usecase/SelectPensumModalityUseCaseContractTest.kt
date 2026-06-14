package com.gdavidpb.tuindice.pensum.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SelectPensumModalityUseCaseContractTest {
	@Test
	fun execute_whenSelectionSucceeds_thenPersistsModalityAndEmitsData() = runTest {
		val repository = RecordingPensumRepository()
		val useCase = createUseCase(repository = repository)

		useCase.execute("long_internship").test {
			awaitLoadingThenData(this)

			awaitComplete()
		}

		assertEquals(listOf("long_internship"), repository.selectedModalityIds)
		assertEquals(emptyList(), repository.selectedPensumYears)
		assertEquals(emptyList(), repository.selectedSelections)
	}

	@Test
	fun execute_whenSelectionFailsWithConnectionIssue_thenEmitsNoConnectionError() = runTest {
		val repository = RecordingPensumRepository(
			selectionThrowable = IllegalStateException("Could not connect to the server")
		)
		val useCase = createUseCase(
			repository = repository,
			networkRepository = FakeNetworkRepository(isAvailable = false)
		)

		useCase.execute("degree_project").test {
			val error = awaitLoadingThenError(this)
			val noConnection = assertIs<UpdatePensumUseCaseError.NoConnection>(error.error)
			assertEquals(false, noConnection.isNetworkAvailable)

			awaitComplete()
		}

		assertEquals(emptyList(), repository.selectedModalityIds)
	}

	private fun createUseCase(
		repository: RecordingPensumRepository,
		networkRepository: FakeNetworkRepository = FakeNetworkRepository(isAvailable = true),
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): SelectPensumModalityUseCase {
		return SelectPensumModalityUseCase(
			pensumRepository = repository,
			reportingRepository = reportingRepository,
			exceptionHandler = UpdatePensumExceptionHandler(
				networkRepository = networkRepository
			)
		)
	}
}
