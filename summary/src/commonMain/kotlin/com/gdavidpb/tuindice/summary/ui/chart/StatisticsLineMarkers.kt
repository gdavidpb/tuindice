package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

// The button of each point of a line chart: its marker, a dot on the marker value, with the ring of the point when it has
// one, and the touch area that opens its tooltip. Tapping a point closes the tooltip of the one that was open, so one
// tooltip is open at a time.
@Composable
fun BoxScope.StatisticsLineMarkers(
	points: List<StatisticsLinePoint>,
	range: ClosedFloatingPointRange<Float>,
	columnWidth: Float,
	labels: StatisticsChartLabels,
	openIndex: MutableState<Int?>
) {
	val dataColor = StatisticsChartDefaults.dataColor()
	val hasSelection = openIndex.value != null
	val half = StatisticsChartDefaults.TargetMinSize.value / 2

	points.forEachIndexed { index, point ->
		key(index) {
			val isOpen = openIndex.value == index
			val centerX = StatisticsChartGeometry.columnCenter(index, columnWidth)
			val centerY = StatisticsChartGeometry.yOf(
				fraction = StatisticsChartGeometry.valueFraction(point.markerValue, range.start, range.endInclusive),
				topDp = StatisticsChartDefaults.LineInset.value,
				heightDp = StatisticsChartDefaults.LineBandHeight.value
			)

			Box(
				modifier = Modifier
					.offset(x = (centerX - half).dp, y = (centerY - half).dp)
					.size(StatisticsChartDefaults.TargetMinSize)
			) {
				StatisticsTooltipTarget(
					target = labels.targetOf(index, point.label, point.tooltip),
					isOpen = isOpen,
					onOpenChange = { open -> openIndex.value = if (open) index else null }
				) {
					point.ringColor?.let { ringColor ->
						Box(
							modifier = Modifier
								.size(StatisticsChartDefaults.RingSize)
								.border(
									width = StatisticsChartDefaults.RingStrokeWidth,
									color = ringColor,
									shape = CircleShape
								)
								.testTag(StatisticsLabelUiTags.ring(labels.key, index))
						)
					}

					Box(
						modifier = Modifier
							.size(StatisticsChartDefaults.MarkerSize)
							.background(
								color = dataColor.copy(
									alpha = StatisticsChartDefaults.alphaOf(isOpen, hasSelection)
								),
								shape = CircleShape
							)
					)
				}
			}
		}
	}
}
