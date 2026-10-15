package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSubjectsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Materias": the subjects of the closed terms by outcome, as one stacked bar with its legend. The figure counts every
 * subject; the bar holds the approved, the failed and the withdrawn, and the subjects without a result are a compact
 * figure among the facts, which also say how many were approved on the first attempt and how many were repeated.
 */
@Composable
fun SummaryStatisticsSubjectsView(
	item: SummaryStatisticsSubjectsItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsSubjectsBlock,
			title = item.title.asString()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsSubjectsItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsSubjectsItem.Content.Data -> SummaryStatisticsSubjectsData(
				data = content,
				chartActionLabel = chartActionLabel
			)
		}
	}
}
