package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.ReportComposition
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The columns of a bar chart: one bar for each point, each growing from the baseline to its share of the [scale] and
 * placed in its own column. A bar with a value label carries that figure above it. Tapping a bar opens its tooltip and
 * closes the one that was open, and the [openIndex] holds the bar whose tooltip is open, so the others dim.
 */
@Composable
fun BoxScope.StatisticsBarColumns(
	bars: List<StatisticsBarPoint>,
	scale: Float,
	columnWidth: Float,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	ReportComposition("statistics-bars")

	val dataColor = StatisticsChartDefaults.dataColor()
	val highlightColor = MaterialTheme.colorScheme.primary
	val hasSelection = openIndex.value != null
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val barWidth = StatisticsChartGeometry.barWidth(columnWidth)

	bars.forEachIndexed { index, bar ->
		key(index) {
			val growth = remember { Animatable(0f) }
			val fraction = StatisticsChartGeometry.barFraction(bar.value, scale)
			val isOpen = openIndex.value == index
			// The growth is read by the bar and its figure where they are laid out, so a frame of it composes no column.
			val shownFraction: () -> Float = { if (animationsEnabled) growth.value else fraction }
			val target = remember(index, bar, labels) { labels.targetOf(index, bar.label, bar.tooltip) }

			LaunchedEffect(fraction, animationsEnabled) {
				if (animationsEnabled) {
					growth.animateTo(fraction, tween(durationMillis = StatisticsChartDefaults.GrowMillis))
				}
			}

			Box(
				modifier = Modifier
					.offset(x = (index * columnWidth).dp)
					.size(width = columnWidth.dp, height = StatisticsChartDefaults.ChartHeight)
			) {
				bar.valueLabel?.let { valueLabel ->
					StatisticsValueLabel(
						text = valueLabel,
						tag = StatisticsLabelUiTags.value(labels.key, index),
						shownFraction = shownFraction,
						columnWidth = columnWidth
					)
				}

				StatisticsTooltipTarget(
					target = target,
					isOpen = isOpen,
					onOpenChange = { open -> openIndex.value = if (open) index else null }
				) {
					StatisticsBar(
						shownFraction = shownFraction,
						width = barWidth,
						color = (if (bar.isHighlighted) highlightColor else dataColor)
							.copy(alpha = StatisticsChartDefaults.alphaOf(isOpen, hasSelection)),
						tag = SummaryUiTags.statisticsFill(labels.key, index)
					)
				}
			}
		}
	}
}
