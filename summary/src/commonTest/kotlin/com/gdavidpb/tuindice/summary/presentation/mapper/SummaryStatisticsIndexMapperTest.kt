package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

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

	@Test
	fun when_twoRegularTermsAreRanked_then_theWorstPointIsMarkedAsWellAsTheBest() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2023, index = 4.5),
				statisticsTerm(year = 2024, index = 3.5)
			)
		)

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertEquals(listOf(true, false), data.points.map { point -> point.isBest })
		assertEquals(listOf(false, true), data.points.map { point -> point.isWorst })
	}

	@Test
	fun when_onlyOneRegularTermIsRanked_then_noPointIsMarkedAsWorst() {
		val statistics = statisticsOf(terms = listOf(statisticsTerm(year = 2024, index = 4.0)))

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertEquals(listOf(false), data.points.map { point -> point.isWorst })
		assertNull(data.worst)
	}

	@Test
	fun when_everyRankedTermHasTheSameIndex_then_theTermIsOnlyTheBest() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2023, index = 4.0),
				statisticsTerm(year = 2024, index = 4.0)
			)
		)

		val index = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.index

		val data = assertIs<SummaryStatisticsIndexItem.Content.Data>(index.content)
		assertEquals(1, data.points.count { point -> point.isBest })
		assertEquals(listOf(false, false), data.points.map { point -> point.isWorst })
		assertNull(data.worst)
	}
}
