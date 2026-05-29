package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.action.ConfirmSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.FlushAndSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.ForceSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.InitializeSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
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
			initializeSignOutActionProcessor = InitializeSignOutActionProcessor(),
			confirmSignOutActionProcessor = ConfirmSignOutActionProcessor(
				ConfirmSignOutUseCase(
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
					reportingRepository = RecordingReportingRepository()
				)
			),
			flushAndSignOutActionProcessor = FlushAndSignOutActionProcessor(
				FlushPendingChangesUseCase(
					pendingChangesRepository = FakePendingChangesRepository(
						pendingChanges = pendingChanges
					),
					reportingRepository = RecordingReportingRepository()
				),
				signOutUseCase = SignOutUseCase(
					authRepository = RecordingAuthRepository(),
					attestationRepository = FakeAttestationRepository(),
					sessionRepository = FakeSessionRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					applicationRepository = RecordingApplicationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			forceSignOutActionProcessor = ForceSignOutActionProcessor(
				SignOutUseCase(
					authRepository = RecordingAuthRepository(),
					attestationRepository = FakeAttestationRepository(),
					sessionRepository = FakeSessionRepository(),
					sessionInvalidationRepository = FakeSessionInvalidationRepository(),
					applicationRepository = RecordingApplicationRepository(),
					syncStatusRepository = FakeSyncStatusRepository(),
					reportingRepository = RecordingReportingRepository()
				)
			),
			openUpdatePasswordActionProcessor = OpenUpdatePasswordActionProcessor(),
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
			reportingRepository = reportingRepository
		)
		val viewModel = SignOutViewModel(
			initializeSignOutActionProcessor = InitializeSignOutActionProcessor(),
			confirmSignOutActionProcessor = ConfirmSignOutActionProcessor(
				ConfirmSignOutUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase
			),
			flushAndSignOutActionProcessor = FlushAndSignOutActionProcessor(
				FlushPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase
			),
			forceSignOutActionProcessor = ForceSignOutActionProcessor(signOutUseCase),
			openUpdatePasswordActionProcessor = OpenUpdatePasswordActionProcessor(),
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
				assertEquals(
					SignOut.State.LoggingOut(pendingChanges = pendingChanges),
					awaitItem()
				)
				assertEquals(
					SignOut.State.FlushFailed(
						pendingChanges = pendingChanges,
						requiresPasswordUpdate = false
					),
					awaitItem()
				)

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
			reportingRepository = reportingRepository
		)
		val viewModel = SignOutViewModel(
			initializeSignOutActionProcessor = InitializeSignOutActionProcessor(),
			confirmSignOutActionProcessor = ConfirmSignOutActionProcessor(
				ConfirmSignOutUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase
			),
			flushAndSignOutActionProcessor = FlushAndSignOutActionProcessor(
				FlushPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				),
				signOutUseCase = signOutUseCase
			),
			forceSignOutActionProcessor = ForceSignOutActionProcessor(signOutUseCase),
			openUpdatePasswordActionProcessor = OpenUpdatePasswordActionProcessor(),
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
}
