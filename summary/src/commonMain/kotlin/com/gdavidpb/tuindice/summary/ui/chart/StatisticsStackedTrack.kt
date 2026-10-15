package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

// The coloured segments, drawn by the shared progress bar at its height and with its rounded ends. Each one is dimmed
// while another is open, and each keeps the tag of its index.
@Composable
fun BoxScope.StatisticsStackedTrack(
	segments: List<StatisticsSegment>,
	progress: Float,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	val hasSelection = openIndex.value != null
	val drawn = StatisticsChartGeometry.drawnSegments(segments.map { segment -> segment.value })

	TuIndiceProgressBar(
		segments = drawn.map { (index, fraction) ->
			TuIndiceProgressSegment(
				fraction = fraction,
				color = segments[index].color.copy(
					alpha = StatisticsChartDefaults.alphaOf(openIndex.value == index, hasSelection)
				),
				tag = SummaryUiTags.statisticsFill(labels.key, index)
			)
		},
		modifier = Modifier.align(Alignment.Center),
		progress = progress
	)
}
