package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.testing.ControllableRecordDataPrerequisiteRepository
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_unavailable_title
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryStatisticsViewModelContractTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theRecordIsReady_then_theScreenReachesTheContent_withTheStatisticsOfTheRecord() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createStatisticsViewModel(
				dispatchers = dispatchers,
				record = closedRecord()
			)

			val content = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<SummaryStatistics.State.Content>().first()
			}

			assertIs<SummaryStatisticsIndexItem.Content.Data>(content.statistics.index.content)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_theRecordIsNotReady_then_theScreenIsUnavailable_andSaysSo() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createStatisticsViewModel(
				dispatchers = dispatchers,
				record = closedRecord(),
				isReady = false
			)

			val unavailable = withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<SummaryStatistics.State.Unavailable>().first()
			}

			assertEquals(UiText.Resource(Res.string.summary_statistics_unavailable_title), unavailable.title)
		}
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun when_aLinkIsOpened_then_theScreenAsksForItsTarget_andStaysAsItIs() = runTest {
		withMainDispatcher { dispatchers ->
			val viewModel = createStatisticsViewModel(
				dispatchers = dispatchers,
				record = closedRecord()
			)
			withTimeout(TIMEOUT_MILLIS) {
				viewModel.state.filterIsInstance<SummaryStatistics.State.Content>().first()
			}

			viewModel.openTargetAction(SummaryHubTarget.Record)

			val effect = withTimeout(TIMEOUT_MILLIS) { viewModel.effect.first() }
			assertEquals(SummaryHubTarget.Record, assertIs<SummaryStatistics.Effect.NavigateTo>(effect).target)
			assertIs<SummaryStatistics.State.Content>(viewModel.state.value)
		}
	}

	private fun createStatisticsViewModel(
		dispatchers: TuIndiceDispatchers,
		record: AcademicRecord?,
		isReady: Boolean = true
	): SummaryStatisticsViewModel {
		return SummaryStatisticsViewModel(
			screenMachine = SummaryStatisticsMachine(
				observeSummaryHubUseCase = createObserveSummaryHubUseCase(
					record = ControllableVisibleAcademicRecordRepository(initialRecord = record),
					prerequisite = ControllableRecordDataPrerequisiteRepository(isReady = isReady)
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	// A record with one closed term that has its index, so the statistics have something to draw.
	private fun closedRecord(): AcademicRecord {
		return hubRecord(
			hubClosedTerm(
				year = 2024,
				attempts = listOf(hubPassedAttempt(code = "CI1")),
				index = 4.0
			)
		)
	}

	private companion object {
		const val TIMEOUT_MILLIS = 3_000L
	}
}
