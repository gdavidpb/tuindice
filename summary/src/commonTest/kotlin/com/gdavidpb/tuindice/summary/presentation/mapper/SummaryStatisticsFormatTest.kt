package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_percent_format
import tuindice.summary.generated.resources.summary_statistics_chart_summary
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_many
import tuindice.summary.generated.resources.summary_statistics_subjects_approved_one
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryStatisticsFormatTest {
	@Test
	fun when_aPartOfAWholeIsGiven_then_ItsPercentIsRoundedAndZeroWithoutAWhole() {
		assertEquals(25, percentOf(part = 1, whole = 4))
		assertEquals(67, percentOf(part = 2, whole = 3))
		assertEquals(0, percentOf(part = 0, whole = 0))
	}

	@Test
	fun when_aShareIsGiven_then_ItsNumberAndItsSignAreTheArgumentsOfThePercentResource() {
		assertEquals(UiText.Resource(Res.string.summary_percent_format, listOf(25, "%")), shareOf(part = 1, whole = 4))
		assertEquals(UiText.Resource(Res.string.summary_percent_format, listOf(50, "%")), shareOf(fraction = 0.5))
	}

	@Test
	fun when_aFigureLeadsAChart_then_ItsValueAndWhatItCountsAreOnePhrase() {
		val figure = SummaryStatisticsFigureItem(value = "4.1250", label = UiText.Raw("acumulado"))

		assertEquals(
			UiText.Resource(Res.string.summary_statistics_chart_summary, listOf("4.1250", UiText.Raw("acumulado"))),
			chartSummaryOf(figure)
		)
	}

	@Test
	fun when_aCountIsGiven_then_ItsSingularOrItsPluralIsChosenByTheNumber() {
		val one = Res.string.summary_statistics_subjects_approved_one
		val many = Res.string.summary_statistics_subjects_approved_many

		assertEquals(UiText.Resource(one, listOf(1)), countText(quantity = 1, one = one, many = many, args = listOf(1)))
		assertEquals(UiText.Resource(many, listOf(2)), countText(quantity = 2, one = one, many = many, args = listOf(2)))
	}

	@Test
	fun when_pointsAreWhole_then_TheyHaveNoDecimalPlace_andAnyOtherHasOne() {
		assertEquals("8", 8.0.formatPoints())
		assertEquals(7.5.formatGrade(decimals = 1), 7.5.formatPoints())
	}
}
