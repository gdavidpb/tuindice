package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationWeekDayItem

@Composable
fun EvaluationWeekDayView(
	item: EvaluationWeekDayItem,
	modifier: Modifier = Modifier
) {
	val selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
	val selectedContentColor = MaterialTheme.colorScheme.onSecondaryContainer
	val selectedAccentColor = MaterialTheme.colorScheme.secondary

	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = item.weekdayText,
			color = if (item.isSelected) selectedAccentColor else MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Bold
		)

		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.size(36.dp)
				.background(
					color = if (item.isSelected) selectedContainerColor else Color.Transparent,
					shape = CircleShape
				),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = item.dayText,
				color = if (item.isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)
		}

		Box(
			modifier = Modifier
				.padding(top = 3.dp)
				.size(4.dp)
				.background(
					color = if (item.hasEvaluations) selectedAccentColor else Color.Transparent,
					shape = CircleShape
				)
		)
	}
}
