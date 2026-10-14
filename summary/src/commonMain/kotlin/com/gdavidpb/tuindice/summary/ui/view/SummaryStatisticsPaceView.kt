package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsPaceItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarPoint
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsReference
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsBlockHead
import com.gdavidpb.tuindice.summary.ui.model.SummaryStatisticsEmptyHeading

/**
 * "Ritmo": the approved credits of each closed term as a bar, with the average of the regular terms as a reference line
 * written at its end. Under the chart, the projection says how many regular terms the pensum still takes at that pace,
 * with the count set apart in a medium weight and the darker ink, and the projection's tag beside it, or why it
 * cannot say yet.
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
			title = title
		),
		modifier = modifier
	) {
		when (val content = item.content) {
			is SummaryStatisticsPaceItem.Content.Empty -> SummaryStatisticsEmptyView(
				message = content.text.asString()
			)

			is SummaryStatisticsPaceItem.Content.Data -> PaceData(
				data = content,
				summary = title,
				chartActionLabel = chartActionLabel
			)
		}
	}
}

@Composable
private fun ColumnScope.PaceData(
	data: SummaryStatisticsPaceItem.Content.Data,
	summary: String,
	chartActionLabel: String
) {
	val bars = data.bars.map { bar ->
		StatisticsBarPoint(
			label = bar.tooltip.title.asString(),
			value = bar.value,
			tooltip = bar.tooltip.toRichText(),
			axisLabel = bar.axisLabel.asString()
		)
	}
	val reference = StatisticsReference(
		value = data.reference.value,
		label = data.reference.label.asString()
	)

	SummaryStatisticsFigureView(figure = data.figure)

	key(bars) {
		StatisticsBarChart(
			bars = bars,
			maxValue = null,
			reference = reference,
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.Pace,
				summary = summary,
				actionLabel = chartActionLabel
			)
		)
	}

	PaceProjection(projection = data.projection)
}

@Composable
private fun ColumnScope.PaceProjection(projection: SummaryStatisticsPaceItem.Projection) {
	when (projection) {
		is SummaryStatisticsPaceItem.Projection.Remaining -> RemainingProjection(projection = projection)

		is SummaryStatisticsPaceItem.Projection.Pending -> Text(
			modifier = Modifier.testTag(SummaryUiTags.StatisticsProjection),
			text = projection.text.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		is SummaryStatisticsPaceItem.Projection.Complete -> Text(
			modifier = Modifier.testTag(SummaryUiTags.StatisticsProjection),
			text = projection.text.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		is SummaryStatisticsPaceItem.Projection.NoPensum -> SummaryStatisticsEmptyView(
			heading = SummaryStatisticsEmptyHeading(
				title = projection.title.asString(),
				icon = Icons.Outlined.AccountTree
			),
			message = projection.message.asString()
		)

		SummaryStatisticsPaceItem.Projection.None -> Unit
	}
}

// The projection's sentence, with its count in a medium weight and the darker ink, and its tag in an outline beside it.
@Composable
private fun RemainingProjection(projection: SummaryStatisticsPaceItem.Projection.Remaining) {
	val emphasisStyle = SpanStyle(
		color = MaterialTheme.colorScheme.onSurface,
		fontWeight = FontWeight.Medium
	)

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			modifier = Modifier
				.weight(1f)
				.testTag(SummaryUiTags.StatisticsProjection),
			text = emphasizedText(
				text = projection.text.asString(),
				emphasis = projection.emphasis.asString(),
				emphasisStyle = emphasisStyle
			),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Text(
			modifier = Modifier
				.border(
					width = 1.dp,
					color = MaterialTheme.colorScheme.outline,
					shape = RoundedCornerShape(TuIndiceRadius.Small)
				)
				.padding(horizontal = TuIndiceSpacing.Medium, vertical = TuIndiceSpacing.Two)
				.testTag(SummaryUiTags.StatisticsProjectionTag),
			text = projection.tag.asString(),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
