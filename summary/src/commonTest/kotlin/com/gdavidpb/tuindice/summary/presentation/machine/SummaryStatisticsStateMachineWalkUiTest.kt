package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryStatisticsItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test

/**
 * Seeded random walk for the Statistics machine. UiTest-suffixed like the Summary walk, so it runs on the iOS gate with
 * the Compose-backed resources; the static validators stay on the host in SummaryStatisticsStateMachineContractTest.
 */
class SummaryStatisticsStateMachineWalkUiTest {
	@Test
	fun when_seededRandomWalkRuns_then_machineSurvives() = runTest {
		runStatisticsMachineWalk(seed = 0x57A7157)
	}

	@Test
	fun when_alternateSeededRandomWalkRuns_then_machineSurvives() = runTest {
		runStatisticsMachineWalk(seed = 0x5EEDBEEF)
	}

	private suspend fun TestScope.runStatisticsMachineWalk(seed: Long) {
		val screenMachine = SummaryStatisticsMachine(
			observeSummaryHubUseCase = createObserveSummaryHubUseCase()
		)

		val statistics = statisticsHubOf(
			statistics = statisticsOf(
				terms = listOf(
					statisticsTerm(year = 2024),
					statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JUL_AUG, index = 3.5)
				)
			)
		).toSummaryStatisticsItem()!!

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			seed = seed,
			sampleEvents = listOf(
				SummaryStatistics.Action.ObserveStatistics,
				SummaryStatistics.Action.Resumed,
				SummaryStatistics.Action.OpenTarget(target = SummaryHubTarget.Record),
				SummaryStatistics.Action.OpenTarget(target = SummaryHubTarget.Evaluations),
				SummaryStatistics.Action.OpenTarget(
					target = SummaryHubTarget.EvaluationsWeek(
						weekStart = LocalDate(2026, 10, 12),
						evaluationId = "evaluation-1"
					)
				),
				SummaryStatisticsInternalEvent.StatisticsObserved(statistics = statistics),
				SummaryStatisticsInternalEvent.StatisticsUnavailable
			),
			coroutineScope = backgroundScope,
			// Conservative floor, as the Summary walk: raise it to the observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}
}
