package com.gdavidpb.tuindice.enrollmentproof.presentation.action

import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF_SOURCE
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeFileRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.clientRequestException
import com.gdavidpb.tuindice.testkit.mvi.reduceMutations
import io.github.vinceglb.filekit.PlatformFile
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FetchEnrollmentProofActionProcessorContractTest {
	@Test
	fun process_emitsOpenEnrollmentProofEffectAndPreservesStateOnSuccess() = runTest {
		val processor = createProcessor(
			fileRepository = FakeFileRepository(canOpen = true),
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				enrollmentProof = DEFAULT_ENROLLMENT_PROOF
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()
		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.OpenEnrollmentProof>(effects.single())
		assertEquals(PlatformFile(DEFAULT_ENROLLMENT_PROOF_SOURCE), effect.file)
	}

	@Test
	fun process_emitsNetworkUnavailableSnackBarWhenNoConnectionAndNetworkIsUnavailable() = runTest {
		val processor = createProcessor(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = IllegalStateException("Could not connect to the server")
			),
			networkRepository = FakeNetworkRepository(isAvailable = false)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Comprueba tu conexión", effect.message)
	}

	@Test
	fun process_emitsServiceUnavailableSnackBarWhenServiceIsUnavailable() = runTest {
		val processor = createProcessor(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = clientRequestException(HttpStatusCode.ServiceUnavailable)
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Servicio no disponible", effect.message)
	}

	@Test
	fun process_emitsNotFoundSnackBarWhenEnrollmentProofIsMissing() = runTest {
		val processor = createProcessor(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = EnrollmentProofNotFoundException()
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Comprobante no disponible", effect.message)
	}

	@Test
	fun process_emitsUnsupportedFileSnackBarWhenFileCannotBeOpened() = runTest {
		val processor = createProcessor(
			fileRepository = FakeFileRepository(canOpen = false),
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				enrollmentProof = DEFAULT_ENROLLMENT_PROOF
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Archivo no soportado ;(", effect.message)
	}

	@Test
	fun process_emitsNavigateToOutdatedCredentialsWhenCredentialsAreOutdated() = runTest {
		val processor = createProcessor(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = clientRequestException(HttpStatusCode.Conflict)
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)
		assertIs<Enrollment.Effect.NavigateToOutdatedCredentials>(effects.single())
	}

	@Test
	fun process_emitsTimeoutSnackBarWhenRequestTimesOut() = runTest {
		val processor = createProcessor(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = IllegalStateException("timed out")
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Tiempo de espera agotado", effect.message)
	}

	@Test
	fun process_emitsDefaultErrorSnackBarWhenErrorIsUnknown() = runTest {
		val processor = createProcessor(
			enrollmentProofRepository = FakeEnrollmentProofRepository(
				throwable = IllegalArgumentException("boom")
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val finalState = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList().reduceMutations(Enrollment.State.Fetching)

		assertEquals(Enrollment.State.Fetching, finalState)

		val effect = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("¡Ha ocurrido un error!", effect.message)
	}

	private fun createProcessor(
		fileRepository: FakeFileRepository = FakeFileRepository(canOpen = true),
		enrollmentProofRepository: FakeEnrollmentProofRepository = FakeEnrollmentProofRepository(),
		networkRepository: FakeNetworkRepository = FakeNetworkRepository(isAvailable = true)
	): FetchEnrollmentProofActionProcessor {
		return FetchEnrollmentProofActionProcessor(
			enrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = fileRepository,
				enrollmentProofRepository = enrollmentProofRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = networkRepository
				)
			),
			textProvider = FakeEnrollmentProofTextProvider()
		)
	}
}
