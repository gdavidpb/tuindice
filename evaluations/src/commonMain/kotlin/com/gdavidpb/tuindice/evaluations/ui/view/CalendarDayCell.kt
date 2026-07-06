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
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.datetime.LocalDate

@Composable
fun CalendarDayCell(
	modifier: Modifier = Modifier,
	date: LocalDate?,
	selectedDate: LocalDate?,
	today: LocalDate,
	isSelectable: Boolean = true,
	onDateSelected: (LocalDate) -> Unit
) {
	val isSelected = date != null && date == selectedDate
	val isToday = date != null && date == today
	val containerColor = when {
		isSelected -> MaterialTheme.colorScheme.secondaryContainer
		isToday && isSelectable -> MaterialTheme.colorScheme.secondaryContainer
		else -> Color.Transparent
	}
	val textColor = when {
		isSelected -> MaterialTheme.colorScheme.onSecondaryContainer
		isToday && isSelectable -> MaterialTheme.colorScheme.onSecondaryContainer
		// Days outside the term range stay visible but read as disabled.
		!isSelectable -> MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Disabled)
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
			.clickable(enabled = date != null && isSelectable) {
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
