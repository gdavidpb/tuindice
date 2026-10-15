package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Créditos": the credits of the closed terms by outcome, as one stacked bar with its legend. When a pensum is cached,
 * its own progress follows the bar: the approved credits out of the pensum's total, with their share and a track in the
 * approved colour.
 */
@Composable
fun SummaryStatisticsCreditsView(
	item: SummaryStatisticsCreditsItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsCreditsBlock,
			title = item.title.asString()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsCreditsItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsCreditsItem.Content.Data -> SummaryStatisticsCreditsData(
				data = content,
				chartActionLabel = chartActionLabel
			)
		}
	}
}
