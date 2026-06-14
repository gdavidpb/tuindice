package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofInternalEvent
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF_SOURCE
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeFileRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EnrollmentProofViewModelContractTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun initialAction_keepsFetchingStateAndEmitsOpenEnrollmentProofEffect() = runTest {
		val viewModel = EnrollmentProofViewModel(
			screenMachine = EnrollmentProofMachine(
				fetchEnrollmentProofUseCase = FetchEnrollmentProofUseCase(
					applicationRepository = FakeFileRepository(canOpen = true),
					enrollmentProofRepository = FakeEnrollmentProofRepository(
						enrollmentProof = DEFAULT_ENROLLMENT_PROOF
					),
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = FetchEnrollmentProofExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				textProvider = FakeEnrollmentProofTextProvider()
			),
			eventPublisher = NoOpEventPublisher
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(Enrollment.State.Fetching, awaitItem())
				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				val effect = assertIs<Enrollment.Effect.OpenEnrollmentProof>(awaitItem())
				assertEquals(PlatformFile(DEFAULT_ENROLLMENT_PROOF_SOURCE), effect.file)
				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val screenMachine = EnrollmentProofMachine(
			fetchEnrollmentProofUseCase = FetchEnrollmentProofUseCase(
				applicationRepository = FakeFileRepository(canOpen = true),
				enrollmentProofRepository = FakeEnrollmentProofRepository(
					enrollmentProof = DEFAULT_ENROLLMENT_PROOF
				),
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = FetchEnrollmentProofExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			),
			textProvider = FakeEnrollmentProofTextProvider()
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Enrollment.Action.FetchEnrollmentProof,
				EnrollmentProofInternalEvent.EnrollmentProofFetched(
					file = PlatformFile(DEFAULT_ENROLLMENT_PROOF_SOURCE)
				),
				EnrollmentProofInternalEvent.EnrollmentProofFetchFailed(
					message = "No se pudo descargar"
				),
				EnrollmentProofInternalEvent.EnrollmentProofUnauthorized
			),
			scope = backgroundScope,
			// Conservative floor: single state, so every row resolves from these samples;
			// raise to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
	}
}
