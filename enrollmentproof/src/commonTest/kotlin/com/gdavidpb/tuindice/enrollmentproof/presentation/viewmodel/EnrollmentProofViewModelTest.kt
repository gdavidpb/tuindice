package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.PlatformFileRef
import com.gdavidpb.tuindice.base.domain.repository.FileGateway
import com.gdavidpb.tuindice.base.domain.repository.NetworkStatusGateway
import com.gdavidpb.tuindice.base.domain.repository.ReportingGateway
import com.gdavidpb.tuindice.enrollmentproof.domain.exception.EnrollmentProofNotFoundException
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.repository.EnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.action.FetchEnrollmentProofActionProcessor
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.resource.EnrollmentProofTextProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class EnrollmentProofViewModelTest {
	@Test
	fun initialAction_whenFetchSucceeds_emitsOpenFileEffect() = runBlocking {
		val viewModel = EnrollmentProofViewModel(
			enrollmentProofActionProcessor = FetchEnrollmentProofActionProcessor(
				enrollmentProofUseCase = FetchEnrollmentProofUseCase(
					applicationRepository = EnrollmentProofViewModelFakeFileGateway(canOpen = true),
					enrollmentProofRepository = EnrollmentProofViewModelFakeRepository(
						enrollmentProof = EnrollmentProof(
							source = "/tmp/enrollment-proof.pdf",
							content = "encoded-content"
						)
					),
					exceptionHandler = FetchEnrollmentProofExceptionHandler(
						networkRepository = EnrollmentProofViewModelFakeNetworkGateway(),
						reportingRepository = EnrollmentProofViewModelFakeReportingGateway()
					)
				),
				textProvider = EnrollmentProofViewModelFakeTextProvider
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { effects.isNotEmpty() }
			val openFile = assertIs<Enrollment.Effect.OpenEnrollmentProof>(effects.single())
			assertEquals(PlatformFileRef("/tmp/enrollment-proof.pdf"), openFile.fileRef)
			assertEquals(Enrollment.State.Fetching, viewModel.state.value)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	@Test
	fun initialAction_whenNotFound_emitsNotFoundSnackBar() = runBlocking {
		val viewModel = EnrollmentProofViewModel(
			enrollmentProofActionProcessor = FetchEnrollmentProofActionProcessor(
				enrollmentProofUseCase = FetchEnrollmentProofUseCase(
					applicationRepository = EnrollmentProofViewModelFakeFileGateway(canOpen = true),
					enrollmentProofRepository = EnrollmentProofViewModelFakeRepository(
						enrollmentProofThrowable = EnrollmentProofNotFoundException()
					),
					exceptionHandler = FetchEnrollmentProofExceptionHandler(
						networkRepository = EnrollmentProofViewModelFakeNetworkGateway(),
						reportingRepository = EnrollmentProofViewModelFakeReportingGateway()
					)
				),
				textProvider = EnrollmentProofViewModelFakeTextProvider
			)
		)
		val effects = mutableListOf<Enrollment.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { effects.isNotEmpty() }
			val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(effects.single())
			assertEquals("Enrollment not found", snackBar.message)
			assertEquals(Enrollment.State.Fetching, viewModel.state.value)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout.")
	}
}

private object EnrollmentProofViewModelFakeTextProvider : EnrollmentProofTextProvider {
	override fun serviceUnavailable(): String = "Service unavailable"
	override fun networkUnavailable(): String = "Network unavailable"
	override fun enrollmentNotFound(): String = "Enrollment not found"
	override fun enrollmentUnsupported(): String = "Enrollment unsupported"
	override fun timeout(): String = "Timeout"
	override fun defaultError(): String = "Default error"
}

private class EnrollmentProofViewModelFakeRepository(
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

private class EnrollmentProofViewModelFakeFileGateway(
	private val canOpen: Boolean
) : FileGateway {
	override suspend fun createTemporaryFile(nameHint: String): PlatformFileRef {
		return PlatformFileRef("/tmp/$nameHint")
	}

	override suspend fun canOpen(fileRef: PlatformFileRef): Boolean = canOpen
}

private class EnrollmentProofViewModelFakeNetworkGateway : NetworkStatusGateway {
	override fun isAvailable(): Boolean = true
}

private class EnrollmentProofViewModelFakeReportingGateway : ReportingGateway {
	override fun setIdentifier(identifier: String) = Unit
	override fun logException(throwable: Throwable) = Unit
	override fun logMessage(message: String) = Unit
	override fun <T : Any> setCustomKey(key: String, value: T) = Unit
}
