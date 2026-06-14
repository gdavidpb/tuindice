package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.presentation.mapper.toCompactCountText
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.decoration.Decoration
import com.patrykandpatrick.vico.compose.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_chart_no_data

@Composable
fun SubjectDetailBarChartCard(
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
								columnProvider = columnProvider ?: rememberSubjectDetailBarChartColumnProvider()
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
