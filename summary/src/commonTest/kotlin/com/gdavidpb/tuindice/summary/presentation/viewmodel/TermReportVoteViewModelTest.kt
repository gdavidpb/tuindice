package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.ManualRatingsInsightsRepository
import com.gdavidpb.tuindice.summary.testing.TermReportViewModelOptions
import com.gdavidpb.tuindice.summary.testing.createTermReportViewModel
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// The thumbs of the report: what the screen shows, what the service confirmed, and what the device keeps of it.
class TermReportVoteViewModelTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aThumbIsTappedTwice_then_onlyOnePostIsSent() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val viewModel = createTermReportViewModel(dispatchers = dispatchers, insights = insights)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = true)

			assertEquals(listOf(true), insights.requests.map { request -> request.useful })
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theThumbsAreTappedFast_then_theLastOneIsShownAndSaved_andTheEarlierAnswerChangesNothing() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = false)

			// The first thumb is replaced on the screen: its answer is not waited for. The second one goes to the service
			// only once the first one has been answered.
			insights.requests.first().refuse()
			awaitThumbsSent(insights = insights, count = 2)
			insights.requests.last().accept()

			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to false), feedback.feedbacksFlow.value)
		}
	}

	// Two thumbs in a row: the second one goes to the service only once the first one has been answered, so the last
	// thumb is the last one the service receives, and the device keeps that same thumb.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_twoThumbsAreGivenInARow_then_theSecondReachesTheServiceOnlyAfterTheFirstIsAnswered() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = false)

			// The screen shows the second thumb at once; the service has only the first one, not answered yet.
			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(listOf(true), insights.requests.map { request -> request.useful })

			// The first thumb is answered late, after the second one was given: only now the second one is sent.
			insights.requests.first().accept()
			awaitThumbsSent(insights = insights, count = 2)

			assertEquals(listOf(true, false), insights.requests.map { request -> request.useful })

			insights.requests.last().accept()
			yield()

			assertEquals(mapOf("term-1:2" to false), feedback.feedbacksFlow.value)
			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
		}
	}

	// A thumb the screen has moved away from is still sent and kept: the service and the device say the same of it.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aThumbReplacedInFlightIsAcceptedAnyway_then_itIsKept_andTheNextRefusalGivesItBack() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = false)

			// The service takes the first thumb, which the screen no longer waits for, and then refuses the second one.
			insights.requests.first().accept()
			yield()

			assertEquals(mapOf("term-1:2" to true), feedback.feedbacksFlow.value)

			awaitThumbsSent(insights = insights, count = 2)
			insights.requests.last().refuse()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to true), feedback.feedbacksFlow.value)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aThumbIsRefusedAndTheNextOneIsOk_then_theNextOneIsShown() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			insights.requests.single().refuse()

			assertNull(assertIs<TermReport.State.Content>(viewModel.state.value).feedback)

			viewModel.rateAction(useful = false)
			insights.requests.last().accept()

			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to false), feedback.feedbacksFlow.value)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aThumbIsRefused_then_theLastConfirmedOneComesBack_notTheOneBeforeTheTap() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository(mapOf("term-1:2" to true))
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = false)
			insights.requests.single().refuse()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to true), feedback.feedbacksFlow.value)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theReportIsReopened_then_theConfirmedVoteIsShown() = runTest {
		withMainDispatcher { dispatchers ->
			val feedback = ControllableTermReportFeedbackRepository(mapOf("term-1:2" to false))
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
				options = TermReportViewModelOptions(feedback = feedback)
			)

			openReport(viewModel)

			val content = assertIs<TermReport.State.Content>(viewModel.state.value)
			assertEquals(false, content.feedback)
			assertEquals(false, content.report.confirmedFeedback)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theReportIsOpenedAndTheRecordIsNotReady_then_itIsNotSeenYet_andIsSeenOnceItIsShown() = runTest {
		withMainDispatcher { dispatchers ->
			val seen = ControllableTermReportSeenRepository()
			val prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false)
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
				options = TermReportViewModelOptions(seen = seen, prerequisite = prerequisite)
			)

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Failed>().first() }

			assertEquals(emptySet<String>(), seen.seenIdsFlow.value)

			prerequisite.stateFlow.value = prerequisite.stateFlow.value.copy(isReady = true)

			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }

			assertEquals(setOf("term-1:2"), seen.seenIdsFlow.value)
		}
	}

	// The first thumb is refused in flight: the second one stays on screen, and it is kept once it is accepted.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theFirstThumbIsRefusedInFlight_then_theSecondStays_andIsKept() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = false)

			insights.requests.first().refuse()
			awaitThumbsSent(insights = insights, count = 2)
			yield()

			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)

			insights.requests.last().accept()
			yield()

			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to false), feedback.feedbacksFlow.value)
		}
	}

	// A thumb the student has moved away from is still sent. When the service refuses it, the flow that sent it reports
	// the refusal, once; the screen keeps the last thumb.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aMovedAwayThumbIsRefused_then_itIsReportedOnce_andTheLastOneStays() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val reporting = RecordingReportingRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(reporting = reporting)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = false)

			insights.requests.first().refuse()
			awaitThumbsSent(insights = insights, count = 2)
			yield()

			assertEquals(1, reporting.loggedExceptions.size)
			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)

			insights.requests.last().accept()
			yield()

			assertEquals(1, reporting.loggedExceptions.size)
			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
		}
	}

	// The thumbs go not useful, then useful, over a confirmed useful one. The first is accepted while the second one is in
	// flight: the screen keeps useful, the last thumb the student gave.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_anOlderThumbIsAcceptedInFlight_then_theLastOneStays() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository(mapOf("term-1:2" to true))
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = false)
			viewModel.rateAction(useful = true)

			insights.requests.first().accept()
			awaitThumbsSent(insights = insights, count = 2)
			yield()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)

			insights.requests.last().accept()
			yield()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to true), feedback.feedbacksFlow.value)
		}
	}

	// Thumbs useful, not useful, useful, with no confirmed one: the first is refused while the last one is pending. The
	// refusal of an earlier thumb gives nothing back; the last thumb stays on screen until it is accepted.
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_anEarlierThumbIsRefusedWhileTheLastOneIsPending_then_theLastOneStays() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val feedback = ControllableTermReportFeedbackRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = insights,
				options = TermReportViewModelOptions(feedback = feedback)
			)
			openReport(viewModel)

			viewModel.rateAction(useful = true)
			viewModel.rateAction(useful = false)
			viewModel.rateAction(useful = true)

			insights.requests.first().refuse()
			awaitThumbsSent(insights = insights, count = 2)
			yield()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)

			insights.requests[1].accept()
			awaitThumbsSent(insights = insights, count = 3)
			yield()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)

			insights.requests.last().accept()
			yield()

			assertEquals(true, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to true), feedback.feedbacksFlow.value)
		}
	}

	// Waits until the service has been asked for [count] thumbs; one that is never sent ends the wait in a timeout.
	private suspend fun awaitThumbsSent(insights: ManualRatingsInsightsRepository, count: Int) {
		withTimeout(TIMEOUT_MILLIS) {
			insights.requestCount.first { sent -> sent >= count }
		}
	}

	private suspend fun openReport(viewModel: TermReportViewModel) {
		viewModel.openAction(insightId = "term-1:2")

		withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }
	}

	private companion object {
		const val TIMEOUT_MILLIS = 5_000L
	}
}
