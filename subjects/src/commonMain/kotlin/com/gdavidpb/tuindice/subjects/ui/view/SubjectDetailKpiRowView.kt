package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.subjects.domain.model.SubjectStatsSegment
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SubjectDetailKpiRowView(
	segment: SubjectStatsSegment
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(12.dp)
	) {
		SubjectDetailKpiCard(
			modifier = Modifier.weight(1f),
			title = "Aprobación eventual",
			value = segment.eventualPassRate.toPercentText()
		)
		SubjectDetailKpiCard(
			modifier = Modifier.weight(1f),
			title = "Primer intento",
			value = segment.firstAttemptPassRate.toPercentText()
		)
		SubjectDetailKpiCard(
			modifier = Modifier.weight(1f),
			title = "Promedio intentos",
			value = segment.avgAttemptsToPass.toDecimalText()
		)
	}
}

@Composable
private fun SubjectDetailKpiCard(
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
	return if (this == null) "--" else "${(this * 100).toInt()}%"
}

private fun Double?.toDecimalText(): String {
	if (this == null) return "--"
	val rounded = (this * 10).roundToInt() / 10.0
	val integerPart = rounded.toInt()
	val decimalPart = ((abs(rounded) * 10).roundToInt()) % 10
	return "$integerPart.$decimalPart"
}
