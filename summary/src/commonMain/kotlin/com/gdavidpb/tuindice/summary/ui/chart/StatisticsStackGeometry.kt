package com.gdavidpb.tuindice.summary.ui.chart

import kotlin.math.max
import kotlin.math.min

/**
 * The arithmetic of the stacked bars, kept free of Compose like [StatisticsChartGeometry]: how the width of a bar
 * is shared among its segments, which segments are drawn, and where the touch area of each one lies. Lengths are dp.
 */
object StatisticsStackGeometry {
	// The share of [widthDp] that each value takes, in order from the start. Negative or missing values take nothing, and
	// when the values add to nothing every span has zero width.
	fun segmentSpans(values: List<Float>, widthDp: Float): List<Span> {
		val parts = values.map { value -> if (value.isNaN() || value < 0f) 0f else value }
		val total = parts.sum()
		var start = 0f

		return parts.map { part ->
			val width = if (total > 0f) widthDp * part / total else 0f
			Span(startDp = start, widthDp = width).also { start += width }
		}
	}

	// The segments a stacked bar draws, in order: those with a value above zero, each with its share of their total. A
	// segment with no value is not drawn and has no target, so the shares of the drawn segments add up to one.
	fun drawnSegments(values: List<Float>): List<DrawnSegment> {
		val drawn = values.withIndex().filter { (_, value) -> value > 0f }
		val total = drawn.sumOf { (_, value) -> value.toDouble() }.toFloat()

		return drawn.map { (index, value) -> DrawnSegment(index = index, fraction = value / total) }
	}

	// The touch areas of the drawn segments of a bar of [totalDp], in order. They cover the bar without overlap, and each
	// is at least a finger wide where the bar has room for that. A segment narrower than a finger takes the room it lacks
	// from the wider segments, which give up only what they can spare, so every segment can be tapped.
	fun touchSpans(widthsDp: List<Float>, totalDp: Float): List<Span> {
		if (widthsDp.isEmpty()) return emptyList()

		val minimum = if (totalDp > 0f) min(StatisticsChartGeometry.MinTargetDp, totalDp / widthsDp.size) else 0f
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

	// A drawn segment: its [index] in the values it came from, and its [fraction] of the drawn total.
	data class DrawnSegment(
		val index: Int,
		val fraction: Float
	)
}
