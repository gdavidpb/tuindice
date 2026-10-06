package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventNames
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.domain.repository.OutdatedAppEventRepository
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The server refusing this version is observed by the machine itself: nobody outside has to
 * collect the refusals and hand them over as actions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelOutdatedAppObservationTest {
	@Test
	fun refusalObservedAfterStartUp_movesContentToOutdatedApp() = runTest {
		withMainDispatcher { dispatchers ->
			val outdatedAppEventRepository = ObservableOutdatedAppEventRepository()
			val eventPublisher = RecordingEventPublisher()
			val viewModel = createMainViewModel(
				outdatedAppEventRepository = outdatedAppEventRepository,
				eventPublisher = eventPublisher,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitState<Main.State.Content>()

				outdatedAppEventRepository.notifyOutdatedApp(
					OutdatedAppState(minimumVersionCode = 52)
				)

				assertEquals(
					Main.State.OutdatedApp(
						outdatedAppState = OutdatedAppState(minimumVersionCode = 52)
					),
					viewModel.awaitState<Main.State.OutdatedApp>()
				)
				// It re-enters as an internal event, not as an action somebody dispatched.
				assertTrue(
					eventPublisher.events.any { event ->
						event.name == EventNames.APP_TRANSITION &&
							event.parameters[EventParameterKeys.EVENT] == "outdated_app_observed" &&
							event.parameters[EventParameterKeys.TO] == "outdated_app"
					}
				)
				assertTrue(
					eventPublisher.events.none { event ->
						event.name == EventNames.APP_ACTION &&
							event.parameters[EventParameterKeys.ACTION] == "show_outdated_app"
					}
				)
			}
		}
	}

	@Test
	fun refusalObservedWhileStartUpIsStillRunning_isNotLost() = runTest {
		withMainDispatcher { dispatchers ->
			val outdatedAppEventRepository = ObservableOutdatedAppEventRepository()
			val sessionRepository = HeldSessionRepository()
			val viewModel = createMainViewModel(
				sessionRepository = sessionRepository,
				outdatedAppEventRepository = outdatedAppEventRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				// The startup has not resolved: it is waiting on the session.
				withTimeout(3_000) { sessionRepository.asked.await() }
				assertIs<Main.State.Starting>(viewModel.state.value)
				// Nothing is replayed, so the refusal only counts if somebody is listening.
				assertEquals(1, outdatedAppEventRepository.observers)

				outdatedAppEventRepository.notifyOutdatedApp(
					OutdatedAppState(minimumVersionCode = 52)
				)

				assertEquals(
					OutdatedAppState(minimumVersionCode = 52),
					viewModel.awaitState<Main.State.OutdatedApp>().outdatedAppState
				)

				sessionRepository.answer()
			}
		}
	}

	@Test
	fun retryingTheStartUp_keepsASingleObserver() = runTest {
		withMainDispatcher { dispatchers ->
			val outdatedAppEventRepository = ObservableOutdatedAppEventRepository()
			val eventPublisher = RecordingEventPublisher()
			val viewModel = createMainViewModel(
				outdatedAppEventRepository = outdatedAppEventRepository,
				eventPublisher = eventPublisher,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitState<Main.State.Content>()

				viewModel.startUpAction()
				viewModel.startUpAction()
				viewModel.awaitState<Main.State.Content>()

				assertEquals(1, outdatedAppEventRepository.observers)

				outdatedAppEventRepository.notifyOutdatedApp(
					OutdatedAppState(minimumVersionCode = 52)
				)
				viewModel.awaitState<Main.State.OutdatedApp>()

				// One observer, one transition: a second one would repeat it.
				assertEquals(
					1,
					eventPublisher.events.count { event ->
						event.name == EventNames.APP_TRANSITION &&
							event.parameters[EventParameterKeys.EVENT] == "outdated_app_observed"
					}
				)
			}
		}
	}

	@Test
	fun showOutdatedAppAction_stillMovesToOutdatedApp() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createMainViewModel(dispatchers = dispatchers)

			collectingState(viewModel) {
				viewModel.awaitState<Main.State.Content>()

				// What the sign-in screen asks of the host when its own request is refused.
				viewModel.showOutdatedAppAction(OutdatedAppState(minimumVersionCode = Long.MAX_VALUE))

				assertEquals(
					OutdatedAppState(minimumVersionCode = Long.MAX_VALUE),
					viewModel.awaitState<Main.State.OutdatedApp>().outdatedAppState
				)
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

	private suspend inline fun <reified S : Main.State> MainViewModel.awaitState(): S {
		return withTimeout(3_000) {
			state.filterIsInstance<S>().first()
		}
	}

	// The real source keeps its flow to itself; this one tells how many are listening.
	private class ObservableOutdatedAppEventRepository : OutdatedAppEventRepository {
		private val events = MutableSharedFlow<OutdatedAppState>(extraBufferCapacity = 1)

		val observers: Int
			get() = events.subscriptionCount.value

		override fun observeOutdatedApp(): Flow<OutdatedAppState> = events

		override suspend fun notifyOutdatedApp(state: OutdatedAppState) {
			events.emit(state)
		}
	}

	// A session store that does not answer until told, so the startup stays unresolved.
	private class HeldSessionRepository(
		private val delegate: SessionRepository = FakeSessionRepository()
	) : SessionRepository by delegate {
		private val answered = CompletableDeferred<Unit>()

		val asked = CompletableDeferred<Unit>()

		override suspend fun hasActiveSession(): Boolean {
			asked.complete(Unit)
			answered.await()

			return delegate.hasActiveSession()
		}

		fun answer() {
			answered.complete(Unit)
		}
	}

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<AppEvent>()

		override fun publish(event: AppEvent) {
			events += event
		}
	}
}
