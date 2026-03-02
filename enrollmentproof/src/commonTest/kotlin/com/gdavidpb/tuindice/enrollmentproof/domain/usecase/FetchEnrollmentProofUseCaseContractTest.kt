package com.gdavidpb.tuindice.enrollmentproof.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.error.FetchEnrollmentProofUseCaseError
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF_SOURCE
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeFileRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.clientRequestException
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenData
import com.gdavidpb.tuindice.testkit.domain.awaitLoadingThenError
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FetchEnrollmentProofUseCaseContractTest {
	@Test
	fun execute_emitsLoadingThenFileWhenEnrollmentProofCanBeOpened() = runTest {
		val fileRepository = FakeFileRepository(canOpen = true)
		val useCase = createUseCase(
			fileRepository = fileRepository,
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				enrollmentProof = DEFAULT_ENROLLMENT_PROOF
			)
		)

		useCase.execute(Unit).test {
			val data = awaitLoadingThenData(this)
			assertEquals(PlatformFile(DEFAULT_ENROLLMENT_PROOF_SOURCE), data)
			assertEquals(PlatformFile(DEFAULT_ENROLLMENT_PROOF_SOURCE), fileRepository.lastCanOpenFile)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsUnsupportedFileErrorWhenEnrollmentProofCannotBeOpened() = runTest {
		val useCase = createUseCase(
			fileRepository = FakeFileRepository(canOpen = false),
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				enrollmentProof = DEFAULT_ENROLLMENT_PROOF
			)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(FetchEnrollmentProofUseCaseError.UnsupportedFile, error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsNotFoundErrorWhenRepositoryCannotResolveEnrollmentProof() = runTest {
		val useCase = createUseCase(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = EnrollmentProofNotFoundException()
			)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(FetchEnrollmentProofUseCaseError.NotFound, error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsNoConnectionErrorWhenRepositoryFailsAndNetworkIsUnavailable() = runTest {
		val useCase = createUseCase(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = IllegalStateException("Could not connect to the server")
			),
			networkRepository = FakeNetworkRepository(isAvailable = false)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			val noConnection = assertIs<FetchEnrollmentProofUseCaseError.NoConnection>(error.error)
			assertEquals(false, noConnection.isNetworkAvailable)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsOutdatedPasswordErrorWhenRepositoryFailsWithConflictResponse() = runTest {
		val useCase = createUseCase(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = clientRequestException(HttpStatusCode.Conflict)
			)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(FetchEnrollmentProofUseCaseError.OutdatedPassword, error.error)

			awaitComplete()
		}
	}

	@Test
	fun execute_emitsTimeoutErrorAndReportsHandledException() = runTest {
		val reportingRepository = RecordingReportingRepository()
			val useCase = createUseCase(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = IllegalStateException("timed out")
			),
			reportingRepository = reportingRepository
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertEquals(FetchEnrollmentProofUseCaseError.Timeout, error.error)

			awaitComplete()
		}

		assertEquals(1, reportingRepository.loggedExceptions.size)
		assertEquals(true, reportingRepository.customKeys["isHandled"])
		assertTrue(reportingRepository.customKeys.containsKey("useCase"))
	}

	@Test
	fun execute_emitsUnknownErrorWhenExceptionCannotBeMapped() = runTest {
		val useCase = createUseCase(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = IllegalArgumentException("boom")
			)
		)

		useCase.execute(Unit).test {
			val error = awaitLoadingThenError(this)
			assertNull(error.error)

			awaitComplete()
		}
	}

	private fun createUseCase(
		fileRepository: FakeFileRepository = FakeFileRepository(canOpen = true),
		enrollmentProofRepository: FakeEnrollmentProofRepository = FakeEnrollmentProofRepository(),
		networkRepository: FakeNetworkRepository = FakeNetworkRepository(isAvailable = true),
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	): FetchEnrollmentProofUseCase {
		return FetchEnrollmentProofUseCase(
			applicationRepository = fileRepository,
			enrollmentProofRepository = enrollmentProofRepository,
			exceptionHandler = FetchEnrollmentProofExceptionHandler(
				networkRepository = networkRepository,
				reportingRepository = reportingRepository
			)
		)
	}
}
