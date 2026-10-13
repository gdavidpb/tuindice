package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.awaitContent
import com.gdavidpb.tuindice.summary.testing.awaitHubOutsideTheContent
import com.gdavidpb.tuindice.summary.testing.createHubScenarioViewModel
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// The hub through a refresh that fails. The failure is told with a text read from the resources, which
// the Android host tests cannot read: these run where the rest of the contracts of the view model do.
class SummaryHubRefreshFailureViewModelContractTest {
	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun theHubThatArrivesBeforeTheUser_survivesAFailedRefresh_andIsTheOneOfTheContentAfterwards() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableSharedFlow<User>()
			val refreshGate = CompletableDeferred<Unit>()
			val viewModel = createHubScenarioViewModel(
				dispatchers = dispatchers,
				users = users,
				refreshFailure = IllegalStateException("hub-refresh"),
				refreshGate = refreshGate
			)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				val beforeTheUser = viewModel.awaitHubOutsideTheContent()

				viewModel.refreshSummaryAction()

				// The refresh waits until it is running, as the one on a real dispatcher does.
				withTimeout(3_000) { viewModel.state.filterIsInstance<Summary.State.Loading>().first() }
				refreshGate.complete(Unit)

				val failed = inRealTime {
					viewModel.state.filterIsInstance<Summary.State.Failed>().first()
				}

				assertEquals(beforeTheUser, failed.hub)

				users.emit(DEFAULT_SUMMARY_USER)

				val content = viewModel.awaitContent { true }

				assertNotNull(content.hub.week)
				assertEquals(beforeTheUser, content.hub)
			} finally {
				collector.cancel()
			}
		}
	}

	@Test
	@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
	fun aRefreshThatFails_keepsTheHubOfTheContent() = runTest {
		withMainDispatcher { dispatchers ->
			val users = MutableStateFlow(DEFAULT_SUMMARY_USER)
			val refreshGate = CompletableDeferred<Unit>()
			val viewModel = createHubScenarioViewModel(
				dispatchers = dispatchers,
				users = users,
				refreshFailure = IllegalStateException("hub-refresh"),
				refreshGate = refreshGate
			)
			val collector = backgroundScope.launchStateCollector(viewModel.state, testScheduler)

			try {
				val before = viewModel.awaitContent { content -> content.hub.week != null }

				viewModel.refreshSummaryAction()
				viewModel.awaitContent { content -> content.isUserRefreshing }
				refreshGate.complete(Unit)

				val after = inRealTime { viewModel.awaitContent { content -> !content.isUserRefreshing } }

				assertEquals(before.hub, after.hub)
			} finally {
				collector.cancel()
			}
		}
	}

	// A refresh that fails says why with a text read from the resources, which takes real time that the
	// virtual clock of the test does not count.
	private suspend fun <T> inRealTime(block: suspend () -> T): T {
		return withContext(Dispatchers.Default.limitedParallelism(1)) {
			withTimeout(5_000) { block() }
		}
	}
}
