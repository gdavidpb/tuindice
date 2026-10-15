package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.SubjectOutcomes
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_chart_summary
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SummaryStatisticsSubjectsMapperTest {
	@Test
	fun when_noSubjectIsClosed_then_theBlockSaysWhatItLacks() {
		val item = subjectsItemOf(outcomes(approved = 0, failed = 0, withdrawn = 0, noResult = 0))

		assertTrue(item.content is SummaryStatisticsSubjectsItem.Content.Empty)
	}

	@Test
	fun when_someSubjectsHaveNoResult_then_theFigureCountsThemAndTheBarHoldsTheRest() {
		val data = dataOf(outcomes(approved = 2, failed = 1, withdrawn = 0, noResult = 2))

		assertEquals("5", data.figure.value)
		assertEquals(listOf(2f, 1f, 0f), data.segments.map { segment -> segment.value })
	}

	@Test
	fun when_theFirstTryHasNoResultBase_then_ItIsLeftOutOfTheFacts() {
		val data = dataOf(outcomes(approved = 0, failed = 0, withdrawn = 2, noResult = 0))

		assertEquals(listOf("0"), data.facts.map { fact -> fact.value })
	}

	@Test
	fun when_theFirstTryIsKnown_then_ItsRatioLeadsTheFacts() {
		val data = dataOf(
			SubjectOutcomes(
				approved = 2,
				failed = 1,
				withdrawn = 0,
				noResult = 2,
				approvedFirstTry = 2,
				firstTryBase = 3,
				repeated = 1
			)
		)

		assertEquals(listOf("2/3", "1", "2"), data.facts.map { fact -> fact.value })
	}

	@Test
	fun when_theChartIsBuilt_then_itsSummaryIsTheFigureAndWhatItCounts() {
		val data = dataOf(outcomes(approved = 2, failed = 1, withdrawn = 0, noResult = 2))

		assertEquals(
			UiText.Resource(Res.string.summary_statistics_chart_summary, listOf("5", data.figure.label)),
			data.chartSummary
		)
	}

	private fun dataOf(outcomes: SubjectOutcomes): SummaryStatisticsSubjectsItem.Content.Data {
		return subjectsItemOf(outcomes).content as SummaryStatisticsSubjectsItem.Content.Data
	}

	private fun outcomes(approved: Int, failed: Int, withdrawn: Int, noResult: Int) = SubjectOutcomes(
		approved = approved,
		failed = failed,
		withdrawn = withdrawn,
		noResult = noResult,
		approvedFirstTry = 0,
		firstTryBase = 0,
		repeated = 0
	)
}
