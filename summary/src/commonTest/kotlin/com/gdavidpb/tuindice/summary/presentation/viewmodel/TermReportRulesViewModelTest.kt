package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
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
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_failed_record_title
import tuindice.summary.generated.resources.term_report_failed_title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// The rules of the screen around the report: what the bar offers, what the summary's line keeps, and what a repeated
// ask or a cause says.
class TermReportRulesViewModelTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theReportIsAskedAgainOnceShown_then_nothingChanges_andNoTransitionIsInvalid() = runTest {
		withMainDispatcher { dispatchers ->
			val events = mutableListOf<AppEvent>()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
				options = TermReportViewModelOptions(eventPublisher = RecordingEvents(events))
			)
			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.Content>().first() }

			viewModel.openAction(insightId = "term-1:2")

			assertIs<TermReport.State.Content>(viewModel.state.value)
			assertEquals(emptyList(), events.filterIsInstance<AppEvent.InvalidTransition>())
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theServiceHasNoReport_then_theLineStaysForIt() = runTest {
		withMainDispatcher { dispatchers ->
			val seen = ControllableTermReportSeenRepository()
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(),
				options = TermReportViewModelOptions(seen = seen)
			)

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { viewModel.state.filterIsInstance<TermReport.State.NoReport>().first() }

			assertEquals(emptySet<String>(), seen.seenIdsFlow.value)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theRecordIsNotReady_then_theFailedSaysThatTheReportWillComeWithIt() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
				options = TermReportViewModelOptions(prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = false))
			)

			viewModel.openAction(insightId = "term-1:2")
			val failed = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Failed>().first()
			}

			assertEquals(UiText.Resource(Res.string.term_report_failed_record_title), failed.title)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theServiceCannotBeReached_then_theFailedSaysSo() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ControllableAssistantInsightsRepository(
					refreshFailure = IllegalStateException("network is unreachable")
				)
			)

			viewModel.openAction(insightId = "term-1:2")
			val failed = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Failed>().first()
			}

			assertEquals(UiText.Resource(Res.string.term_report_failed_title), failed.title)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theTermIsInProgress_then_theBarHasNoShare_andAShareIsIgnored() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			)
			viewModel.openAction(insightId = "term-1:2")
			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Content>().first()
			}

			assertNull(content.topBarConfig)

			viewModel.shareAction()

			assertIs<TermReport.State.Content>(viewModel.state.value)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theTermIsClosedWithAnIndex_then_theBarOffersTheShare() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createTermReportViewModel(
				dispatchers = dispatchers,
				insights = ManualRatingsInsightsRepository(
					listOf(termReportInsight(id = "term-1:2", termKey = "2026-JAN_MAR"))
				)
			)
			viewModel.openAction(insightId = "term-1:2")

			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Content>().first()
			}

			assertEquals(TopBarConfig.TermReport, content.topBarConfig)
		}
	}

	private class RecordingEvents(
		private val events: MutableList<AppEvent>
	) : EventPublisher {
		override fun publish(event: AppEvent) {
			events += event
		}
	}

	private companion object {
		const val TIMEOUT_MILLIS = 5_000L
	}
}
