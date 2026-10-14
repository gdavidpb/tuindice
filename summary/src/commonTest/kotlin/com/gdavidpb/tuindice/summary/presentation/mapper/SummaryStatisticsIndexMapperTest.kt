package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SummaryStatisticsIndexMapperTest {
	@Test
	fun when_termsHaveAnIndex_then_onlyTheBestTermsPointIsMarked() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2023, index = 3.5),
				statisticsTerm(year = 2024, index = 4.5),
				statisticsTerm(year = 2025, index = 4.0)
			)
		)

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertEquals(listOf(false, true, false), data.points.map { point -> point.isBest })
	}

	@Test
	fun when_noTermIsRanked_then_noPointIsMarked() {
		val statistics = statisticsOf(terms = listOf(statisticsTerm(year = 2024, index = 4.0)))

		val index = statisticsHubOf(statistics = statistics.copy(bestTerm = null)).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertEquals(listOf(false), data.points.map { point -> point.isBest })
	}
}
