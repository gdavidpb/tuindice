package com.gdavidpb.tuindice.summary.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.TransitionResult
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.presentation.machine.TermReportInternalEvent
import com.gdavidpb.tuindice.summary.presentation.mapper.termReportMissingState
import com.gdavidpb.tuindice.summary.testing.ManualRatingsInsightsRepository
import com.gdavidpb.tuindice.summary.testing.createTermReportViewModel
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TermReportNoReportTransitionsTest {
	// A refusal that arrives once the record says the report is missing has no thumb on screen to take back. It changes
	// nothing, and it is a transition the machine knows: it is not reported as an invalid one.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aRefusalArrivesWhenTheReportIsMissing_then_nothingChanges_andNoTransitionIsInvalid() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository()
			)
			val missing = termReportMissingState()

			val result = viewModel.machine.process(
				state = missing,
				event = TermReportInternalEvent.FeedbackRejected(thumb = 1, rejected = true)
			)

			val transitioned = assertIs<TransitionResult.Transitioned<TermReport.State>>(result)
			assertEquals(missing, transitioned.toState)
		}
	}

	// The end of a vote that comes once the record says the report is missing changes nothing, and is not invalid either.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aVoteEndsWhenTheReportIsMissing_then_nothingChanges_andNoTransitionIsInvalid() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository()
			)
			val missing = termReportMissingState()

			val result = viewModel.machine.process(
				state = missing,
				event = TermReportInternalEvent.ThumbSettled(thumb = 1)
			)

			val transitioned = assertIs<TransitionResult.Transitioned<TermReport.State>>(result)
			assertEquals(missing, transitioned.toState)
		}
	}
}
