package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.mapper.toBlockInfo
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsGradesItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead

/**
 * "Tus notas": one bar for each grade from 1 to 5, with the attempts that carry it written above the bar and the grade
 * under it. The mark beside the title explains what the bars count, and the figure under them says what the record did
 * not report, when anything.
 */
@Composable
fun SummaryStatisticsGradesView(
	item: SummaryStatisticsGradesItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	val title = item.title.asString()

	SummaryStatisticsBlockView(
		head = SummaryStatisticsBlockHead(
			tag = SummaryUiTags.StatisticsGradesBlock,
			title = title,
			info = item.info.toBlockInfo()
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsGradesItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsGradesItem.Content.Data -> SummaryStatisticsGradesData(
				data = content,
				summary = title,
				chartActionLabel = chartActionLabel
			)
		}
	}
}
