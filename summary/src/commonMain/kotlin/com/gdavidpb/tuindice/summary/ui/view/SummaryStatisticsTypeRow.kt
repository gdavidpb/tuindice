package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.summary.presentation.mapper.toRichText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsTarget
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsTooltipTarget

// One evaluation type: its bar and its score. A type with a grade opens a tooltip that names its points and its share.
@Composable
fun SummaryStatisticsTypeRow(
	index: Int,
	row: SummaryStatisticsEvaluationTypeItem,
	openRow: MutableState<Int?>,
	chartActionLabel: String
) {
	val tooltip = row.tooltip

	if (tooltip == null) {
		SummaryStatisticsTypeRowContent(row = row)
	} else {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = StatisticsChartDefaults.TargetMinSize)
		) {
			StatisticsTooltipTarget(
				target = StatisticsTarget(
					tag = SummaryUiTags.statisticsTarget(SummaryStatisticsChartKey.CurrentTerm, index),
					description = tooltip.title.asString(),
					actionLabel = chartActionLabel,
					tooltip = tooltip.toRichText()
				),
				isOpen = openRow.value == index,
				onOpenChange = { open -> openRow.value = if (open) index else null }
			) {
				SummaryStatisticsTypeRowContent(row = row)
			}
		}
	}
}
