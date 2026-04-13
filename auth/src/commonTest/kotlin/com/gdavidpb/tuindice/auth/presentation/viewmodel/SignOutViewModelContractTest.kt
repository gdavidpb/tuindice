package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.LoadPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.action.ConfirmSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.FlushAndSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.ForceSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.LoadPendingChangesActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.testing.FakeAttestationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignOutViewModelContractTest {
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
			applicationRepository = applicationRepository,
			syncStatusRepository = syncStatusRepository,
			reportingRepository = reportingRepository
		)
		val viewModel = SignOutViewModel(
			loadPendingChangesActionProcessor = LoadPendingChangesActionProcessor(
				LoadPendingChangesUseCase(
					pendingChangesRepository = pendingChangesRepository,
					reportingRepository = reportingRepository
				)
			),
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
			openUpdatePasswordActionProcessor = OpenUpdatePasswordActionProcessor()
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
