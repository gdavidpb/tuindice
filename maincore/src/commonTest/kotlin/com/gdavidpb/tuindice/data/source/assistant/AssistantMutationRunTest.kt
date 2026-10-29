package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcomeKind
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.gdavidpb.tuindice.chatcontract.intent.MutationOutcome as ItemOutcome

@OptIn(ExperimentalCoroutinesApi::class)
class AssistantMutationRunTest {
	@Test
	fun settle_aSlowCall_leavesTheWaitWithTheTimeoutCountedFromTheStart() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		val slowCall = AssistantMutation("m1") {
			delay(SLOW_CALL_MILLIS)
			UseCaseState.Data(Unit)
		}

		val execution = settleMutations(
			mutations = listOf(slowCall),
			outcomes = outcomes,
			queued = queuedMutationsOf(StandardTestDispatcher(testScheduler))
		)

		assertEquals(OutcomeKind.QUEUED, execution.outcome)
		assertEquals(MUTATION_OUTCOME_TIMEOUT_MILLIS, testScheduler.currentTime)
	}

	@Test
	fun settle_aCallLongerThanTheTimeout_isNotCutShortAndItsMutationIsQueued() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		var completed = false
		val longCall = AssistantMutation("m1") {
			delay(LONG_CALL_MILLIS)
			completed = true
			UseCaseState.Data(Unit)
		}

		val execution = settleMutations(
			mutations = listOf(longCall),
			outcomes = outcomes,
			queued = queuedMutationsOf(StandardTestDispatcher(testScheduler))
		)

		assertTrue(completed)
		assertEquals(OutcomeKind.QUEUED, execution.outcome)
		assertEquals(LONG_CALL_MILLIS, testScheduler.currentTime)
	}

	@Test
	fun settle_anOutcomeThatCameInDuringACallLongerThanTheTimeout_isTaken() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		val settlingCall = AssistantMutation("m1") {
			outcomes.emit(MutationOutcome("m1", MutationOutcomeKind.CONFIRMED, null))
			delay(LONG_CALL_MILLIS)
			UseCaseState.Data(Unit)
		}

		val execution = settleMutations(
			mutations = listOf(settlingCall),
			outcomes = outcomes,
			queued = queuedMutationsOf(StandardTestDispatcher(testScheduler))
		)

		assertEquals(OutcomeKind.CONFIRMED, execution.outcome)
		assertEquals(LONG_CALL_MILLIS, testScheduler.currentTime)
	}

	@Test
	fun settle_anOutcomeThatArrivesAfterASlowCall_isTakenWithinTheTimeout() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		val slowCall = AssistantMutation("m1") {
			delay(SLOW_CALL_MILLIS)
			UseCaseState.Data(Unit)
		}
		launch {
			delay(LATE_OUTCOME_MILLIS)
			outcomes.emit(MutationOutcome("m1", MutationOutcomeKind.CONFIRMED, null))
		}

		val execution = settleMutations(
			mutations = listOf(slowCall),
			outcomes = outcomes,
			queued = queuedMutationsOf(StandardTestDispatcher(testScheduler))
		)

		assertEquals(OutcomeKind.CONFIRMED, execution.outcome)
		assertEquals(LATE_OUTCOME_MILLIS, testScheduler.currentTime)
	}

	// The budget is armed before the first call. This call starts an outcome that comes in at the instant the budget
	// ends: after the run has stopped waiting, and before its listener forwards it. The run hands the mutation over at
	// that moment, so the outcome must be adopted by the session and complete the follower of the mutation.
	@Test
	fun settle_anOutcomeThatArrivesAtTheDeadline_isAdoptedAndCompletesItsFollower() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		val clock = FixedTestClock(nowMillis = 0L)
		val queued = queuedMutationsOf(StandardTestDispatcher(testScheduler), clock)
		val deadlineCall = AssistantMutation("m1") {
			launch {
				delay(MUTATION_OUTCOME_TIMEOUT_MILLIS)
				outcomes.emit(MutationOutcome("m1", MutationOutcomeKind.CONFIRMED, null))
			}
			UseCaseState.Data(Unit)
		}

		val execution = settleMutations(
			mutations = listOf(deadlineCall),
			outcomes = outcomes,
			queued = queued
		)

		val followed = mutableListOf<ItemOutcome>()
		queued.followQueued(listOf("m1"), untilMillis = FOLLOW_UNTIL_MILLIS) { settled -> followed += settled }
		testScheduler.advanceUntilIdle()

		assertEquals(OutcomeKind.QUEUED, execution.outcome)
		assertEquals(listOf(ItemOutcome("m1", OutcomeKind.CONFIRMED)), followed)
	}

	@Test
	fun settle_aRunThatSettlesInTimeStopsItsSubscription() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		val quickCall = AssistantMutation("m1") {
			outcomes.emit(MutationOutcome("m1", MutationOutcomeKind.CONFIRMED, null))
			UseCaseState.Data(Unit)
		}

		val execution = settleMutations(
			mutations = listOf(quickCall),
			outcomes = outcomes,
			queued = queuedMutationsOf(StandardTestDispatcher(testScheduler))
		)
		testScheduler.advanceUntilIdle()

		assertEquals(OutcomeKind.CONFIRMED, execution.outcome)
		assertEquals(0, outcomes.subscriptionCount.value)
	}

	@Test
	fun settle_aQueuedRunKeepsItsSubscriptionOnlyWhileItsFollowLasts() = runTest {
		val outcomes = MutableSharedFlow<MutationOutcome>(extraBufferCapacity = 64)
		val slowCall = AssistantMutation("m1") {
			delay(SLOW_CALL_MILLIS)
			UseCaseState.Data(Unit)
		}

		val execution = settleMutations(
			mutations = listOf(slowCall),
			outcomes = outcomes,
			queued = queuedMutationsOf(StandardTestDispatcher(testScheduler))
		)

		assertEquals(OutcomeKind.QUEUED, execution.outcome)
		assertEquals(1, outcomes.subscriptionCount.value)

		// The follow ends at its limit, and with it the subscription that the session took over from the run.
		testScheduler.advanceUntilIdle()

		assertEquals(0, outcomes.subscriptionCount.value)
	}

	private companion object {
		const val SLOW_CALL_MILLIS = 6_000L
		const val LATE_OUTCOME_MILLIS = 7_000L
		const val LONG_CALL_MILLIS = 12_000L
		const val FOLLOW_UNTIL_MILLIS = 60_000L
	}
}
