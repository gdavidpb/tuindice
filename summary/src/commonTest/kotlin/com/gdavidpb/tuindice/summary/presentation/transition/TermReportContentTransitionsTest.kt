package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.testing.ManualRatingsInsightsRepository
import com.gdavidpb.tuindice.summary.testing.createTermReportViewModel
import com.gdavidpb.tuindice.summary.testing.termReportItemFixture
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// The thumb on screen while the student's last vote is in flight.
class TermReportContentTransitionsTest {
	// A report read while the student's vote is in flight: the thumb on screen stays, and the report is updated.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aReportIsReadWhileAVoteIsPending_then_theThumbOnScreenStays() = runTest {
		val state = content(confirmedFeedback = true, feedback = true, pendingThumb = 2)

		val next = transitionOf(
			state = state,
			event = TermReportInternalEvent.ReportObserved(report = termReportItemFixture(confirmedFeedback = false))
		)

		val content = assertIs<TermReport.State.Content>(next)
		assertEquals(true, content.feedback)
		assertEquals(false, content.report.confirmedFeedback)
	}

	// With no vote in flight, a report read shows the thumb the service confirmed.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aReportIsReadWithNoVotePending_then_theConfirmedThumbShows() = runTest {
		val state = content(confirmedFeedback = true, feedback = true, pendingThumb = null)

		val next = transitionOf(
			state = state,
			event = TermReportInternalEvent.ReportObserved(report = termReportItemFixture(confirmedFeedback = false))
		)

		assertEquals(false, assertIs<TermReport.State.Content>(next).feedback)
	}

	// The last vote ending clears the thumb that waits for it.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theLastVoteEnds_then_nothingIsPendingAnymore() = runTest {
		val state = content(confirmedFeedback = false, feedback = false, pendingThumb = 2)

		val next = transitionOf(state = state, event = TermReportInternalEvent.ThumbSettled(thumb = 2))

		val content = assertIs<TermReport.State.Content>(next)
		assertNull(content.pendingThumb)
		assertEquals(false, content.feedback)
	}

	// An earlier vote ending while the last one is in flight changes nothing: the last one still waits.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_anEarlierVoteEnds_then_theLastOneIsStillPending() = runTest {
		val state = content(confirmedFeedback = false, feedback = false, pendingThumb = 2)

		val next = transitionOf(state = state, event = TermReportInternalEvent.ThumbSettled(thumb = 1))

		assertEquals(state, next)
	}

	// The service refuses the last thumb, the one on screen: the confirmed thumb comes back.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theLastVoteIsRefused_then_theConfirmedThumbComesBack() = runTest {
		val state = content(confirmedFeedback = false, feedback = true, pendingThumb = 2)

		val next = transitionOf(
			state = state,
			event = TermReportInternalEvent.FeedbackRejected(thumb = 2, rejected = true)
		)

		assertEquals(false, assertIs<TermReport.State.Content>(next).feedback)
	}

	// A refusal of an earlier thumb gives nothing back, even when that thumb is the one on screen.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_anEarlierVoteIsRefused_then_nothingChanges() = runTest {
		val state = content(confirmedFeedback = false, feedback = true, pendingThumb = 3)

		val next = transitionOf(
			state = state,
			event = TermReportInternalEvent.FeedbackRejected(thumb = 1, rejected = true)
		)

		assertEquals(state, next)
	}

	// A refusal that arrives after the student has moved to the other thumb changes nothing: the thumb on screen is
	// the one the student chose last, not the one refused.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theLastVoteIsRefusedAfterTheStudentMovedAway_then_nothingChanges() = runTest {
		val state = content(confirmedFeedback = false, feedback = false, pendingThumb = 2)

		val next = transitionOf(
			state = state,
			event = TermReportInternalEvent.FeedbackRejected(thumb = 2, rejected = true)
		)

		assertEquals(state, next)
	}

	private fun content(
		confirmedFeedback: Boolean?,
		feedback: Boolean?,
		pendingThumb: Int?
	) = TermReport.State.Content(
		report = termReportItemFixture(confirmedFeedback = confirmedFeedback),
		feedback = feedback,
		pendingThumb = pendingThumb
	)

	// The state the machine of the screen moves to on [event], from [state].
	@OptIn(ExperimentalCoroutinesApi::class)
	private suspend fun TestScope.transitionOf(state: TermReport.State, event: Any): TermReport.State {
		var next: TermReport.State? = null

		withMainDispatcher { dispatchers ->
			val machine = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository()
			).machine

			next = assertIs<TransitionResult.Transitioned<TermReport.State>>(machine.process(state, event)).toState
		}

		return checkNotNull(next)
	}
}
