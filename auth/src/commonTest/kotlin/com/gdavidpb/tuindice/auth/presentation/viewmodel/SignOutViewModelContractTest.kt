package com.gdavidpb.tuindice.auth.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.testing.RecordingAuthRepository
import com.gdavidpb.tuindice.auth.testing.FakeSessionRepository
import com.gdavidpb.tuindice.auth.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.auth.testing.RecordingMessagingRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SignOutViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun signOutAction_updatesState_andNavigatesToSignIn() = runTest {
		val viewModel = SignOutViewModel(
			signOutActionProcessor = SignOutActionProcessor(
				signOutUseCase = SignOutUseCase(
					authRepository = RecordingAuthRepository(),
					sessionRepository = FakeSessionRepository(),
					messagingRepository = RecordingMessagingRepository(),
					applicationRepository = RecordingApplicationRepository(),
					credentialsRepository = FakeCredentialsRepository()
				)
			)
		)
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			viewModel.state.test {
				assertEquals(SignOut.State.Idle, awaitItem())

				viewModel.signOutAction()
				assertEquals(SignOut.State.LoggingOut, awaitItem())

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
