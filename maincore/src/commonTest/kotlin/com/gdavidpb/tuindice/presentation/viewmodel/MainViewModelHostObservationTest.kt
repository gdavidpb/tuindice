package com.gdavidpb.tuindice.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
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
 * The sync status, the session ended from outside and the pending work read before signing out
 * reach the app host through its machine: the route only reads the state and runs the effects.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelHostObservationTest {
	@Test
	fun syncStatusObservedWithTheContent_isFoldedIntoIt_andFollowsEveryChange() = runTest {
		withMainDispatcher { dispatchers ->
			val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
			val viewModel = createMainViewModel(
				syncStatusRepository = syncStatusRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				assertEquals(
					SyncStatus.OutdatedCredentials,
					viewModel.awaitContent { content -> content.syncStatus.requiresPassword }.syncStatus
				)

				syncStatusRepository.setSyncStatus(SyncStatus.Healthy)

				assertEquals(
					SyncStatus.Healthy,
					viewModel.awaitContent { content -> !content.syncStatus.requiresPassword }.syncStatus
				)
			}
		}
	}

	// Closing the password dialog holds it back only for the problem that opened it: once the
	// status stops asking for a password the dismissal is forgotten, so the next problem asks again.
	@Test
	fun dismissingThePasswordDialog_isRememberedWhileTheStatusAsksForIt_andForgottenAfter() = runTest {
		withMainDispatcher { dispatchers ->
			val syncStatusRepository = FakeSyncStatusRepository(initialValue = SyncStatus.OutdatedCredentials)
			val viewModel = createMainViewModel(
				syncStatusRepository = syncStatusRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { content -> content.syncStatus.requiresPassword }

				viewModel.dismissUpdatePasswordAction()
				viewModel.awaitContent { content -> content.isUpdatePasswordDismissed }

				// Another status that still asks for the password keeps the dismissal.
				syncStatusRepository.setSyncStatus(SyncStatus.MissingCredentials)
				assertEquals(
					true,
					viewModel.awaitContent { content ->
						content.syncStatus == SyncStatus.MissingCredentials
					}.isUpdatePasswordDismissed
				)

				syncStatusRepository.setSyncStatus(SyncStatus.Healthy)
				assertEquals(
					false,
					viewModel.awaitContent { content ->
						content.syncStatus == SyncStatus.Healthy
					}.isUpdatePasswordDismissed
				)
			}
		}
	}

	@Test
	fun dismissingThePasswordDialog_whenNoPasswordIsAskedFor_changesNothing() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createMainViewModel(dispatchers = dispatchers)

			collectingState(viewModel) {
				val content = viewModel.awaitContent { true }

				viewModel.dismissUpdatePasswordAction()
				testScheduler.advanceUntilIdle()

				assertEquals(content, viewModel.state.value)
			}
		}
	}

	@Test
	fun requestingSignOut_readsThePendingWork_andOpensTheDialogWithIt() = runTest {
		withMainDispatcher { dispatchers ->
			val pendingChanges = PendingChanges(
				totalCount = 2,
				recordCount = 1,
				evaluationsCount = 1,
				hasFailedMutations = false
			)
			val pendingChangesRepository = FakePendingChangesRepository(pendingChanges = pendingChanges)
			val viewModel = createMainViewModel(
				pendingChangesRepository = pendingChangesRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }

				viewModel.effect.test {
					viewModel.requestSignOutAction()

					val effect = awaitSignOutOutcome()

					assertIs<Main.Effect.NavigateToSignOutDialog>(effect)
					assertEquals(pendingChanges, effect.pendingChanges)
					cancelAndIgnoreRemainingEvents()
				}
			}

			assertEquals(1, pendingChangesRepository.getPendingChangesCalls)
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
