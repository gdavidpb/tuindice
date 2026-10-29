package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics
import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.contract.SummaryStatistics
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import com.gdavidpb.tuindice.summary.domain.model.SummaryStatistics as SummaryStatisticsModel

class SummaryStatisticsAssistantContextTest {
	@Test
	fun content_namesTheSubjectsByOutcomeTheCreditsAndThePaceTheScreenDraws() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2025)),
			subjects = SubjectOutcomes(
				approved = 9,
				failed = 2,
				withdrawn = 1,
				noResult = 0,
				approvedFirstTry = 7,
				firstTryBase = 9,
				repeated = 1
			)
		).copy(
			pace = PaceStatistics(averageCredits = 14.5, remainingTerms = 3),
			pensumProgress = SummaryCreditsProgress(approvedCredits = 156, totalCredits = 240)
		)

		val context = contentOf(statistics).assistantContext

		assertEquals(AssistantScreen.STATISTICS, context.screen)
		assertEquals(emptyList<AssistantEntityRef>(), context.entities)
		assertEquals(
			mapOf(
				"approvedSubjects" to "9",
				"failedSubjects" to "2",
				"withdrawnSubjects" to "1",
				"approvedCredits" to "156",
				"averageApprovedCreditsPerTerm" to "14.5"
			),
			context.visibleFields
		)
		assertEquals(emptyMap<String, String>(), context.filters)
	}

	@Test
	fun content_withNothingToCount_namesNoFigure() {
		val context = contentOf(statisticsOf()).assistantContext

		assertEquals(AssistantScreen.STATISTICS, context.screen)
		assertEquals(emptyMap<String, String>(), context.visibleFields)
	}

	@Test
	fun otherStates_describeTheScreenAlone() {
		val expected = AssistantScreenContext(screen = AssistantScreen.STATISTICS)

		assertEquals(expected, SummaryStatistics.State.Idle.assistantContext)
		assertEquals(expected, SummaryStatistics.State.Loading.assistantContext)
		assertEquals(
			expected,
			SummaryStatistics.State.Unavailable(title = UiText.Empty, message = UiText.Empty).assistantContext
		)
	}

	// The content the Statistics screen shows for the statistics of the hub.
	private fun contentOf(statistics: SummaryStatisticsModel): SummaryStatistics.State.Content {
		val item = assertNotNull(statisticsHubOf(statistics = statistics).toSummaryStatisticsItem())

		return SummaryStatistics.State.Content(statistics = item)
	}
}
