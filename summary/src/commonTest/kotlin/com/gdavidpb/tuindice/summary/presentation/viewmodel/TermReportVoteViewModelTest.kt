package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportFeedbackRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.ManualRatingsInsightsRepository
import com.gdavidpb.tuindice.summary.testing.TermReportViewModelOptions
import com.gdavidpb.tuindice.summary.testing.createTermReportViewModel
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
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

			// The first request is replaced: its answer, when it comes, is not waited for.
			insights.requests.first().refuse()
			insights.requests.last().accept()

			assertEquals(false, assertIs<TermReport.State.Content>(viewModel.state.value).feedback)
			assertEquals(mapOf("term-1:2" to false), feedback.feedbacksFlow.value)
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

	private suspend fun openReport(viewModel: TermReportViewModel) {
		viewModel.openAction(insightId = "term-1:2")

		withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }
	}

	private companion object {
		const val TIMEOUT_MILLIS = 5_000L
	}
}
