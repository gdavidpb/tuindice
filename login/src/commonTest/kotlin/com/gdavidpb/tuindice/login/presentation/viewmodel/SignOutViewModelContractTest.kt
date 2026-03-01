package com.gdavidpb.tuindice.login.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.testing.FakeSessionRepository
import com.gdavidpb.tuindice.login.testing.RecordingApplicationRepository
import com.gdavidpb.tuindice.login.testing.RecordingMessagingRepository
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
					sessionRepository = FakeSessionRepository(),
					messagingRepository = RecordingMessagingRepository(),
					applicationRepository = RecordingApplicationRepository()
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
