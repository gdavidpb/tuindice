package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsIndexItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Índice": the cumulative average of the closed terms as a line, with the index of each term as its marker and each
 * term named under its column. Its figure is the cumulative average at the last closed term, written as the header
 * writes a grade, and the caption names that term. The best and the worst regular terms are written under the chart,
 * each beside a ring in the colour of its point.
 */
@Composable
fun SummaryStatisticsIndexView(
	item: SummaryStatisticsIndexItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsIndexBlock,
			title = item.title.asString()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsIndexItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsIndexItem.Content.Data -> SummaryStatisticsIndexData(
				data = content,
				chartActionLabel = chartActionLabel
			)
		}
	}
}
