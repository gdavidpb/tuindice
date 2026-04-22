package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDifficultyBand
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import kotlin.math.roundToInt
import org.jetbrains.compose.resources.stringResource
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.subjects_kpi_approval
import tuindice.subjects.generated.resources.subjects_kpi_difficulty
import tuindice.subjects.generated.resources.subjects_kpi_failure
import tuindice.subjects.generated.resources.subjects_kpi_first_attempt
import tuindice.subjects.generated.resources.subjects_kpi_withdrawal

@Composable
fun SubjectDetailKpiRowView(
	segment: SubjectStatsSegment
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
				value = segment.difficultyScore.toScoreText(),
				supporting = segment.difficultyBand.toDisplayText()
			)
			SubjectDetailStandardKpiCard(
				modifier = Modifier
					.weight(0.85f)
					.fillMaxHeight(),
				title = stringResource(Res.string.subjects_kpi_first_attempt),
				value = segment.firstAttemptPassRate.toPercentText()
			)
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			SubjectDetailStandardKpiCard(
				modifier = Modifier.weight(1f),
				title = stringResource(Res.string.subjects_kpi_approval),
				value = segment.approvalRate.toPercentText()
			)
			SubjectDetailStandardKpiCard(
				modifier = Modifier.weight(1f),
				title = stringResource(Res.string.subjects_kpi_failure),
				value = segment.latestFailureRate.toPercentText()
			)
			SubjectDetailStandardKpiCard(
				modifier = Modifier.weight(1f),
				title = stringResource(Res.string.subjects_kpi_withdrawal),
				value = segment.latestWithdrawalRate.toPercentText()
			)
		}
	}
}

@Composable
private fun SubjectDetailHighlightKpiCard(
	modifier: Modifier = Modifier,
	title: String,
	value: String,
	supporting: String
) {
	ElevatedCard(
		modifier = modifier,
		colors = CardDefaults.elevatedCardColors(
			containerColor = MaterialTheme.colorScheme.primaryContainer,
			contentColor = MaterialTheme.colorScheme.onPrimaryContainer
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
			Text(
				text = value,
				style = MaterialTheme.typography.headlineMedium,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
			Text(
				text = supporting,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
			)
		}
	}
}

@Composable
private fun SubjectDetailStandardKpiCard(
	modifier: Modifier = Modifier,
	title: String,
	value: String
) {
	ElevatedCard(
		modifier = modifier,
		colors = CardDefaults.elevatedCardColors(
			containerColor = MaterialTheme.colorScheme.secondaryContainer,
			contentColor = MaterialTheme.colorScheme.onSecondaryContainer
		)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Text(
				text = title,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSecondaryContainer
			)
			Text(
				text = value,
				style = MaterialTheme.typography.headlineSmall,
				fontWeight = FontWeight.SemiBold,
				color = MaterialTheme.colorScheme.onSecondaryContainer
			)
		}
	}
}

private fun Double?.toPercentText(): String {
	return if (this == null) "--" else "${(this * 100).roundToInt()}%"
}

private fun Int?.toScoreText(): String {
	return if (this == null) "--" else "$this/100"
}

private fun SubjectDifficultyBand?.toDisplayText(): String {
	return when (this) {
		SubjectDifficultyBand.LOW -> "Baja"
		SubjectDifficultyBand.MEDIUM -> "Media"
		SubjectDifficultyBand.HIGH -> "Alta"
		SubjectDifficultyBand.VERY_HIGH -> "Muy alta"
		null -> "--"
	}
}
