package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * A row of bars, one per point, each growing from the baseline to its share of the scale. The scale tops out at
 * [maxValue], or at the largest of the values and the reference when it is absent. A [reference] is a dashed line
 * across the chart, labelled above its end. Tapping a bar opens its tooltip and closes the one that was open. Past six
 * bars the chart scrolls, and it opens at its latest bar.
 */
@Composable
fun StatisticsBarChart(
	bars: List<StatisticsBarPoint>,
	maxValue: Float?,
	reference: StatisticsReference?,
	labels: StatisticsChartLabels,
	modifier: Modifier = Modifier
) {
	val openIndex = remember { mutableStateOf<Int?>(null) }
	val scale = maxValue ?: StatisticsChartGeometry.scaleOf(bars.map { bar -> bar.value }, reference?.value)

	BoxWithConstraints(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.statisticsChart(labels.key))
			.semantics { contentDescription = labels.summary }
	) {
		val columnWidth = StatisticsChartGeometry.columnWidth(maxWidth.value, bars.size)
		val contentWidth = StatisticsChartGeometry.contentWidth(columnWidth, bars.size)

		StatisticsScrollFrame {
			Box(modifier = Modifier.width(contentWidth.dp).height(StatisticsChartDefaults.ChartHeight)) {
				BarColumns(
					bars = bars,
					scale = scale,
					columnWidth = columnWidth,
					labels = labels,
					openIndex = openIndex
				)

				reference?.let { line ->
					StatisticsReferenceLine(
						label = line.label,
						y = StatisticsChartGeometry.yOf(
							fraction = StatisticsChartGeometry.barFraction(line.value, scale),
							topDp = StatisticsChartDefaults.PlotTop.value,
							heightDp = StatisticsChartDefaults.PlotHeight.value
						),
						widthDp = contentWidth
					)
				}
			}
		}
	}
}

@Composable
private fun BoxScope.BarColumns(
	bars: List<StatisticsBarPoint>,
	scale: Float,
	columnWidth: Float,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	val dataColor = StatisticsChartDefaults.dataColor()
	val hasSelection = openIndex.value != null
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val barWidth = StatisticsChartGeometry.barWidth(columnWidth)

	bars.forEachIndexed { index, bar ->
		key(index) {
			val growth = remember { Animatable(0f) }
			val fraction = StatisticsChartGeometry.barFraction(bar.value, scale)
			val isOpen = openIndex.value == index

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
				StatisticsTooltipTarget(
					target = labels.targetOf(index, bar.label, bar.tooltip),
					isOpen = isOpen,
					onOpenChange = { open -> openIndex.value = if (open) index else null }
				) {
					StatisticsBar(
						shownFraction = if (animationsEnabled) growth.value else fraction,
						width = barWidth,
						color = dataColor.copy(alpha = StatisticsChartDefaults.alphaOf(isOpen, hasSelection)),
						tag = SummaryUiTags.statisticsFill(labels.key, index)
					)
				}
			}
		}
	}
}

@Composable
private fun BoxScope.StatisticsBar(
	shownFraction: Float,
	width: Float,
	color: Color,
	tag: String
) {
	Box(
		modifier = Modifier
			.align(Alignment.BottomCenter)
			.width(width.dp)
			.height((StatisticsChartDefaults.PlotHeight.value * shownFraction).dp)
			.background(
				color = color,
				shape = RoundedCornerShape(
					topStart = StatisticsChartDefaults.BarCornerRadius,
					topEnd = StatisticsChartDefaults.BarCornerRadius
				)
			)
			.testTag(tag)
	)
}

@Composable
private fun BoxScope.StatisticsReferenceLine(
	label: String,
	y: Float,
	widthDp: Float
) {
	val color = MaterialTheme.colorScheme.onSurfaceVariant

	Canvas(modifier = Modifier.matchParentSize()) {
		val lineY = y.dp.toPx()

		drawLine(
			color = color,
			start = Offset(x = 0f, y = lineY),
			end = Offset(x = widthDp.dp.toPx(), y = lineY),
			strokeWidth = 1.dp.toPx(),
			pathEffect = PathEffect.dashPathEffect(
				intervals = floatArrayOf(
					StatisticsChartDefaults.ReferenceDashLength.toPx(),
					StatisticsChartDefaults.ReferenceGapLength.toPx()
				)
			)
		)
	}

	Text(
		modifier = Modifier
			.align(Alignment.TopEnd)
			.offset(y = (y - StatisticsChartDefaults.ReferenceLabelHeight.value).dp)
			.padding(end = StatisticsChartDefaults.EdgeFadeWidth)
			.testTag(SummaryUiTags.StatisticsReferenceLabel),
		text = label,
		style = MaterialTheme.typography.labelSmall,
		color = color
	)
}
