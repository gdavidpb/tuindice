package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import kotlin.math.roundToInt

@Composable
fun SubjectDetailChartsView(
	detail: SubjectDetailModel,
	segment: SubjectStatsSegment
) {
	if (detail.gradingMode == GradingMode.QUALITATIVE_PASS_FAIL) {
		SubjectDetailChartCard(
			title = "Distribución de resultado",
			values = listOf(
				segment.latestApprovedCount,
				segment.latestFailedCount,
				segment.latestRetiredCount,
				segment.latestUnreportedCount
			),
			labels = listOf("Apr", "Rep", "Ret", "Sin")
		)
	} else {
		SubjectDetailChartCard(
			title = "Distribución de nota",
			values = segment.latestGradeBins.map(SubjectGradeBin::count),
			labels = segment.latestGradeBins.map { bin -> bin.grade.toString() }
		)
	}

	SubjectDetailChartCard(
		title = "Intentos para aprobar",
		values = segment.attemptsToPassBins.map { bin -> bin.count },
		labels = segment.attemptsToPassBins.map { bin ->
			when (bin.bucket) {
				"1" -> "1"
				"2" -> "2"
				else -> "3+"
			}
		}
	)
}

@Composable
private fun SubjectDetailChartCard(
	title: String,
	values: List<Int>,
	labels: List<String>
) {
	val modelProducer = remember { CartesianChartModelProducer() }
	val bottomAxisValueFormatter = remember(labels) {
		CartesianValueFormatter { _, value, _ ->
			labels[value.roundToInt().coerceIn(labels.indices)]
		}
	}

	LaunchedEffect(values) {
		modelProducer.runTransaction {
			columnSeries {
				series(values)
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
				CartesianChartHost(
					chart = rememberCartesianChart(
						rememberColumnCartesianLayer(),
						startAxis = VerticalAxis.rememberStart(),
						bottomAxis = HorizontalAxis.rememberBottom(
							valueFormatter = bottomAxisValueFormatter,
							line = null,
							tick = null,
							guideline = null
						)
					),
					modelProducer = modelProducer,
					modifier = Modifier
						.fillMaxWidth()
						.height(180.dp)
				)
			} else {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(120.dp),
					contentAlignment = Alignment.Center
				) {
					Text(
						text = "Sin datos suficientes",
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}
