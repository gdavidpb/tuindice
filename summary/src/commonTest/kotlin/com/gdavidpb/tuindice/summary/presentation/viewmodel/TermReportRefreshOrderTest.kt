package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.base.domain.repository.AssistantInsightsRepository
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.testing.createTermReportViewModel
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// The report on the device is read after the service has answered the refresh, never before: the screen holds the
// loading state until then, and the report comes with the refresh's answer.
class TermReportRefreshOrderTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theReportIsOpened_then_theReportOnTheDeviceIsShownOnlyOnceTheServiceHasAnswered() = runTest {
		withMainDispatcher { dispatchers ->
			val insights = GatedRefreshInsightsRepository(listOf(termReportInsight(id = "term-1:2")))
			val viewModel = createTermReportViewModel(dispatchers = dispatchers, insights = insights)

			// The machine runs while the screen collects its state, as it does on screen.
			backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)

			viewModel.openAction(insightId = "term-1:2")
			withTimeout(TIMEOUT_MILLIS) { insights.refreshStarted.await() }

			// The service has not answered yet: the report that is already on the device is not on screen.
			assertIs<TermReport.State.Loading>(viewModel.state.value)

			insights.refreshAnswer.complete(Unit)

			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<TermReport.State.Content>().first()
			}

			assertEquals("term-1:2", content.report.insightId)
		}
	}

	// The refresh waits until the test answers it, so that the order of the two reads can be seen.
	private class GatedRefreshInsightsRepository(
		initialInsights: List<AssistantTermInsight>
	) : AssistantInsightsRepository {
		val insightsFlow = MutableStateFlow(initialInsights)

		val refreshStarted = CompletableDeferred<Unit>()

		val refreshAnswer = CompletableDeferred<Unit>()

		override fun observeInsightsFlow(): Flow<List<AssistantTermInsight>> = insightsFlow

		override suspend fun refreshInsights() {
			refreshStarted.complete(Unit)

			refreshAnswer.await()
		}

		override suspend fun rateInsight(insightId: String, useful: Boolean) = Unit
	}

	private companion object {
		const val TIMEOUT_MILLIS = 5_000L
	}
}
