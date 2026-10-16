package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.mapper.toBlockInfo
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Ritmo": the approved credits of each closed term as a bar, with the average of the regular terms as a reference line
 * written at its end. Under the chart, the projection counts as a compact figure how many regular terms the pensum
 * still takes at that pace, with the projection's tag beside it, or says why it cannot say yet. The mark beside the
 * title explains the line and the projection.
 */
@Composable
fun SummaryStatisticsPaceView(
	item: SummaryStatisticsPaceItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	val title = item.title.asString()

	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsPaceBlock,
			title = title,
			info = item.info.toBlockInfo()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsPaceItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsPaceItem.Content.Data -> SummaryStatisticsPaceData(
				data = content,
				summary = title,
				chartActionLabel = chartActionLabel
			)
		}
	}
}
