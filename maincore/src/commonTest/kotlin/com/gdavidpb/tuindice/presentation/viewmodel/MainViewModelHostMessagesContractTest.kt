package com.gdavidpb.tuindice.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.PendingSessionEnd
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * What the app host owes the user about a session that ended, and the sign-out refusal, whose message the
 * machine reads from the resources. Kept apart
 * from MainViewModelHostObservationTest because the Android host cannot read them: this class is
 * in the host exclusions of the root build and runs on the simulator.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelHostMessagesContractTest {
	@Test
	fun sessionInvalidatedFromOutside_isPendingStateWithItsMessage() = runTest {
		withMainDispatcher { dispatchers ->
			val sessionInvalidationRepository = FakeSessionInvalidationRepository()
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			val viewModel = createMainViewModel(
				settingsRepository = settingsRepository,
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }
				testScheduler.advanceUntilIdle()

				// What the invalidation does: leave the notice, then tell.
				settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
				sessionInvalidationRepository.notifySessionInvalidated()

				val pending = viewModel.awaitPendingSessionEnd { true }

				assertEquals("Inicia sesión nuevamente", pending.message)
			}
		}
	}

	// A signal without a notice (the user was already told, or there is none) still takes the user to the
	// sign-in: it is a pending end with nothing to say, not an event that nobody hears.
	@Test
	fun sessionInvalidatedWithoutANotice_isStillAPendingEnd() = runTest {
		withMainDispatcher { dispatchers ->
			val sessionInvalidationRepository = FakeSessionInvalidationRepository()
			val viewModel = createMainViewModel(
				settingsRepository = FakeSettingsRepository(reviewSuggested = false),
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }
				testScheduler.advanceUntilIdle()

				sessionInvalidationRepository.notifySessionInvalidated()

				assertNull(viewModel.awaitPendingSessionEnd { true }.message)
			}
		}
	}

	// The notice reaches the route as state once the content is up, and the copy kept for the next start
	// is taken only when the route says it attended it: a process that dies in between (a background
	// kill) still explains the sign-in screen at the next start.
	@Test
	fun theCopyForTheNextStart_isKeptUntilTheRouteSaysItAttendedTheEnd_andThenTakenByItsStamp() = runTest {
		withMainDispatcher { dispatchers ->
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			// The real one: it keeps the signal until the machine observes it.
			val sessionInvalidationRepository = SessionInvalidationDataSource()
			val viewModel = createMainViewModel(
				settingsRepository = settingsRepository,
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }

				// The session ends once the start-up is over (it would have taken the notice before).
				settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
				sessionInvalidationRepository.notifySessionInvalidated()

				val pending = viewModel.awaitPendingSessionEnd { true }
				assertEquals(true, settingsRepository.sessionResetNoticePending, "told is not shown")

				viewModel.sessionEndHandledAction(id = pending.id)

				viewModel.awaitState { state -> state.pendingSessionEnd == null }
				withTimeout(3_000) {
					while (settingsRepository.sessionResetNoticePending) yield()
				}
			}
		}
	}

	// An end that is replaced while the route attends the old one: the old id changes nothing.
	@Test
	fun handlingAnOldEnd_doesNotClearTheNewerOne() = runTest {
		withMainDispatcher { dispatchers ->
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			val sessionInvalidationRepository = FakeSessionInvalidationRepository()
			val viewModel = createMainViewModel(
				settingsRepository = settingsRepository,
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }
				testScheduler.advanceUntilIdle()

				// First without a notice, then the notice arrives: the end is replaced by one with a message.
				sessionInvalidationRepository.notifySessionInvalidated()
				val old = viewModel.awaitPendingSessionEnd { true }
				settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
				sessionInvalidationRepository.notifySessionInvalidated()
				val newer = viewModel.awaitPendingSessionEnd { pending -> pending.id != old.id }

				viewModel.sessionEndHandledAction(id = old.id)
				testScheduler.advanceUntilIdle()

				assertEquals(newer, viewModel.state.value.pendingSessionEnd, "the newer end is still owed")
				assertEquals(true, settingsRepository.sessionResetNoticePending, "and its notice is not taken")
			}
		}
	}

	// The start-up offers the notice the invalidation left (it is still pending until the route says it
	// attended it) and the live signal of the same invalidation arrives afterwards: the user is told once.
	@Test
	fun sessionNoticeOfferedByTheStartUp_isStateFromTheStart_andTheLiveSignalOfTheSameInvalidationKeepsIt() = runTest {
		withMainDispatcher { dispatchers ->
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			val sessionInvalidationRepository = SessionInvalidationDataSource()
			// What the invalidation did before the start-up: leave the notice, and tell.
			settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
			sessionInvalidationRepository.notifySessionInvalidated()
			val liveSignalProcessed = CompletableDeferred<Unit>()
			val viewModel = createMainViewModel(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				settingsRepository = settingsRepository,
				sessionInvalidationRepository = sessionInvalidationRepository,
				eventPublisher = TransitionWaitingEventPublisher(
					eventName = "session_invalidation_observed",
					seen = liveSignalProcessed
				),
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				val offered = viewModel.awaitPendingSessionEnd { true }

				// The server ended the session: the notice says that, not that the data could not be read.
				assertEquals("Inicia sesión nuevamente", offered.message)
				assertEquals(true, settingsRepository.sessionResetNoticePending, "offered is not shown")

				// The live signal of the same invalidation reached the machine while the end was pending:
				// the end is the same one, with the same notice, neither lost nor repeated.
				// The transition is published once the state is applied, so waiting for it is waiting for the
				// machine to have decided: an assertion before it would pass whatever the machine did.
				withTimeout(5_000) { liveSignalProcessed.await() }
				assertEquals(offered, viewModel.state.value.pendingSessionEnd)

				viewModel.sessionEndHandledAction(id = offered.id)
				viewModel.awaitState { state -> state.pendingSessionEnd == null }
				withTimeout(3_000) {
					while (settingsRepository.sessionResetNoticePending) yield()
				}
			}
		}
	}

	// The notice of the start-up says what happened: here the data could not be read.
	@Test
	fun sessionNoticeOfferedByTheStartUp_saysWhyAccordingToItsReason() = runTest {
		withMainDispatcher { dispatchers ->
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.UnreadableData)
			val viewModel = createMainViewModel(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				settingsRepository = settingsRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				assertEquals(
					"No pudimos leer tus datos y reiniciamos la sesión. Inicia sesión de nuevo.",
					viewModel.awaitPendingSessionEnd { true }.message
				)
			}
		}
	}

	// A start-up with nothing owed leaves nothing pending.
	@Test
	fun aStartUpWithNoNoticeOwed_leavesNoPendingEnd() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createMainViewModel(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				settingsRepository = FakeSettingsRepository(reviewSuggested = false),
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				val content = viewModel.awaitContent { true }

				assertNull(content.pendingSessionEnd)
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

	// The live signal and the notice text are read in real time (the text comes from the resources).
	private suspend fun MainViewModel.awaitState(predicate: (Main.State) -> Boolean): Main.State {
		return withContext(Dispatchers.Default) {
			withTimeout(3_000) { state.first(predicate) }
		}
	}

	private suspend fun MainViewModel.awaitPendingSessionEnd(
		predicate: (PendingSessionEnd) -> Boolean
	): PendingSessionEnd {
		val state = awaitState { candidate -> candidate.pendingSessionEnd?.let(predicate) == true }

		return checkNotNull(state.pendingSessionEnd)
	}

	private suspend fun MainViewModel.awaitContent(
		predicate: (Main.State.Content) -> Boolean
	): Main.State.Content {
		return withTimeout(3_000) {
			state.filterIsInstance<Main.State.Content>().first(predicate)
		}
	}
}

// Completes [seen] when the machine publishes the transition it made for the event named [eventName].
private class TransitionWaitingEventPublisher(
	private val eventName: String,
	private val seen: CompletableDeferred<Unit>
) : EventPublisher {
	override fun publish(event: AppEvent) {
		if (event is AppEvent.Transition && event.parameters[EventParameterKeys.EVENT] == eventName) {
			seen.complete(Unit)
		}
	}
}
