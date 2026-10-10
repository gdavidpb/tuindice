package com.gdavidpb.tuindice.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * The two effects of the app host whose message the machine reads from the resources. Kept apart
 * from MainViewModelHostObservationTest because the Android host cannot read them: this class is
 * in the host exclusions of the root build and runs on the simulator.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelHostMessagesContractTest {
	@Test
	fun sessionInvalidatedFromOutside_isToldAsAnEffectWithItsMessage() = runTest {
		withMainDispatcher { dispatchers ->
			val sessionInvalidationRepository = FakeSessionInvalidationRepository()
			val viewModel = createMainViewModel(
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }
				testScheduler.advanceUntilIdle()

				viewModel.effect.test {
					sessionInvalidationRepository.notifySessionInvalidated()

					val effect = awaitItem()

					assertIs<Main.Effect.SessionInvalidated>(effect)
					assertEquals("Inicia sesión nuevamente", effect.message)
					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	@Test
	fun requestingSignOut_whenThePendingWorkCannotBeRead_saysSoInsteadOfOpeningTheDialog() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createMainViewModel(
				pendingChangesRepository = FakePendingChangesRepository(
					getPendingChangesThrowable = IllegalStateException("main-sign-out")
				),
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }

				viewModel.effect.test {
					viewModel.requestSignOutAction()

					val effect = awaitSignOutOutcome()

					assertIs<Main.Effect.ShowSnackBar>(effect)
					assertEquals(
						"No pudimos revisar tus cambios pendientes. Intenta nuevamente.",
						effect.message
					)
					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	// The startup of a signed-in session also asks for the review and the update check: those
	// effects are not what these cases are about.
	private suspend fun app.cash.turbine.ReceiveTurbine<Main.Effect>.awaitSignOutOutcome(): Main.Effect {
		while (true) {
			val effect = awaitItem()

			if (effect is Main.Effect.NavigateToSignOutDialog || effect is Main.Effect.ShowSnackBar) {
				return effect
			}
		}
	}

	private suspend fun TestScope.collectingState(
		viewModel: MainViewModel,
		block: suspend () -> Unit
	) {
		val stateCollector = backgroundScope.launchStateCollector(
			flow = viewModel.state,
			testScheduler = testScheduler
		)

		try {
			block()
		} finally {
			stateCollector.cancel()
		}
	}

	private suspend fun MainViewModel.awaitContent(
		predicate: (Main.State.Content) -> Boolean
	): Main.State.Content {
		return withTimeout(3_000) {
			state.filterIsInstance<Main.State.Content>().first(predicate)
		}
	}
}
