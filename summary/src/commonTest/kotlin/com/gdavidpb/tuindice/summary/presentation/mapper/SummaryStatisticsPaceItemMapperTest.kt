package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.PaceStatistics
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.domain.model.SummaryCreditsProgress
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFactItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.testing.statisticsHubOf
import com.gdavidpb.tuindice.summary.testing.statisticsOf
import com.gdavidpb.tuindice.summary.testing.statisticsTerm
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_statistics_pace_complete
import tuindice.summary.generated.resources.summary_statistics_pace_empty
import tuindice.summary.generated.resources.summary_statistics_pace_figure
import tuindice.summary.generated.resources.summary_statistics_pace_info
import tuindice.summary.generated.resources.summary_statistics_pace_invite_message
import tuindice.summary.generated.resources.summary_statistics_pace_invite_title
import tuindice.summary.generated.resources.summary_statistics_pace_projection_pending
import tuindice.summary.generated.resources.summary_statistics_pace_remaining_many
import tuindice.summary.generated.resources.summary_statistics_pace_remaining_one
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// "Ritmo": the bars of the closed terms, the reference line, and the projection under the chart.
class SummaryStatisticsPaceItemMapperTest {
	@Test
	fun when_aJulyAugustTermCloses_then_itIsDrawnButLeavesTheAverageOut() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JUL_AUG, approvedCredits = 6)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace

		val data = assertIs<SummaryStatisticsPaceItem.Content.Data>(pace.content)
		assertEquals(2, data.bars.size)
		assertEquals(6f, data.bars[1].value)
		assertEquals("15.0", data.figure.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_pace_figure), data.figure.label)
	}

	@Test
	fun when_noRegularTermCloses_then_thePaceSaysItWillAppear() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JUL_AUG, approvedCredits = 6))
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace

		assertEquals(
			SummaryStatisticsPaceItem.Content.Empty(UiText.Resource(Res.string.summary_statistics_pace_empty)),
			pace.content
		)
	}

	@Test
	fun when_noPensumIsCached_then_thePaceInvitesToOpenIt() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2024))
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.pace
			.content as SummaryStatisticsPaceItem.Content.Data

		assertEquals(
			SummaryStatisticsPaceItem.Projection.NoPensum(
				title = UiText.Resource(Res.string.summary_statistics_pace_invite_title),
				message = UiText.Resource(Res.string.summary_statistics_pace_invite_message)
			),
			data.projection
		)
	}

	@Test
	fun when_oneRegularTermCloses_then_itsBarShowsAndTheProjectionWaits() {
		val statistics = statisticsOf(
			terms = listOf(statisticsTerm(year = 2024, approvedCredits = 15))
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null),
			pensumProgress = SummaryCreditsProgress(approvedCredits = 15, totalCredits = 205)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.pace
			.content as SummaryStatisticsPaceItem.Content.Data

		assertEquals(1, data.bars.size)
		assertEquals(15f, data.reference.value)
		assertEquals(
			SummaryStatisticsPaceItem.Projection.Pending(
				UiText.Resource(Res.string.summary_statistics_pace_projection_pending)
			),
			data.projection
		)
	}

	@Test
	fun when_theRemainingTermsAreKnown_then_theProjectionCountsThemAsAFactAndTheInfoExplainsIt() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = 4),
			pensumProgress = SummaryCreditsProgress(approvedCredits = 30, totalCredits = 205)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace
		val data = pace.content as SummaryStatisticsPaceItem.Content.Data
		val projection = data.projection as SummaryStatisticsPaceItem.Projection.Remaining

		assertEquals(
			SummaryStatisticsFactItem(
				value = "4",
				label = UiText.Resource(Res.string.summary_statistics_pace_remaining_many)
			),
			projection.fact
		)
		assertEquals(UiText.Resource(Res.string.summary_statistics_pace_info), pace.info)
	}

	@Test
	fun when_oneTermIsLeftToProject_then_theFactCountsOneTerm() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = 1),
			pensumProgress = SummaryCreditsProgress(approvedCredits = 190, totalCredits = 205)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace
		val data = pace.content as SummaryStatisticsPaceItem.Content.Data
		val projection = data.projection as SummaryStatisticsPaceItem.Projection.Remaining

		assertEquals(
			SummaryStatisticsFactItem(
				value = "1",
				label = UiText.Resource(Res.string.summary_statistics_pace_remaining_one)
			),
			projection.fact
		)
	}

	@Test
	fun when_thePensumIsComplete_then_thePaceSaysSo() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null),
			pensumProgress = SummaryCreditsProgress(approvedCredits = 205, totalCredits = 205)
		)

		val data = statisticsHubOf(statistics = statistics)
			.toSummaryStatisticsItem()!!
			.pace
			.content as SummaryStatisticsPaceItem.Content.Data

		assertEquals(
			SummaryStatisticsPaceItem.Projection.Complete(UiText.Resource(Res.string.summary_statistics_pace_complete)),
			data.projection
		)
	}

	@Test
	fun when_theAxisNamesCollide_then_theyKeepTheirThreeLetterMonths() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAR),
				statisticsTerm(year = 2024, periodCode = AcademicTermPeriod.JAN_MAY),
				statisticsTerm(year = 2023, periodCode = AcademicTermPeriod.SEP_DEC),
				statisticsTerm(year = 2023, periodCode = AcademicTermPeriod.JUL_AUG)
			)
		).copy(
			pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null)
		)

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace
		val bars = (pace.content as SummaryStatisticsPaceItem.Content.Data).bars

		assertEquals(
			listOf(UiText.Raw("Ene-Mar 24"), UiText.Raw("Ene-May 24"), UiText.Raw("S-D 23"), UiText.Raw("J-A 23")),
			bars.map { bar -> bar.axisLabel }
		)
	}

	@Test
	fun when_thePaceDrawsItsBars_then_noFigureIsWrittenOverThem() {
		val statistics = statisticsOf(
			terms = listOf(
				statisticsTerm(year = 2024, approvedCredits = 15),
				statisticsTerm(year = 2025, approvedCredits = 15)
			)
		).copy(pace = PaceStatistics(averageCredits = 15.0, remainingTerms = null))

		val pace = statisticsHubOf(statistics = statistics).toSummaryStatisticsItem()!!.pace

		val data = pace.content as SummaryStatisticsPaceItem.Content.Data
		assertTrue(data.bars.all { bar -> bar.valueLabel == null })
	}
}
