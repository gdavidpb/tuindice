package com.gdavidpb.tuindice.summary.ui.chart

import kotlin.math.max
import kotlin.math.min

/**
 * The arithmetic of the statistics charts, kept free of Compose so that it is tested as plain numbers. Lengths are
 * dp. A column is the slot of one point or bar; a target is the touch area of a point and is never narrower than
 * [MinTargetDp], which is also the least a finger can hit.
 */
object StatisticsChartGeometry {
	const val MinTargetDp = 48f

	// Past this many points or bars a chart scrolls rather than squeezing them in.
	const val DenseCount = 6

	private const val BarShareOfColumn = 0.6f
	private const val MaxBarWidthDp = 32f
	private const val HalfColumn = 0.5f
	private const val MiddleFraction = 0.5f

	// A dense chart gives every column the minimum target, so it scrolls; a sparse one shares the width and no column is
	// narrower than a target.
	fun columnWidth(availableWidthDp: Float, count: Int): Float = when {
		count <= 0 -> 0f
		count > DenseCount -> MinTargetDp
		else -> max(availableWidthDp / count, MinTargetDp)
	}

	fun contentWidth(columnWidthDp: Float, count: Int): Float = columnWidthDp * max(count, 0)

	fun columnCenter(index: Int, columnWidthDp: Float): Float = (index + HalfColumn) * columnWidthDp

	fun barWidth(columnWidthDp: Float): Float = min(columnWidthDp * BarShareOfColumn, MaxBarWidthDp)

	// A value on a range, 0 at the minimum and 1 at the maximum. A range with no width puts its values in the middle.
	fun valueFraction(value: Float, minValue: Float, maxValue: Float): Float = when {
		value.isNaN() -> 0f
		maxValue <= minValue -> MiddleFraction
		else -> ((value - minValue) / (maxValue - minValue)).coerceIn(0f, 1f)
	}

	// A bar's share of its scale. A scale that is not positive leaves every bar empty.
	fun barFraction(value: Float, scale: Float): Float = when {
		value.isNaN() || scale <= 0f -> 0f
		else -> (value / scale).coerceIn(0f, 1f)
	}

	fun scaleOf(values: List<Float>, reference: Float?): Float {
		return max(values.maxOrNull() ?: 0f, reference ?: 0f)
	}

	// The y of a fraction inside a band that starts at [topDp]: the top of the band is the full fraction.
	fun yOf(fraction: Float, topDp: Float, heightDp: Float): Float = topDp + heightDp * (1f - fraction)
}
