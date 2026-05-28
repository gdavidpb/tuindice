package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import com.gdavidpb.tuindice.subjects.ui.SubjectsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_chart_attempts_to_pass
import tuindice.subjects.generated.resources.subjects_chart_outcome_distribution

@Composable
fun SubjectDetailChartsView(
	detail: SubjectDetailItem,
	segment: SubjectDetailItem.SegmentItem
) {
	Column(
		modifier = Modifier.testTag(SubjectsUiTags.Charts),
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		if (detail.chartMode == SubjectDetailItem.ChartMode.QUALITATIVE_OUTCOMES) {
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
}
