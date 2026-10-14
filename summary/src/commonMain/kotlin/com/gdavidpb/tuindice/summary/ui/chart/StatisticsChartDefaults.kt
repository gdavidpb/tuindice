package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAnimation
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius

/**
 * The look of the statistics charts: their data colour, sizes and durations. The data is the load band's light blue,
 * the colour a term's light load already wears, so a chart and a term read alike. When something is open its point
 * stays in full colour and the rest fade to [DimmedAlpha]; nothing is drawn in grey, and a bar grows in the standard
 * time.
 */
object StatisticsChartDefaults {
	val TargetMinSize = StatisticsChartGeometry.MinTargetDp.dp

	val ChartHeight = 168.dp
	val ReferenceLabelSpace = 20.dp
	val ReferenceLabelHeight = 16.dp

	// Bars stand on the plot, which leaves the space above it for a reference label. A line keeps half a target clear of
	// the top and the bottom, so a point at either end is still a whole target.
	val PlotTop = ReferenceLabelSpace
	val PlotHeight = ChartHeight - ReferenceLabelSpace
	val LineInset = TargetMinSize / 2f
	val LineBandHeight = ChartHeight - LineInset * 2f

	// Under the plot, each column names its point in a band of one small line that scrolls with the columns. A value
	// written above a bar takes one line of its own, which the bar's top leaves clear.
	val AxisLabelBandHeight = 20.dp
	val ValueLabelHeight = 16.dp
	val ContentHeight = ChartHeight + AxisLabelBandHeight

	val MarkerSize = 8.dp
	val RingSize = 19.dp
	val RingStrokeWidth = 1.5.dp
	val LineStrokeWidth = 2.dp
	val SegmentHeight = 24.dp
	val EdgeFadeWidth = 24.dp
	val ReferenceDashLength = 6.dp
	val ReferenceGapLength = 4.dp
	val BarCornerRadius = TuIndiceRadius.ExtraSmall

	const val DimmedAlpha = TuIndiceAlpha.Muted
	const val GrowMillis = TuIndiceAnimation.StandardMillis

	@Composable
	fun dataColor(): Color = AcademicStatusColors.loadBandLight()

	fun alphaOf(isSelected: Boolean, hasSelection: Boolean): Float {
		return if (hasSelection && !isSelected) DimmedAlpha else 1f
	}
}
