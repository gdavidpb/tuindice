package com.gdavidpb.tuindice.summary.ui.chart

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.model.RichTooltipText

/**
 * A segment of a [StatisticsStackedBar]: its [value] out of the whole bar, drawn in [color]. [label] names it for a
 * screen reader, and [tooltip] is what it shows when it is open. A segment with no value is not drawn and has no
 * target.
 */
@Immutable
data class StatisticsSegment(
	val label: String,
	val value: Float,
	val color: Color,
	val tooltip: RichTooltipText
)
