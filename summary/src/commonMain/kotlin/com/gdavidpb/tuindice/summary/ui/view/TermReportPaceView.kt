package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportPaceItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsBarChart
import com.gdavidpb.tuindice.summary.ui.chart.StatisticsChartLabels
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsBarPoints
import com.gdavidpb.tuindice.summary.ui.chart.toStatisticsReference
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_pace_title
import tuindice.summary.generated.resources.term_report_pace_unit

/**
 * "Tu ritmo": the credits each regular term approved as a bar, the term of the report in the accent, the average of the
 * regular terms as a dashed line, and under the chart what the term approved beside that average.
 */
@Composable
fun TermReportPaceView(
	pace: TermReportPaceItem,
	chartActionLabel: String,
	modifier: Modifier = Modifier
) {
	val title = stringResource(Res.string.term_report_pace_title)

	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportPace),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)

			Spacer(modifier = Modifier.weight(1f))

			Text(
				text = stringResource(Res.string.term_report_pace_unit),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		SummaryHubCardView {
			Column(
				modifier = Modifier.padding(TuIndiceSpacing.Screen),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
			) {
				when (pace) {
					is TermReportPaceItem.Empty -> Text(
						modifier = Modifier.testTag(TermReportUiTags.TermReportPaceEmpty),
						text = pace.text.asString(),
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)

					is TermReportPaceItem.Data -> TermReportPaceData(
						pace = pace,
						title = title,
						chartActionLabel = chartActionLabel
					)
				}
			}
		}
	}
}

// The bars under their average, and under the chart what the term approved beside it.
@Composable
private fun TermReportPaceData(
	pace: TermReportPaceItem.Data,
	title: String,
	chartActionLabel: String
) {
	val bars = pace.bars.toStatisticsBarPoints()

	key(bars) {
		StatisticsBarChart(
			bars = bars,
			maxValue = null,
			reference = pace.reference.toStatisticsReference(),
			labels = StatisticsChartLabels(
				key = SummaryStatisticsChartKey.TermReportPace,
				summary = title,
				actionLabel = chartActionLabel
			)
		)
	}

	TermReportPaceLegend(
		current = pace.currentLegend.asString(),
		average = pace.averageLegend.asString()
	)
}

// What the chart marks: a dot in the accent for the term of the report, and the dashed line for the average.
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TermReportPaceLegend(
	current: String,
	average: String,
	modifier: Modifier = Modifier
) {
	val accent = MaterialTheme.colorScheme.primary
	val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

	FlowRow(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportPaceLegend),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Large),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
		) {
			Box(
				modifier = Modifier
					.size(DOT_SIZE)
					.background(color = accent, shape = CircleShape)
			)

			Text(
				text = current,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
		) {
			Canvas(
				modifier = Modifier
					.width(LINE_WIDTH)
					.height(LINE_THICKNESS)
			) {
				drawLine(
					color = lineColor,
					start = Offset(x = 0f, y = size.height / 2),
					end = Offset(x = size.width, y = size.height / 2),
					strokeWidth = size.height,
					pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(DASH_ON, DASH_OFF))
				)
			}

			Text(
				text = average,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

private val DOT_SIZE = 8.dp
private val LINE_WIDTH = 16.dp
private val LINE_THICKNESS = 2.dp
private const val DASH_ON = 6f
private const val DASH_OFF = 4f
