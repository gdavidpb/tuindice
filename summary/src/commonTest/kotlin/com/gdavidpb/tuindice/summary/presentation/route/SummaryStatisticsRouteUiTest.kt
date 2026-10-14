package com.gdavidpb.tuindice.summary.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryStatisticsMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryScreenLinks
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryStatisticsViewModel
import com.gdavidpb.tuindice.summary.testing.ControllableVisibleAcademicRecordRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.hubClosedTerm
import com.gdavidpb.tuindice.summary.testing.hubPassedAttempt
import com.gdavidpb.tuindice.summary.testing.hubRecord
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.coroutines.Dispatchers
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class SummaryStatisticsRouteUiTest {
	@Test
	fun when_theStatisticsAreRead_then_theScreenShowsItsBlocks() = runTuIndiceUiTest {
		val viewModel = createStatisticsViewModel()

		setTuIndiceTestContent {
			SummaryStatisticsRoute(
				navigation = SummaryNavDependencies(),
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is SummaryStatistics.State.Content
		}
		waitForIdle()

		onAllNodesWithTag(SummaryUiTags.StatisticsIndexBlock, useUnmergedTree = true).assertCountEquals(1)
		onAllNodesWithTag(SummaryUiTags.StatisticsPaceBlock, useUnmergedTree = true).assertCountEquals(1)
	}

	@Test
	fun when_aTargetIsOpened_then_theRouteTakesTheStudentThroughTheHostConnection() = runTuIndiceUiTest {
		val viewModel = createStatisticsViewModel()
		val sections = mutableListOf<MainSection>()

		setTuIndiceTestContent {
			SummaryStatisticsRoute(
				navigation = SummaryNavDependencies(
					screens = SummaryScreenLinks(
						onNavigateToSection = { section -> sections += section }
					)
				),
				viewModel = viewModel
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			viewModel.state.value is SummaryStatistics.State.Content
		}
		runOnIdle {
			viewModel.openTargetAction(SummaryHubTarget.Record)
		}
		waitUntil(timeoutMillis = 2_000) {
			sections.isNotEmpty()
		}

		assertEquals(listOf(MainSection.RECORD), sections)
	}

	private fun createStatisticsViewModel(
		record: AcademicRecord = closedRecord(),
		dispatchers: TuIndiceDispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
	): SummaryStatisticsViewModel {
		return SummaryStatisticsViewModel(
			screenMachine = SummaryStatisticsMachine(
				observeSummaryHubUseCase = createObserveSummaryHubUseCase(
					record = ControllableVisibleAcademicRecordRepository(initialRecord = record)
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
}
