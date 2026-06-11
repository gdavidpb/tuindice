package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.utils.currentEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.utils.formatMonthYear
import com.gdavidpb.tuindice.evaluations.presentation.utils.toCalendarGrid
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.datetime.LocalDate

@Composable
fun EvaluationCalendarContent(
	displayedMonth: LocalDate,
	selectedDate: LocalDate?,
	onPreviousMonthClick: () -> Unit,
	onNextMonthClick: () -> Unit,
	onDateSelected: (LocalDate) -> Unit
) {
	Column(
		modifier = Modifier.testTag(EvaluationsUiTags.EvaluationCalendarContainer),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			IconButton(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationCalendarPreviousMonthButton),
				onClick = onPreviousMonthClick
			) {
				Icon(
					imageVector = Icons.Outlined.ChevronLeft,
					contentDescription = null
				)
			}

			Text(
				modifier = Modifier
					.testTag(EvaluationsUiTags.EvaluationCalendarMonthLabel)
					.weight(1f),
				text = displayedMonth.formatMonthYear(),
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold,
				textAlign = TextAlign.Center
			)

			IconButton(
				modifier = Modifier.testTag(EvaluationsUiTags.EvaluationCalendarNextMonthButton),
				onClick = onNextMonthClick
			) {
				Icon(
					imageVector = Icons.Outlined.ChevronRight,
					contentDescription = null
				)
			}
		}

		WeekdayHeaderRow()

		displayedMonth.toCalendarGrid().chunked(7).forEach { week ->
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.spacedBy(4.dp)
			) {
				week.forEach { date ->
					CalendarDayCell(
						modifier = Modifier.weight(1f),
						date = date,
						selectedDate = selectedDate,
						today = currentEvaluationLocalDate(),
						onDateSelected = onDateSelected
					)
				}
			}
		}
	}
}
