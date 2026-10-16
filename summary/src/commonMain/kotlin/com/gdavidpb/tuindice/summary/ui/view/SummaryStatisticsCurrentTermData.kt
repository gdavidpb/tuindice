package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

// The figures of the term in progress: its points out of the total with their track, the rows of its evaluation types,
// and the line of the evaluations that went by without a grade. One row's tooltip is open at a time.
@Composable
fun ColumnScope.SummaryStatisticsCurrentTermData(
	data: SummaryStatisticsCurrentTermItem.Content.Data,
	chartActionLabel: String,
	actions: SummaryHubActions
) {
	val openRow = remember { mutableStateOf<Int?>(null) }

	data.figure?.let { figure -> SummaryStatisticsFigureView(figure = figure) }

	data.fraction?.let { fraction ->
		// The data blue of the statistics, as every chart of the screen draws its figures.
		TuIndiceProgressBar(
			fraction = fraction,
			color = StatisticsChartDefaults.dataColor(),
			fillTag = SummaryUiTags.StatisticsTrackFill
		)
	}

	data.rows.forEachIndexed { index, row ->
		SummaryStatisticsTypeRow(
			index = index,
			row = row,
			openRow = openRow,
			chartActionLabel = chartActionLabel
		)
	}

	data.overdue?.let { overdue ->
		SummaryStatisticsOverdueRow(overdue = overdue, actions = actions)
	}
}
