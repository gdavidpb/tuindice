package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.presentation.model.SubjectDetailItem
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_kpi_approval
import tuindice.subjects.generated.resources.subjects_kpi_difficulty
import tuindice.subjects.generated.resources.subjects_kpi_failure
import tuindice.subjects.generated.resources.subjects_kpi_first_attempt
import tuindice.subjects.generated.resources.subjects_kpi_withdrawal

@Composable
fun SubjectDetailKpiRowView(
	segment: SubjectDetailItem.SegmentItem
) {
	Column(
		verticalArrangement = Arrangement.spacedBy(12.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.height(IntrinsicSize.Min),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			SubjectDetailHighlightKpiCard(
				modifier = Modifier
					.weight(1.15f)
					.fillMaxHeight(),
				title = stringResource(Res.string.subjects_kpi_difficulty),
				value = segment.difficultyScoreText,
				supporting = segment.difficultyBandText
			)
			SubjectDetailStandardKpiCard(
				modifier = Modifier
					.weight(0.85f)
					.fillMaxHeight(),
				title = stringResource(Res.string.subjects_kpi_first_attempt),
				value = segment.firstAttemptPassRateText
			)
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			SubjectDetailStandardKpiCard(
				modifier = Modifier.weight(1f),
				title = stringResource(Res.string.subjects_kpi_approval),
				value = segment.approvalRateText
			)
			SubjectDetailStandardKpiCard(
				modifier = Modifier.weight(1f),
				title = stringResource(Res.string.subjects_kpi_failure),
				value = segment.failureRateText
			)
			SubjectDetailStandardKpiCard(
				modifier = Modifier.weight(1f),
				title = stringResource(Res.string.subjects_kpi_withdrawal),
				value = segment.withdrawalRateText
			)
		}
	}
}
