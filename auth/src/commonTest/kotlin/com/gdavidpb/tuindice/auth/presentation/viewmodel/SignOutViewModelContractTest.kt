package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutInternalEvent
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import com.gdavidpb.tuindice.testkit.mvi.awaitUntilState
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignOutViewModelContractTest {
	@Test
	fun initializeAction_updatesState_toPending_whenResolvedInputHasPendingChanges() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 2,
			recordCount = 2,
			evaluationsCount = 0,
			hasFailedMutations = true
		)
		val viewModel = SignOutViewModel(
			screenMachine = SignOutMachine(
				confirmSignOutUseCase = ConfirmSignOutUseCase(
					pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges),
					reportingRepository = RecordingReportingRepository()
				),
				signOutUseCase = SignOutUseCase(
					authRepository = RecordingAuthRepository(),
					attestationRepository = FakeAttestationRepository(),
					sessionRepository = FakeSessionRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					applicationRepository = RecordingApplicationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					sessionCoroutineScope = testSessionCoroutineScope(),
					reportingRepository = RecordingReportingRepository()
				),
				flushPendingChangesUseCase = FlushPendingChangesUseCase(
					pendingChangesRepository = FakePendingChangesRepository(
						pendingChanges = pendingChanges
					),
					reportingRepository = RecordingReportingRepository()
				)
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignOut.State.Plain, awaitItem())

				viewModel.initializeAction(pendingChanges)
				assertEquals(SignOut.State.Pending(pendingChanges), awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun signOutAction_usesResolvedPendingChanges_beforeStateBootstrap() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 4,
			recordCount = 4,
			evaluationsCount = 0,
			hasFailedMutations = true
		)
		val pendingChangesRepository = FakePendingChangesRepository(
			pendingChanges = PendingChanges.Empty,
			flushResult = FlushPendingChangesResult.PendingRemaining(pendingChanges)
		)
		val reportingRepository = RecordingReportingRepository()
		val signOutUseCase = SignOutUseCase(
			authRepository = RecordingAuthRepository(),
			attestationRepository = FakeAttestationRepository(),
			sessionRepository = FakeSessionRepository(),
			sessionInvalidationRepository = FakeSessionInvalidationRepository(),
			applicationRepository = RecordingApplicationRepository(),
			syncStatusRepository = FakeSyncStatusRepository(),
			sessionCoroutineScope = testSessionCoroutineScope(),
			reportingRepository = reportingRepository
		)
		val viewModel = SignOutViewModel(
			screenMachine = SignOutMachine(
				confirmSignOutUseCase = ConfirmSignOutUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase,
				flushPendingChangesUseCase = FlushPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)

		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignOut.State.Plain, awaitItem())

				viewModel.signOutAction(resolvedPendingChanges = pendingChanges)
				// The conflated state flow may skip the intermediate LoggingOut emission;
				// the resolved payload is asserted on the terminal state that carries it
				// forward, which is what proves bootstrap was bypassed.
				val flushFailed = awaitUntilState<SignOut.State.FlushFailed> { state ->
					state.pendingChanges == pendingChanges
				}
				assertEquals(false, flushFailed.requiresPasswordUpdate)

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}

		assertEquals(0, pendingChangesRepository.getPendingChangesCalls)
		assertEquals(1, pendingChangesRepository.flushCalls)
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signOutAction_updatesState_andNavigatesToSignIn() = runTest {
		val pendingChangesRepository = FakePendingChangesRepository()
		val reportingRepository = RecordingReportingRepository()
		val authRepository = RecordingAuthRepository()
		val attestationRepository = FakeAttestationRepository()
		val sessionRepository = FakeSessionRepository()
		val applicationRepository = RecordingApplicationRepository()
		val syncStatusRepository = FakeSyncStatusRepository()
		val signOutUseCase = SignOutUseCase(
			authRepository = authRepository,
			attestationRepository = attestationRepository,
			sessionRepository = sessionRepository,
			sessionInvalidationRepository = FakeSessionInvalidationRepository(),
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			sessionCoroutineScope = testSessionCoroutineScope(),
			reportingRepository = reportingRepository
		)
		val viewModel = SignOutViewModel(
			screenMachine = SignOutMachine(
				confirmSignOutUseCase = ConfirmSignOutUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase,
				flushPendingChangesUseCase = FlushPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				)
			),
			eventPublisher = NoOpEventPublisher
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignOut.State.Plain, awaitItem())

				viewModel.signOutAction()
				assertEquals(SignOut.State.LoggingOut(), awaitItem())

				cancelAndIgnoreRemainingEvents()
			}

			viewModel.effect.test {
				viewModel.signOutAction()
				assertIs<SignOut.Effect.NavigateToSignIn>(awaitItem())

				cancelAndIgnoreRemainingEvents()
			}
		} finally {
			stateCollector.cancel()
		}
	}

	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val pendingChanges = PendingChanges(
			totalCount = 2,
			recordCount = 2,
			evaluationsCount = 0,
			hasFailedMutations = true
		)
		val pendingChangesRepository = FakePendingChangesRepository(
			pendingChanges = pendingChanges
		)
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = SignOutMachine(
			confirmSignOutUseCase = ConfirmSignOutUseCase(
				pendingChangesRepository = pendingChangesRepository,
				reportingRepository = reportingRepository
			),
			signOutUseCase = SignOutUseCase(
				authRepository = RecordingAuthRepository(),
				attestationRepository = FakeAttestationRepository(),
				sessionRepository = FakeSessionRepository(),
				sessionInvalidationRepository = FakeSessionInvalidationRepository(),
				applicationRepository = RecordingApplicationRepository(),
				syncStatusRepository = FakeSyncStatusRepository(),
				sessionCoroutineScope = testSessionCoroutineScope(),
				reportingRepository = reportingRepository
			),
			flushPendingChangesUseCase = FlushPendingChangesUseCase(
				pendingChangesRepository = pendingChangesRepository,
				reportingRepository = reportingRepository
			)
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				SignOut.Action.Initialize(pendingChanges = pendingChanges),
				SignOut.Action.ClickSignOut(resolvedPendingChanges = pendingChanges),
				SignOut.Action.ClickSignOut(resolvedPendingChanges = null),
				SignOut.Action.RetryFlushAndSignOut(pendingChanges = pendingChanges),
				SignOut.Action.ForceSignOut,
				SignOutInternalEvent.SignOutInitializedPlain,
				SignOutInternalEvent.SignOutInitializedPending(pendingChanges = pendingChanges),
				SignOutInternalEvent.LoggingOutObserved(
					pendingChanges = pendingChanges,
					requiresPasswordUpdate = false
				),
				SignOutInternalEvent.PendingChangesFound(pendingChanges = pendingChanges),
				SignOutInternalEvent.SignOutSucceeded,
				SignOutInternalEvent.SignOutFailedToPlain(message = "No se pudo cerrar sesión"),
				SignOutInternalEvent.FlushFailedObserved(
					pendingChanges = pendingChanges,
					requiresPasswordUpdate = false,
					message = "Cambios pendientes sin sincronizar"
				)
			),
			coroutineScope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}
}
