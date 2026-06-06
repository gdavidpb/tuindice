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
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = item.weekdayText,
			color = if (item.isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.labelSmall,
			fontWeight = FontWeight.Bold
		)

		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.size(36.dp)
				.background(
					color = if (item.isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
					shape = CircleShape
				),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = item.dayText,
				color = if (item.isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Bold
			)
		}

		Box(
			modifier = Modifier
				.padding(top = 3.dp)
				.size(4.dp)
				.background(
					color = if (item.hasEvaluations) MaterialTheme.colorScheme.primary else Color.Transparent,
					shape = CircleShape
				)
		)
	}
}
