package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSegmentItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsTooltipItem
import org.jetbrains.compose.resources.StringResource

// A segment of a stacked bar whose legend and tooltip name a count of [outcome] with the noun of its quantity.
internal fun countSegmentOf(
	outcome: SummaryStatisticsOutcome,
	count: Int,
	whole: Int,
	legend: Pair<StringResource, StringResource>,
	share: Pair<StringResource, StringResource>
): SummaryStatisticsSegmentItem {
	return SummaryStatisticsSegmentItem(
		outcome = outcome,
		value = count.toFloat(),
		legend = countText(quantity = count, one = legend.first, many = legend.second, args = listOf(count)),
		tooltip = SummaryStatisticsTooltipItem(
			title = countText(
				quantity = count,
				one = share.first,
				many = share.second,
				args = listOf(count, shareOf(part = count, whole = whole))
			)
		)
	)
}

// The segment of a stacked bar that names its count, for the outcomes that have no noun to agree with.
internal fun plainSegmentOf(
	outcome: SummaryStatisticsOutcome,
	count: Int,
	whole: Int,
	legend: StringResource,
	share: StringResource
): SummaryStatisticsSegmentItem {
	return SummaryStatisticsSegmentItem(
		outcome = outcome,
		value = count.toFloat(),
		legend = UiText.Resource(legend, listOf(count)),
		tooltip = SummaryStatisticsTooltipItem(
			title = UiText.Resource(share, listOf(count, shareOf(part = count, whole = whole)))
		)
	)
}
