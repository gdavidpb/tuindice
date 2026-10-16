package com.gdavidpb.tuindice.summary.presentation.route

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryEffect
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryScreenLinks
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

// What the Statistics screen asks for reaches the same connections the hub uses for the same target.
class SummaryStatisticsNavigationTest {
	@Test
	fun when_theRecordIsAsked_then_theRecordSectionIsOpened() {
		val calls = mutableListOf<String>()

		recordingNavigation(calls).leadTo(SummaryHubTarget.Record.toSummaryEffect())

		assertEquals(listOf("section:RECORD"), calls)
	}

	@Test
	fun when_theEvaluationsAreAsked_then_theEvaluationsSectionIsOpenedOnNoWeek() {
		val calls = mutableListOf<String>()

		recordingNavigation(calls).leadTo(SummaryHubTarget.Evaluations.toSummaryEffect())

		assertEquals(listOf("section:${MainSection.EVALUATIONS.name}"), calls)
	}

	@Test
	fun when_aWeekOfEvaluationsIsAsked_then_theWeekAndItsEvaluationAreAsked() {
		val calls = mutableListOf<String>()
		val target = SummaryHubTarget.EvaluationsWeek(
			weekStart = LocalDate(2026, 10, 5),
			evaluationId = "evaluation-1"
		)

		recordingNavigation(calls).leadTo(target.toSummaryEffect())

		assertEquals(listOf("week:2026-10-05:evaluation-1"), calls)
	}

	@Test
	fun when_thePensumIsAsked_then_thePensumSectionIsOpened() {
		val calls = mutableListOf<String>()

		recordingNavigation(calls).leadTo(SummaryHubTarget.Pensum.toSummaryEffect())

		assertEquals(listOf("section:${MainSection.PENSUM.name}"), calls)
	}

	@Test
	fun when_theStatisticsAreAsked_then_theStatisticsScreenIsPushed() {
		val calls = mutableListOf<String>()

		recordingNavigation(calls).leadTo(SummaryHubTarget.Statistics.toSummaryEffect())

		assertEquals(listOf("statistics"), calls)
	}

	private fun recordingNavigation(calls: MutableList<String>): SummaryNavDependencies {
		return SummaryNavDependencies(
			screens = SummaryScreenLinks(
				onNavigateToSection = { section -> calls += "section:${section.name}" },
				onNavigateToEvaluationsWeek = { weekStart, evaluationId -> calls += "week:$weekStart:$evaluationId" },
				onNavigateToStatistics = { calls += "statistics" }
			)
		)
	}
}
