package com.gdavidpb.tuindice.pensum.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.exceptionhandler.UpdatePensumExceptionHandler
import com.gdavidpb.tuindice.pensum.testing.RecordingPensumRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import com.gdavidpb.tuindice.testkit.ktor.serverResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdatePensumUseCaseContractTest {
	@Test
	fun execute_whenRefreshSucceeds_thenEmitsLoadingThenData() = runTest {
		val repository = RecordingPensumRepository()
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			awaitLoadingThenData(this)

			awaitComplete()
		}

		assertEquals(1, repository.refreshCalls)
	}

	@Test
	fun execute_whenRefreshFailsWithNotFound_thenEmitsNotFoundError() = runTest {
		val repository = RecordingPensumRepository(
			refreshThrowable = clientRequestException(HttpStatusCode.NotFound)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(UpdatePensumUseCaseError.NotFound, error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenRefreshFailsWithServiceUnavailable_thenEmitsUnavailableError() = runTest {
		val repository = RecordingPensumRepository(
			refreshThrowable = serverResponseException(HttpStatusCode.ServiceUnavailable)
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(UpdatePensumUseCaseError.Unavailable, error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenRefreshTimesOut_thenEmitsTimeoutError() = runTest {
		val repository = RecordingPensumRepository(
			refreshThrowable = IllegalStateException("Request timed out")
		)
		val useCase = createUseCase(repository = repository)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(UpdatePensumUseCaseError.Timeout, error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenConnectionFailsWithoutNetwork_thenEmitsNoConnectionWithNetworkUnavailable() = runTest {
		val repository = RecordingPensumRepository(
			refreshThrowable = IllegalStateException("Could not connect to the server")
		)
		val useCase = createUseCase(
			repository = repository,
			networkRepository = FakeNetworkRepository(isAvailable = false)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			val noConnection = assertIs<UpdatePensumUseCaseError.NoConnection>(error.error)
			assertEquals(false, noConnection.isNetworkAvailable)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenConnectionFailsWithNetwork_thenEmitsNoConnectionWithNetworkAvailable() = runTest {
		val repository = RecordingPensumRepository(
			refreshThrowable = IllegalStateException("Could not connect to the server")
		)
		val useCase = createUseCase(
			repository = repository,
			networkRepository = FakeNetworkRepository(isAvailable = true)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			val noConnection = assertIs<UpdatePensumUseCaseError.NoConnection>(error.error)
			assertEquals(true, noConnection.isNetworkAvailable)

			awaitComplete()
		}
	}

	@Test
	fun execute_whenExceptionCannotBeMapped_thenEmitsUnhandledErrorAndReportsException() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val repository = RecordingPensumRepository(
			refreshThrowable = IllegalArgumentException("boom")
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
	fun execute_whenHandledErrorIsEmitted_thenStillReportsHandledException() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val repository = RecordingPensumRepository(
			refreshThrowable = clientRequestException(HttpStatusCode.NotFound)
		)
		val useCase = createUseCase(
			repository = repository,
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			awaitLoadingThenError(this)

			awaitComplete()
		}

		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(true, reportingRepository.customKeys["is-handled"])
	}

	private fun createUseCase(
		repository: RecordingPensumRepository,
		networkRepository: FakeNetworkRepository = FakeNetworkRepository(isAvailable = true),
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): UpdatePensumUseCase {
		return UpdatePensumUseCase(
			pensumRepository = repository,
			reportingRepository = reportingRepository,
			exceptionHandler = UpdatePensumExceptionHandler(
				networkRepository = networkRepository
			)
		)
	}
}
