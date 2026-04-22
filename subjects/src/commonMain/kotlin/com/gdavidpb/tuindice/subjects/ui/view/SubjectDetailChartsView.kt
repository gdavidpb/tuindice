package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.presentation.mapper.toCompactCountText
import com.gdavidpb.tuindice.subjects.ui.model.SubjectGradeChartSummary
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.component.LineComponent
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_chart_attempts_to_pass
import tuindice.subjects.generated.resources.subjects_chart_grade_distribution
import tuindice.subjects.generated.resources.subjects_chart_no_data
import tuindice.subjects.generated.resources.subjects_chart_outcome_distribution

@Composable
fun SubjectDetailChartsView(
	detail: SubjectDetailModel,
	segment: SubjectStatsSegment
) {
	if (detail.gradingMode == GradingMode.QUALITATIVE_PASS_FAIL) {
		SubjectDetailBarChartCard(
			title = stringResource(Res.string.subjects_chart_outcome_distribution),
			xValues = listOf(1, 2, 3, 4),
			values = listOf(
				segment.latestApprovedCount,
				segment.latestFailedCount,
				segment.latestRetiredCount,
				segment.latestUnreportedCount
			),
			labelForX = { xValue ->
				when (xValue) {
					1 -> "Apr"
					2 -> "Rep"
					3 -> "Ret"
					else -> "Sin"
				}
			}
		)
	} else {
		SubjectDetailNumericGradeChartCard(
			segment = segment
		)
	}

	SubjectDetailBarChartCard(
		title = stringResource(Res.string.subjects_chart_attempts_to_pass),
		xValues = listOf(1, 2, 3),
		values = listOf(
			segment.attemptsToPassBins.firstOrNull { bin -> bin.bucket == "1" }?.count ?: 0,
			segment.attemptsToPassBins.firstOrNull { bin -> bin.bucket == "2" }?.count ?: 0,
			segment.attemptsToPassBins.firstOrNull { bin -> bin.bucket == "3_plus" }?.count ?: 0
		),
		labelForX = { xValue ->
			when (xValue) {
				1 -> "1"
				2 -> "2"
				else -> "3+"
			}
		}
	)
}

@Composable
private fun SubjectDetailNumericGradeChartCard(
	segment: SubjectStatsSegment
) {
	val gradeBins = remember(segment.latestGradeBins) {
		(1..5).map { grade ->
			SubjectGradeBin(
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
	val baseBarColor = Color(0xFF4A8DFF)
	val highlightedBarColor = Color(0xFF88BAFF)
	val barShape = remember { RoundedCornerShape(10.dp) }
	val defaultColumn = remember(barShape) {
		LineComponent(
			fill = Fill(baseBarColor),
			thickness = 24.dp,
			shape = barShape
		)
	}
	val highlightedColumn = remember(barShape) {
		LineComponent(
			fill = Fill(highlightedBarColor),
			thickness = 24.dp,
			shape = barShape,
			strokeFill = Fill(Color.White.copy(alpha = 0.35f)),
			strokeThickness = 1.dp
		)
	}
	val modalGrades = summary.modalGrades.toSet()
	val columnProvider = remember(defaultColumn, highlightedColumn, modalGrades) {
		object : ColumnCartesianLayer.ColumnProvider {
			override fun getColumn(
				entry: com.patrykandpatrick.vico.compose.cartesian.data.ColumnCartesianLayerModel.Entry,
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
	val bandDecoration = remember(summary.stddevRangeStart, summary.stddevRangeEnd) {
		if (summary.stddevRangeStart != null && summary.stddevRangeEnd != null) {
			VerticalRangeBandDecoration(
				startX = { summary.stddevRangeStart },
				endX = { summary.stddevRangeEnd },
				component = ShapeComponent(
					fill = Fill(Color.White.copy(alpha = 0.08f)),
					shape = RoundedCornerShape(12.dp)
				)
			)
		} else {
			null
		}
	}
	val medianLine = remember {
		LineComponent(
			fill = Fill(Color.White.copy(alpha = 0.72f)),
			thickness = 2.dp,
			shape = RoundedCornerShape(999.dp)
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
		xValues = gradeBins.map(SubjectGradeBin::grade),
		values = gradeBins.map(SubjectGradeBin::count),
		labelForX = { xValue -> xValue.toString() },
		columnProvider = columnProvider,
		decorations = listOfNotNull(bandDecoration, medianDecoration)
	)
}

@Composable
private fun SubjectDetailBarChartCard(
	title: String,
	xValues: List<Int>,
	values: List<Int>,
	labelForX: (Int) -> String,
	columnProvider: ColumnCartesianLayer.ColumnProvider? = null,
	decorations: List<Decoration> = emptyList(),
	overlay: @Composable BoxScope.() -> Unit = {}
) {
	val modelProducer = remember { CartesianChartModelProducer() }
	val labelMap = remember(xValues, labelForX) { xValues.associateWith(labelForX) }
	val bottomAxisValueFormatter = remember(labelMap) {
		CartesianValueFormatter { _, value, _ ->
			labelMap[value.roundToInt()].orEmpty()
		}
	}
	val startAxisValueFormatter = remember {
		CartesianValueFormatter { _, value, _ ->
			value.roundToInt().toCompactCountText()
		}
	}

	LaunchedEffect(xValues, values) {
		modelProducer.runTransaction {
			columnSeries {
				series(x = xValues, y = values)
			}
		}
	}

	ElevatedCard {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold
			)

			if (values.any { value -> value > 0 }) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(196.dp)
				) {
					CartesianChartHost(
						chart = rememberCartesianChart(
							rememberColumnCartesianLayer(
								columnProvider = columnProvider ?: rememberDefaultColumnProvider()
							),
							startAxis = VerticalAxis.rememberStart(
								valueFormatter = startAxisValueFormatter
							),
							bottomAxis = HorizontalAxis.rememberBottom(
								valueFormatter = bottomAxisValueFormatter,
								line = null,
								tick = null,
								guideline = null
							),
							decorations = decorations
						),
						modelProducer = modelProducer,
						modifier = Modifier.fillMaxSize()
					)
					overlay()
				}
			} else {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(120.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = stringResource(Res.string.subjects_chart_no_data),
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}

@Composable
private fun rememberDefaultColumnProvider(): ColumnCartesianLayer.ColumnProvider {
	val baseColumn = remember {
		LineComponent(
			fill = Fill(Color(0xFF4A8DFF)),
			thickness = 24.dp,
			shape = RoundedCornerShape(10.dp)
		)
	}
	return remember(baseColumn) {
		ColumnCartesianLayer.ColumnProvider.series(baseColumn)
	}
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
