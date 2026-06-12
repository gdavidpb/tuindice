package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.ui.model.SubjectChartDefaults
import com.gdavidpb.tuindice.subjects.ui.model.SubjectGradeChartSummary
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_chart_grade_distribution

@Composable
fun SubjectDetailNumericGradeChartCard(
	segment: SubjectDetailItem.SegmentItem
) {
	val gradeBins = remember(segment.latestGradeBins) {
		(1..5).map { grade ->
			SubjectDetailItem.GradeBinItem(
				grade = grade,
				count = segment.latestGradeBins.firstOrNull { bin -> bin.grade == grade }?.count ?: 0
			)
		}
	}
	val summary = remember(gradeBins, segment.medianGrade, segment.stddevGrade) {
		SubjectGradeChartSummary.from(
			latestGradeBins = gradeBins,
			medianGrade = segment.medianGrade,
			stddevGrade = segment.stddevGrade
		)
	}
	val markerColor = MaterialTheme.colorScheme.onSurface
	val barShape = remember { RoundedCornerShape(TuIndiceRadius.Medium) }
	val defaultColumn = remember(barShape) {
		LineComponent(
			fill = Fill(SubjectChartDefaults.BarColor),
			thickness = 24.dp,
			shape = barShape
		)
	}
	val highlightedColumn = remember(barShape, markerColor) {
		LineComponent(
			fill = Fill(SubjectChartDefaults.HighlightedBarColor),
			thickness = 24.dp,
			shape = barShape,
			strokeFill = Fill(markerColor.copy(alpha = 0.35f)),
			strokeThickness = 1.dp
		)
	}
	val modalGrades = summary.modalGrades.toSet()
	val columnProvider = remember(defaultColumn, highlightedColumn, modalGrades) {
		object : ColumnCartesianLayer.ColumnProvider {
			override fun getColumn(
				entry: ColumnCartesianLayerModel.Entry,
				seriesIndex: Int,
				extraStore: ExtraStore
			): LineComponent {
				return if (entry.x.roundToInt() in modalGrades) highlightedColumn else defaultColumn
			}

			override fun getWidestSeriesColumn(
				seriesIndex: Int,
				extraStore: ExtraStore
			): LineComponent = highlightedColumn
		}
	}
	val bandDecoration = remember(summary.stddevRangeStart, summary.stddevRangeEnd, markerColor) {
		if (summary.stddevRangeStart != null && summary.stddevRangeEnd != null) {
			VerticalRangeBandDecoration(
				startX = { summary.stddevRangeStart },
				endX = { summary.stddevRangeEnd },
				component = ShapeComponent(
					fill = Fill(markerColor.copy(alpha = 0.08f)),
					shape = RoundedCornerShape(TuIndiceRadius.Medium)
				)
			)
		} else {
			null
		}
	}
	val medianLine = remember(markerColor) {
		LineComponent(
			fill = Fill(markerColor.copy(alpha = TuIndiceAlpha.Deemphasis)),
			thickness = 2.dp,
			shape = RoundedCornerShape(TuIndiceRadius.Full)
		)
	}
	val medianDecoration = remember(summary.medianGrade) {
		if (summary.medianGrade != null) {
			VerticalMarkerLineDecoration(
				x = { summary.medianGrade },
				line = medianLine
			)
		} else {
			null
		}
	}
	SubjectDetailBarChartCard(
		title = stringResource(Res.string.subjects_chart_grade_distribution),
		xValues = gradeBins.map(SubjectDetailItem.GradeBinItem::grade),
		values = gradeBins.map(SubjectDetailItem.GradeBinItem::count),
		labelForX = { xValue -> xValue.toString() },
		columnProvider = columnProvider,
		decorations = listOfNotNull(bandDecoration, medianDecoration)
	)
}

private class VerticalRangeBandDecoration(
	private val startX: (ExtraStore) -> Double?,
	private val endX: (ExtraStore) -> Double?,
	private val component: ShapeComponent
) : Decoration {
	override fun drawUnderLayers(context: CartesianDrawingContext) {
		val start = startX(context.model.extraStore) ?: return
		val end = endX(context.model.extraStore) ?: return
		if (end <= start) return
		val startCanvasX = context.toCanvasX(start)
		val endCanvasX = context.toCanvasX(end)
		component.draw(
			context = context,
			left = minOf(startCanvasX, endCanvasX),
			top = context.layerBounds.top,
			right = maxOf(startCanvasX, endCanvasX),
			bottom = context.layerBounds.bottom
		)
	}
}

private class VerticalMarkerLineDecoration(
	private val x: (ExtraStore) -> Double?,
	private val line: LineComponent
) : Decoration {
	override fun drawOverLayers(context: CartesianDrawingContext) {
		val xValue = x(context.model.extraStore) ?: return
		val canvasX = context.toCanvasX(xValue)
		val dashHeight = with(context) { 9.dp.pixels }
		val gapHeight = with(context) { 6.dp.pixels }
		var currentTop = context.layerBounds.top
		while (currentTop < context.layerBounds.bottom) {
			line.drawVertical(
				context = context,
				x = canvasX,
				top = currentTop,
				bottom = minOf(currentTop + dashHeight, context.layerBounds.bottom)
			)
			currentTop += dashHeight + gapHeight
		}
	}
}

private fun CartesianDrawingContext.toCanvasX(xValue: Double): Float {
	val xStep = ranges.xStep.takeUnless { step -> step == 0.0 } ?: 1.0
	val offset = ((xValue - ranges.minX) / xStep).toFloat() * layerDimensions.xSpacing
	return if (isLtr) {
		layerBounds.left + layerDimensions.startPadding + offset
	} else {
		layerBounds.right - layerDimensions.startPadding - offset
	}
}
