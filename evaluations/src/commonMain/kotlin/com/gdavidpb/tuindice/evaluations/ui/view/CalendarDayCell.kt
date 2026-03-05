package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.datetime.LocalDate

@Composable
fun CalendarDayCell(
	modifier: Modifier = Modifier,
	date: LocalDate?,
	selectedDate: LocalDate?,
	today: LocalDate,
	onDateSelected: (LocalDate) -> Unit
) {
	val isSelected = date != null && date == selectedDate
	val isToday = date != null && date == today
	val containerColor = when {
		isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
		isToday -> MaterialTheme.colorScheme.secondaryContainer
		else -> Color.Transparent
	}
	val textColor = when {
		isSelected -> MaterialTheme.colorScheme.primary
		isToday -> MaterialTheme.colorScheme.onSecondaryContainer
		else -> MaterialTheme.colorScheme.onSurface
	}

	Box(
		modifier = modifier
			.testTag(
				if (date != null) {
					EvaluationsUiTags.calendarDayCell(date.day)
				} else {
					"evaluation_calendar_day_placeholder"
				}
			)
			.aspectRatio(1f)
			.clip(CircleShape)
			.background(containerColor)
			.clickable(enabled = date != null) {
				if (date != null) {
					onDateSelected(date)
				}
			},
		contentAlignment = Alignment.Center
	) {
		if (date != null) {
			Text(
				text = date.day.toString(),
				style = MaterialTheme.typography.bodyMedium,
				color = textColor,
				textAlign = TextAlign.Center
			)
		}
	}
}
