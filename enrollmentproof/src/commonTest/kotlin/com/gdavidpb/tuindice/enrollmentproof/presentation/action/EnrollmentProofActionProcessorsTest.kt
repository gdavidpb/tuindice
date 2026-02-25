package com.gdavidpb.tuindice.enrollmentproof.presentation.action

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.FileGateway
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EnrollmentProofActionProcessorsTest {
	@Test
	fun fetchEnrollmentProofActionProcessor_whenFetchSucceeds_emitsOpenFileEffect() = runBlocking {
		val processor = FetchEnrollmentProofActionProcessor(
			enrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = FakeFileGateway(canOpen = true),
				enrollmentProofRepository = FakeEnrollmentProofRepository(
					enrollmentProof = EnrollmentProof(
						source = "/tmp/enrollment-proof.pdf",
						content = "encoded-content"
					)
				),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeEnrollmentTextProvider
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val mutations = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Enrollment.State.Fetching, mutations)

		assertEquals(Enrollment.State.Fetching, finalState)
		val effect = assertIs<Enrollment.Effect.OpenEnrollmentProof>(effects.single())
		assertEquals(PlatformFileRef("/tmp/enrollment-proof.pdf"), effect.fileRef)
	}

	@Test
	fun fetchEnrollmentProofActionProcessor_whenNotFound_emitsNotFoundSnackBar() = runBlocking {
		val processor = FetchEnrollmentProofActionProcessor(
			enrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = FakeFileGateway(canOpen = true),
				enrollmentProofRepository = FakeEnrollmentProofRepository(
					enrollmentProofThrowable = EnrollmentProofNotFoundException()
				),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeEnrollmentTextProvider
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val mutations = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Enrollment.State.Fetching, mutations)

		assertEquals(Enrollment.State.Fetching, finalState)
		val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Enrollment not found", snackBar.message)
	}

	@Test
	fun fetchEnrollmentProofActionProcessor_whenUnsupportedFile_emitsUnsupportedSnackBar() = runBlocking {
		val processor = FetchEnrollmentProofActionProcessor(
			enrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = FakeFileGateway(canOpen = false),
				enrollmentProofRepository = FakeEnrollmentProofRepository(
					enrollmentProof = EnrollmentProof(
						source = "/tmp/enrollment-proof.pdf",
						content = "encoded-content"
					)
				),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeEnrollmentTextProvider
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val mutations = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Enrollment.State.Fetching, mutations)

		assertEquals(Enrollment.State.Fetching, finalState)
		val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Enrollment unsupported", snackBar.message)
	}

	@Test
	fun fetchEnrollmentProofActionProcessor_whenTimeout_emitsTimeoutSnackBar() = runBlocking {
		val processor = FetchEnrollmentProofActionProcessor(
			enrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = FakeFileGateway(canOpen = true),
				enrollmentProofRepository = FakeEnrollmentProofRepository(
					enrollmentProofThrowable = timeoutThrowable()
				),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeEnrollmentTextProvider
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val mutations = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Enrollment.State.Fetching, mutations)

		assertEquals(Enrollment.State.Fetching, finalState)
		val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Timeout", snackBar.message)
	}

	@Test
	fun fetchEnrollmentProofActionProcessor_whenUnexpectedError_emitsDefaultSnackBar() = runBlocking {
		val processor = FetchEnrollmentProofActionProcessor(
			enrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = FakeFileGateway(canOpen = true),
				enrollmentProofRepository = FakeEnrollmentProofRepository(
					enrollmentProofThrowable = IllegalStateException("unexpected")
				),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = FakeNetworkStatusGateway(),
					reportingRepository = FakeReportingGateway()
				)
			),
			textProvider = FakeEnrollmentTextProvider
		)
		val effects = mutableListOf<Enrollment.Effect>()

		val mutations = processor.process(
			action = Enrollment.Action.FetchEnrollmentProof,
			sideEffect = effects::add
		).toList()
		val finalState = applyMutations(Enrollment.State.Fetching, mutations)

		assertEquals(Enrollment.State.Fetching, finalState)
		val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
		assertEquals("Default error", snackBar.message)
	}

	private fun applyMutations(
		initialState: Enrollment.State,
		mutations: List<(Enrollment.State) -> Enrollment.State>
	): Enrollment.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}

private object FakeEnrollmentTextProvider : EnrollmentProofTextProvider {
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun enrollmentNotFound(): String = "Enrollment not found"
	override fun enrollmentUnsupported(): String = "Enrollment unsupported"
	override fun timeout(): String = "Timeout"
	override fun defaultError(): String = "Default error"
}

private class FakeEnrollmentProofRepository(
	private val enrollmentProof: EnrollmentProof = EnrollmentProof(
		source = "/tmp/enrollment-proof.pdf",
		content = "encoded-content"
	),
	private val enrollmentProofThrowable: Throwable? = null
) : EnrollmentProofRepository {
	override suspend fun getEnrollmentProof(): EnrollmentProof {
		enrollmentProofThrowable?.let { throw it }
		return enrollmentProof
	}
}

private class FakeFileGateway(
	private val canOpen: Boolean
) : FileGateway {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = canOpen
}

private class FakeNetworkStatusGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class FakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit

	override fun logException(throwable: Throwable) = Unit

	override fun logMessage(message: String) = Unit

	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}

private fun timeoutThrowable(): Throwable = runBlocking {
	val throwable = runCatching {
		withTimeout(1) {
			delay(5)
		}
	}.exceptionOrNull()

	checkNotNull(throwable)
}
