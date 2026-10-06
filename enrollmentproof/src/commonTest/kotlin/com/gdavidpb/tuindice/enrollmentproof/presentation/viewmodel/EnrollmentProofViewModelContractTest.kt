package com.gdavidpb.tuindice.enrollmentproof.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.enrollmentproof.domain.model.EnrollmentProof
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.FetchEnrollmentProofUseCase
import com.gdavidpb.tuindice.enrollmentproof.domain.usecase.exceptionhandler.FetchEnrollmentProofExceptionHandler
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofInternalEvent
import com.gdavidpb.tuindice.enrollmentproof.presentation.machine.EnrollmentProofMachine
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF
import com.gdavidpb.tuindice.enrollmentproof.testing.DEFAULT_ENROLLMENT_PROOF_SOURCE
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeEnrollmentProofTextProvider
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeFileRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.FakeNetworkRepository
import com.gdavidpb.tuindice.enrollmentproof.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
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
	@OptIn(ExperimentalCoroutinesApi::class)
	fun openCompletedWithoutAViewer_emitsTheViewerMissingSnackBarWithoutRetry() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.effect.test {
					assertIs<Enrollment.Effect.OpenEnrollmentProof>(awaitItem())

					// The route ran the effect and the device had nothing to open the file with.
					viewModel.openEnrollmentProofCompletedAction(opened = false)

					val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(awaitItem())
					assertEquals("Archivo no soportado ;(", snackBar.message)
					assertEquals(false, snackBar.canRetry)
					assertEquals(Enrollment.State.Fetching, viewModel.state.value)
					expectNoEvents()
				}
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun openCompletedWithoutAViewer_isAnsweredFromTheSavedCopyConfirmationToo() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				enrollmentProof = DEFAULT_ENROLLMENT_PROOF.copy(isFromCache = true)
			)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				val confirming = viewModel.state
					.filterIsInstance<Enrollment.State.ConfirmingSavedCopy>()
					.first()

				viewModel.effect.test {
					// Nothing opens until the saved copy is confirmed.
					expectNoEvents()

					viewModel.openSavedEnrollmentProofAction()
					assertIs<Enrollment.Effect.OpenEnrollmentProof>(awaitItem())

					viewModel.openEnrollmentProofCompletedAction(opened = false)

					val snackBar = assertIs<Enrollment.Effect.ShowSnackBar>(awaitItem())
					assertEquals("Archivo no soportado ;(", snackBar.message)
					assertEquals(false, snackBar.canRetry)
					// The question stays as it was: the route is who closes the dialog.
					assertEquals(confirming, viewModel.state.value)
					expectNoEvents()
				}
			} finally {
				stateCollector.cancel()
			}
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun openCompletedWithAViewer_emitsNothingElse() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createViewModel(dispatchers = dispatchers)
			val stateCollector = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.effect.test {
					assertIs<Enrollment.Effect.OpenEnrollmentProof>(awaitItem())

					viewModel.openEnrollmentProofCompletedAction(opened = true)

					expectNoEvents()
					assertEquals(Enrollment.State.Fetching, viewModel.state.value)
				}
			} finally {
				stateCollector.cancel()
			}
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
				EnrollmentProofInternalEvent.SavedEnrollmentProofFound(
					file = PlatformFile(DEFAULT_ENROLLMENT_PROOF_SOURCE)
				),
				Enrollment.Action.OpenSavedEnrollmentProof,
				EnrollmentProofInternalEvent.EnrollmentProofFetchFailed(
					message = "No se pudo descargar",
					canRetry = true
				),
				EnrollmentProofInternalEvent.EnrollmentProofUnauthorized,
				Enrollment.Action.OpenEnrollmentProofCompleted(opened = true),
				Enrollment.Action.OpenEnrollmentProofCompleted(opened = false),
				EnrollmentProofInternalEvent.EnrollmentProofViewerMissing(
					message = "Archivo no soportado ;("
				)
			),
			coroutineScope = backgroundScope,
			// Conservative floor: single state, so every row resolves from these samples;
			// raise to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.5
		)
	}

	private fun createViewModel(
		dispatchers: TuIndiceDispatchers,
		enrollmentProof: EnrollmentProof = DEFAULT_ENROLLMENT_PROOF
	): EnrollmentProofViewModel {
		return EnrollmentProofViewModel(
			screenMachine = EnrollmentProofMachine(
				fetchEnrollmentProofUseCase = FetchEnrollmentProofUseCase(
					applicationRepository = FakeFileRepository(canOpen = true),
					enrollmentProofRepository = FakeEnrollmentProofRepository(
						enrollmentProof = enrollmentProof
					),
					reportingRepository = RecordingReportingRepository(),
					exceptionHandler = FetchEnrollmentProofExceptionHandler(
						networkRepository = FakeNetworkRepository(isAvailable = true)
					)
				),
				textProvider = FakeEnrollmentProofTextProvider()
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}
}
