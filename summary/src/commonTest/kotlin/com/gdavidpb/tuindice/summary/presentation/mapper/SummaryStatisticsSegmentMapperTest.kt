package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_percent_format
import tuindice.summary.generated.resources.summary_statistics_credits_approved_many
import tuindice.summary.generated.resources.summary_statistics_credits_approved_one
import tuindice.summary.generated.resources.summary_statistics_credits_share_approved_many
import tuindice.summary.generated.resources.summary_statistics_credits_share_approved_one
import kotlin.test.Test
import kotlin.test.assertEquals

class SummaryStatisticsSegmentMapperTest {
	@Test
	fun when_aSingleCountIsGiven_then_ItsLegendAndTooltipUseTheSingular() {
		val segment = countSegmentOf(
			outcome = SummaryStatisticsOutcome.APPROVED,
			count = 1,
			whole = 4,
			legend = Res.string.summary_statistics_credits_approved_one to Res.string.summary_statistics_credits_approved_many,
			share = Res.string.summary_statistics_credits_share_approved_one to
				Res.string.summary_statistics_credits_share_approved_many
		)

		assertEquals(1f, segment.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_credits_approved_one, listOf(1)), segment.legend)
		assertEquals(
			SummaryStatisticsTooltipItem(
				title = UiText.Resource(
					Res.string.summary_statistics_credits_share_approved_one,
					listOf(1, UiText.Resource(Res.string.summary_percent_format, listOf(25)))
				)
			),
			segment.tooltip
		)
	}

	@Test
	fun when_severalCountsAreGiven_then_ItsLegendAndTooltipUseThePlural() {
		val segment = countSegmentOf(
			outcome = SummaryStatisticsOutcome.APPROVED,
			count = 3,
			whole = 4,
			legend = Res.string.summary_statistics_credits_approved_one to Res.string.summary_statistics_credits_approved_many,
			share = Res.string.summary_statistics_credits_share_approved_one to
				Res.string.summary_statistics_credits_share_approved_many
		)

		assertEquals(3f, segment.value)
		assertEquals(UiText.Resource(Res.string.summary_statistics_credits_approved_many, listOf(3)), segment.legend)
		assertEquals(
			UiText.Resource(
				Res.string.summary_statistics_credits_share_approved_many,
				listOf(3, UiText.Resource(Res.string.summary_percent_format, listOf(75)))
			),
			segment.tooltip.title
		)
	}
}
