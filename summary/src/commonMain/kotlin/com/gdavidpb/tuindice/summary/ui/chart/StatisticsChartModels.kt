package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.summary.presentation.mapper.toRichText
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsBarItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsLinePointItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsReferenceItem
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsSegmentItem
import com.gdavidpb.tuindice.summary.ui.view.color

// The conversions from the presentation items of the statistics to the models the charts draw. Each kind of item has
// one conversion, which every view that draws it uses. They resolve the words and colours of the screen, so they run in
// composition.

// The segments of a stacked bar: each in the colour of its outcome, with its legend and its tooltip.
@Composable
internal fun List<SummaryStatisticsSegmentItem>.toStatisticsSegments(): List<StatisticsSegment> {
	return map { segment ->
		StatisticsSegment(
			label = segment.legend.asString(),
			value = segment.value,
			color = segment.outcome.color(),
			tooltip = segment.tooltip.toRichText()
		)
	}
}

// The bars of a bar chart, each with its axis label and the figure above it when it carries one.
@Composable
internal fun List<SummaryStatisticsBarItem>.toStatisticsBarPoints(): List<StatisticsBarPoint> {
	return map { bar ->
		StatisticsBarPoint(
			label = bar.tooltip.title.asString(),
			value = bar.value,
			tooltip = bar.tooltip.toRichText(),
			axisLabel = bar.axisLabel.asString(),
			valueLabel = bar.valueLabel?.asString()
		)
	}
}

// The points of a line chart. The best regular term is ringed in the approval colour and the worst in the error colour.
@Composable
internal fun List<SummaryStatisticsLinePointItem>.toStatisticsLinePoints(): List<StatisticsLinePoint> {
	val bestColor = AcademicStatusColors.approved()
	val worstColor = MaterialTheme.colorScheme.error

	return map { point ->
		StatisticsLinePoint(
			label = point.tooltip.title.asString(),
			lineValue = point.lineValue,
			tooltip = point.tooltip.toRichText(),
			axisLabel = point.axisLabel.asString(),
			markerValue = point.markerValue,
			ringColor = when {
				point.isBest -> bestColor
				point.isWorst -> worstColor
				else -> null
			}
		)
	}
}

// The reference line of a bar chart, with its label written at its end.
@Composable
internal fun SummaryStatisticsReferenceItem.toStatisticsReference(): StatisticsReference {
	return StatisticsReference(
		value = value,
		label = label.asString()
	)
}
