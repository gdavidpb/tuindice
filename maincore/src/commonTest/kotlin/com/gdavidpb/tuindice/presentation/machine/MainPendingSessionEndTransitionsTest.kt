package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.contract.PendingSessionEnd
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createMainViewModel
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// The way to the sign-in is owed whatever else happens to the state: no transition between states
// drops it, and only the interface attending it clears it.
class MainPendingSessionEndTransitionsTest {
	private val pending = PendingSessionEnd(id = 3, message = "notice")
	private val outdated = OutdatedAppState(minimumVersionCode = 52)

	@Test
	fun everyTransitionBetweenStates_keepsThePendingEnd() = runTest {
		val machine = createMainViewModel().machine
		val states = listOf(
			Main.State.Starting(pendingSessionEnd = pending),
			Main.State.Content(startDestination = SummaryDestination.Summary, pendingSessionEnd = pending),
			Main.State.Failed(pendingSessionEnd = pending),
			Main.State.OutdatedApp(outdatedAppState = outdated, pendingSessionEnd = pending),
			Main.State.AppUnavailable(
				notice = AppAvailabilityNotice(enabled = true, title = "t", message = "m"),
				pendingSessionEnd = pending
			)
		)
		val events = listOf(
			MainInternalEvent.StartUpStarting,
			MainInternalEvent.StartUpCompleted(startDestination = SummaryDestination.Summary),
			// The notice the start-up found goes through the rule of the live one: an end that already
			// carries a message keeps it.
			MainInternalEvent.StartUpCompleted(
				startDestination = SummaryDestination.Summary,
				sessionNotice = SessionNoticeText(stamp = 9, message = "found at the start-up")
			),
			MainInternalEvent.StartUpFailed(noServices = false),
			MainInternalEvent.AppUnavailableResolved(
				notice = AppAvailabilityNotice(enabled = true, title = "t", message = "m")
			),
			MainInternalEvent.OutdatedAppResolved(outdatedAppState = outdated),
			MainInternalEvent.OutdatedAppObserved(outdatedAppState = outdated),
			Main.Action.ShowOutdatedApp(outdatedAppState = outdated)
		)

		for (state in states) {
			for (event in events) {
				val result = machine.process(state, event)
				val next = assertIs<TransitionResult.Transitioned<Main.State>>(result).toState

				assertEquals(pending, next.pendingSessionEnd, "${event::class.simpleName} from ${state::class.simpleName}")
			}
		}
	}

	@Test
	fun attendingTheEnd_clearsItInAnyState() = runTest {
		val machine = createMainViewModel().machine
		val state = Main.State.Failed(pendingSessionEnd = pending)

		val attended = machine.process(state, Main.Action.SessionEndHandled(id = pending.id))
		val stale = machine.process(state, Main.Action.SessionEndHandled(id = pending.id + 1))

		assertNull(assertIs<TransitionResult.Transitioned<Main.State>>(attended).toState.pendingSessionEnd)
		assertEquals(pending, assertIs<TransitionResult.Transitioned<Main.State>>(stale).toState.pendingSessionEnd)
	}
}
