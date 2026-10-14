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

	// The share of [widthDp] that each value takes, in order from the start. Negative or missing values take nothing,
	// and when the values add to nothing every span has zero width.
	fun segmentSpans(values: List<Float>, widthDp: Float): List<Span> {
		val parts = values.map { value -> if (value.isNaN() || value < 0f) 0f else value }
		val total = parts.sum()
		var start = 0f

		return parts.map { part ->
			val width = if (total > 0f) widthDp * part / total else 0f
			Span(startDp = start, widthDp = width).also { start += width }
		}
	}

	// The touch areas of the drawn segments of a bar of [totalDp], in order. They cover the bar without overlap, and each
	// is at least a finger wide where the bar has room for that. A segment narrower than a finger takes the room it lacks
	// from the wider segments, which give up only what they can spare, so every segment can be tapped.
	fun touchSpans(widthsDp: List<Float>, totalDp: Float): List<Span> {
		if (widthsDp.isEmpty()) return emptyList()

		val minimum = if (totalDp > 0f) min(MinTargetDp, totalDp / widthsDp.size) else 0f
		val targets = widthsDp.map { width -> max(width, minimum) }
		val excess = targets.sum() - totalDp
		val spare = targets.fold(0f) { sum, target -> sum + (target - minimum) }
		val areas = if (excess > 0f && spare > 0f) {
			targets.map { target -> target - excess * (target - minimum) / spare }
		} else {
			targets
		}

		var start = 0f

		return areas.map { width -> Span(startDp = start, widthDp = width).also { start += width } }
	}

	data class Span(
		val startDp: Float,
		val widthDp: Float
	)
}
