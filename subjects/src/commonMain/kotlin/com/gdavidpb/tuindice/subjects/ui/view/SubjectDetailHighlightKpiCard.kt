package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha

@Composable
fun SubjectDetailHighlightKpiCard(
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
				color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = TuIndiceAlpha.Deemphasis)
			)
		}
	}
}
