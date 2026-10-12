package com.gdavidpb.tuindice.presentation.viewmodel

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakePendingChangesRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionInvalidationRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
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
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			val viewModel = createMainViewModel(
				settingsRepository = settingsRepository,
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.awaitContent { true }
				testScheduler.advanceUntilIdle()

				viewModel.effect.test {
					// What the invalidation does: leave the notice, then tell.
					settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
					sessionInvalidationRepository.notifySessionInvalidated()

					val effect = awaitItem()

					assertIs<Main.Effect.SessionInvalidated>(effect)
					assertEquals("Inicia sesión nuevamente", effect.message)
					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	// The notice reaches the route once the content is up, and the copy kept for the next start is
	// taken only when the route says it showed it: a process that dies in between (a background kill)
	// still explains the sign-in screen at the next start.
	@Test
	fun sessionInvalidation_keepsTheCopyForTheNextStart_untilTheRouteSaysItShowedTheNotice() = runTest {
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

				viewModel.effect.test {
					assertIs<Main.Effect.SessionInvalidated>(awaitSessionInvalidated())
					assertEquals(true, settingsRepository.sessionResetNoticePending, "told is not shown")

					viewModel.sessionInvalidationShownAction()
					withTimeout(3_000) {
						while (settingsRepository.sessionResetNoticePending) yield()
					}

					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	// The start-up offers the notice the invalidation left (it is still pending until the route says it
	// showed it) and the live signal of the same invalidation arrives afterwards: the user is told once.
	@Test
	fun sessionNoticeOfferedByTheStartUp_isNotFollowedByTheLiveSignalOfTheSameInvalidation() = runTest {
		withMainDispatcher { dispatchers ->
			val settingsRepository = FakeSettingsRepository(reviewSuggested = false)
			val sessionInvalidationRepository = SessionInvalidationDataSource()
			// What the invalidation did before the start-up: leave the notice, and tell.
			settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
			sessionInvalidationRepository.notifySessionInvalidated()
			val viewModel = createMainViewModel(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				settingsRepository = settingsRepository,
				sessionInvalidationRepository = sessionInvalidationRepository,
				dispatchers = dispatchers
			)

			collectingState(viewModel) {
				viewModel.effect.test {
					val effect = awaitSessionInvalidated()

					assertIs<Main.Effect.SessionInvalidated>(effect)
					// The server ended the session: the notice says that, not that the data could not be read.
					assertEquals("Inicia sesión nuevamente", effect.message)
					assertEquals(true, settingsRepository.sessionResetNoticePending, "offered is not shown")

					// The content is up, and the live signal of the same invalidation with it. The start-up
					// reads the message from the resources, which takes real time: the wait is in real time.
					withContext(Dispatchers.Default) {
						withTimeout(3_000) { viewModel.state.filterIsInstance<Main.State.Content>().first() }
					}
					testScheduler.advanceUntilIdle()
					// The live signal reached the machine while the notice was still pending: it takes the user
					// to the sign-in all the same, and carries no second notice.
					assertNull(awaitSessionInvalidated().let { live -> (live as Main.Effect.SessionInvalidated).message })
					expectNoEvents()

					viewModel.sessionInvalidationShownAction()
					withTimeout(3_000) {
						while (settingsRepository.sessionResetNoticePending) yield()
					}

					cancelAndIgnoreRemainingEvents()
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
				viewModel.effect.test {
					val effect = awaitSessionInvalidated()

					assertIs<Main.Effect.SessionInvalidated>(effect)
					assertEquals(
						"No pudimos leer tus datos y reiniciamos la sesión. Inicia sesión de nuevo.",
						effect.message
					)
					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	// Taking the user to the sign-in never depends on the notice: a notice that was told and never
	// confirmed (the route was cancelled before it could show it) holds the machine's lock, and the
	// invalidation that follows must still navigate, with nothing new to say.
	@Test
	fun anInvalidationAfterANoticeThatWasNeverConfirmed_stillTakesTheUserToTheSignIn() = runTest {
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

				viewModel.effect.test {
					settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
					sessionInvalidationRepository.notifySessionInvalidated()
					val first = awaitSessionInvalidated() as Main.Effect.SessionInvalidated
					assertEquals("Inicia sesión nuevamente", first.message)

					// Nobody confirmed it. The next invalidation navigates, without a second notice.
					sessionInvalidationRepository.notifySessionInvalidated()
					val second = awaitSessionInvalidated() as Main.Effect.SessionInvalidated
					assertNull(second.message, "navigates, and the notice already on its way is not told twice")
					assertEquals(true, settingsRepository.sessionResetNoticePending)

					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	// The route could not show the notice (it was cancelled while it waited for the navigator): it says
	// so, the lock is free and the notice, still persisted, is told with the next invalidation.
	@Test
	fun aNoticeTheRouteCouldNotShow_isOfferedAgainWithTheNextInvalidation() = runTest {
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

				viewModel.effect.test {
					settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)
					sessionInvalidationRepository.notifySessionInvalidated()
					assertEquals("Inicia sesión nuevamente", (awaitSessionInvalidated() as Main.Effect.SessionInvalidated).message)

					viewModel.sessionInvalidationNotShownAction()
					testScheduler.advanceUntilIdle()
					assertEquals(true, settingsRepository.sessionResetNoticePending, "the notice stays pending")

					sessionInvalidationRepository.notifySessionInvalidated()
					assertEquals("Inicia sesión nuevamente", (awaitSessionInvalidated() as Main.Effect.SessionInvalidated).message)

					cancelAndIgnoreRemainingEvents()
				}
			}
		}
	}

	private suspend fun app.cash.turbine.ReceiveTurbine<Main.Effect>.awaitSessionInvalidated(): Main.Effect {
		while (true) {
			val effect = awaitItem()

			if (effect is Main.Effect.SessionInvalidated) return effect
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
