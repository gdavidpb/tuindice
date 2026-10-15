package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsEvaluationTypeItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartDefaults

// The name of the type, its track, and the words that say how many of its evaluations are graded and what they score.
@Composable
fun SummaryStatisticsTypeRowContent(row: SummaryStatisticsEvaluationTypeItem) {
	val onSurface = MaterialTheme.colorScheme.onSurface

	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Text(
			text = row.name.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = onSurface
		)

		TuIndiceProgressBar(
			fraction = row.fraction,
			color = StatisticsChartDefaults.dataColor(),
			fillTag = SummaryUiTags.StatisticsTrackFill
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				text = row.gradedText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)

			Text(
				text = row.scoreText.asString(),
				style = MaterialTheme.typography.bodySmall,
				color = onSurface
			)
		}
	}
}
