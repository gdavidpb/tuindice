package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCreditsItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Créditos": the credits the pensum has approved out of its total, as one figure with its share and a bar that follows
 * it. With no pensum cached, or no closed term yet, the block says what it lacks.
 */
@Composable
fun SummaryStatisticsCreditsView(
	item: SummaryStatisticsCreditsItem,
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
				data = content
			)
		}
	}
}
