package com.gdavidpb.tuindice.subjects.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetail as SubjectDetailModel
import com.gdavidpb.tuindice.subjects.domain.model.SubjectGradeBin
import com.gdavidpb.tuindice.subjects.domain.model.SubjectSegmentTab
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.columnSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SubjectDetailScreen(
	state: SubjectDetail.State,
	careerTabText: String,
	globalTabText: String,
	unavailableTitle: String,
	unavailableBody: String,
	failedTitle: String,
	retryText: String,
	closeText: String,
	onRetryClick: () -> Unit,
	onTabSelected: (SubjectSegmentTab) -> Unit,
	onDismissRequest: () -> Unit
) {
	when (val resolvedState = state) {
		SubjectDetail.State.Loading ->
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 48.dp)
					.testTag(SubjectsUiTags.Loading),
				contentAlignment = Alignment.Center
			) {
				CircularProgressIndicator()
			}

		is SubjectDetail.State.Content ->
			SubjectDetailContent(
				modifier = Modifier.testTag(SubjectsUiTags.Content),
				detail = resolvedState.detail,
				selectedTab = resolvedState.selectedTab,
				careerTabText = careerTabText,
				globalTabText = globalTabText,
				onTabSelected = onTabSelected
			)

		is SubjectDetail.State.Unavailable ->
			MessageState(
				modifier = Modifier.testTag(SubjectsUiTags.Unavailable),
				title = unavailableTitle,
				body = unavailableBody,
				actionText = closeText,
				onActionClick = onDismissRequest
			)

		is SubjectDetail.State.Failed ->
			MessageState(
				modifier = Modifier.testTag(SubjectsUiTags.Failed),
				title = failedTitle,
				body = resolvedState.subjectCode,
				actionText = retryText,
				onActionClick = onRetryClick,
				actionTestTag = SubjectsUiTags.Retry
			)
	}
}

@Composable
private fun SubjectDetailContent(
	modifier: Modifier = Modifier,
	detail: SubjectDetailModel,
	selectedTab: SubjectSegmentTab,
	careerTabText: String,
	globalTabText: String,
	onTabSelected: (SubjectSegmentTab) -> Unit
) {
	val colors = remember(detail.id) { CourseCodeColorGenerator.fromCode(detail.id) }
	val segment = when (selectedTab) {
		SubjectSegmentTab.CAREER -> detail.careerSegment ?: detail.globalSegment
		SubjectSegmentTab.GLOBAL -> detail.globalSegment ?: detail.careerSegment
	} ?: return

	Column(
		modifier = modifier
			.fillMaxWidth()
			.verticalScroll(rememberScrollState())
			.padding(horizontal = 20.dp, vertical = 8.dp),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
			Text(
				text = detail.name ?: detail.id,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.SemiBold
			)

			Row(
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					modifier = Modifier
						.background(
							color = colors.containerColor,
							shape = RoundedCornerShape(10.dp)
						)
						.padding(horizontal = 12.dp, vertical = 6.dp),
					text = detail.id,
					color = colors.color,
					style = MaterialTheme.typography.labelLarge,
					fontWeight = FontWeight.SemiBold
				)
				detail.credits?.let { credits ->
					Text(
						text = "$credits uc",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
				detail.gradingMode?.let { gradingMode ->
					Text(
						text = if (gradingMode.name == "QUALITATIVE_PASS_FAIL") "Cualitativa" else "Numérica",
						style = MaterialTheme.typography.bodyMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}

		if (detail.careerSegment != null && detail.globalSegment != null) {
			Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
				FilterChip(
					selected = selectedTab == SubjectSegmentTab.CAREER,
					onClick = { onTabSelected(SubjectSegmentTab.CAREER) },
					label = { Text(careerTabText) },
					modifier = Modifier.testTag(SubjectsUiTags.CareerTab)
				)
				FilterChip(
					selected = selectedTab == SubjectSegmentTab.GLOBAL,
					onClick = { onTabSelected(SubjectSegmentTab.GLOBAL) },
					label = { Text(globalTabText) },
					modifier = Modifier.testTag(SubjectsUiTags.GlobalTab)
				)
			}
		}

		Text(
			text = "${segment.sampleStudents} estudiantes, ${segment.closedAttempts} intentos cerrados",
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		FlowRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp),
			verticalArrangement = Arrangement.spacedBy(12.dp)
		) {
			KpiCard(
				title = "Aprobación eventual",
				value = segment.eventualPassRate.toPercentText()
			)
			KpiCard(
				title = "Primer intento",
				value = segment.firstAttemptPassRate.toPercentText()
			)
			KpiCard(
				title = "Promedio intentos",
				value = segment.avgAttemptsToPass.toDecimalText()
			)
		}

		if (detail.gradingMode?.name == "QUALITATIVE_PASS_FAIL") {
			ChartCard(
				title = "Outcomes más recientes",
				values = listOf(
					segment.latestApprovedCount,
					segment.latestFailedCount,
					segment.latestRetiredCount,
					segment.latestUnreportedCount
				),
				labels = listOf("Apr", "Rep", "Ret", "Sin")
			)
		} else {
			ChartCard(
				title = "Distribución de nota",
				values = segment.latestGradeBins.map(SubjectGradeBin::count),
				labels = segment.latestGradeBins.map { bin -> bin.grade.toString() }
			)
		}

		ChartCard(
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

		Text(
			text = "Actualizado ${detail.generatedAt.toDateText()}",
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(modifier = Modifier.height(8.dp))
	}
}

@Composable
private fun KpiCard(
	title: String,
	value: String
) {
	ElevatedCard(
		modifier = Modifier.width(156.dp)
	) {
		Column(
			modifier = Modifier.padding(16.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = value,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.SemiBold
			)
		}
	}
}

@Composable
private fun ChartCard(
	title: String,
	values: List<Int>,
	labels: List<String>
) {
	val modelProducer = remember { CartesianChartModelProducer() }

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
						startAxis = VerticalAxis.rememberStart()
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

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween
			) {
				labels.forEach { label ->
					Text(
						text = label,
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
		}
	}
}

@Composable
private fun MessageState(
	modifier: Modifier = Modifier,
	title: String,
	body: String,
	actionText: String,
	onActionClick: () -> Unit,
	actionTestTag: String? = null
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(horizontal = 20.dp, vertical = 24.dp),
		verticalArrangement = Arrangement.spacedBy(14.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.SemiBold
		)
		Text(
			text = body,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
		TextButton(
			onClick = onActionClick,
			modifier = if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier
		) {
			Text(actionText)
		}
	}
}

private fun Double?.toPercentText(): String {
	return if (this == null) "--" else "${(this * 100).toInt()}%"
}

private fun Double?.toDecimalText(): String {
	if (this == null) return "--"
	val rounded = (this * 10).roundToInt() / 10.0
	val integerPart = rounded.toInt()
	val decimalPart = ((abs(rounded) * 10).roundToInt()) % 10
	return "$integerPart.$decimalPart"
}

private fun Long.toDateText(): String {
	val localDate = Instant.fromEpochMilliseconds(this)
		.toLocalDateTime(TimeZone.currentSystemDefault())
		.date
	return "${localDate.dayOfMonth}/${localDate.monthNumber}/${localDate.year}"
}
