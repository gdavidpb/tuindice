package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.presentation.extension.*
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsShortDayOfWeekAndDate
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.*

@Composable
fun EvaluationDatePicker(
	modifier: Modifier = Modifier,
	selectedDate: Long?,
	onDateChange: (date: Long?) -> Unit
) {
	val committedDate = selectedDate?.toEvaluationLocalDate()
	val isPickerDialogOpen = remember { mutableStateOf(false) }
	val draftSelectedDate = remember(selectedDate) { mutableStateOf(committedDate) }
	val displayedMonth = remember(selectedDate) {
		mutableStateOf((committedDate ?: currentEvaluationLocalDate()).monthStart())
	}

	fun resetDialogState() {
		draftSelectedDate.value = committedDate
		displayedMonth.value = (committedDate ?: currentEvaluationLocalDate()).monthStart()
	}

	if (isPickerDialogOpen.value) {
		AlertDialog(
			onDismissRequest = {
				resetDialogState()
				isPickerDialogOpen.value = false
			},
			title = {
				Text(text = stringResource(Res.string.label_add_evaluation_date))
			},
			text = {
				EvaluationCalendarContent(
					displayedMonth = displayedMonth.value,
					selectedDate = draftSelectedDate.value,
					onPreviousMonthClick = {
						displayedMonth.value = displayedMonth.value.previousMonthStart()
					},
					onNextMonthClick = {
						displayedMonth.value = displayedMonth.value.nextMonthStart()
					},
					onDateSelected = { date ->
						draftSelectedDate.value = date
					}
				)
			},
			confirmButton = {
				TextButton(
					onClick = {
						isPickerDialogOpen.value = false
						onDateChange(draftSelectedDate.value?.toEvaluationEpochMillis())
					},
					enabled = draftSelectedDate.value != null
				) {
					Text(text = stringResource(Res.string.accept))
				}
			},
			dismissButton = {
				TextButton(
					onClick = {
						resetDialogState()
						isPickerDialogOpen.value = false
					}
				) {
					Text(text = stringResource(Res.string.cancel))
				}
			}
		)
	}

	Row(modifier = modifier) {
		OutlinedButton(
			modifier = Modifier
				.offset(x = 0.5.dp)
				.weight(0.5f),
			colors = if (committedDate != null) {
				ButtonDefaults.filledTonalButtonColors()
			} else {
				ButtonDefaults.outlinedButtonColors()
			},
			onClick = {
				resetDialogState()
				isPickerDialogOpen.value = true
			},
			shape = RoundedCornerShape(
				topStartPercent = 50,
				topEndPercent = 0,
				bottomEndPercent = 0,
				bottomStartPercent = 50
			)
		) {
			Icon(
				modifier = Modifier
					.offset(x = (-8).dp),
				imageVector = Icons.Outlined.Event,
				tint = MaterialTheme.colorScheme.outline,
				contentDescription = null
			)

			Text(
				text = selectedDate?.formatAsShortDayOfWeekAndDate()
					?: stringResource(Res.string.label_evaluation_date),
				maxLines = 1,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		OutlinedButton(
			modifier = Modifier
				.offset(x = (-0.5).dp)
				.weight(0.5f),
			colors = if (committedDate == null) {
				ButtonDefaults.filledTonalButtonColors()
			} else {
				ButtonDefaults.outlinedButtonColors()
			},
			onClick = {
				onDateChange(null)
			},
			shape = RoundedCornerShape(
				topStartPercent = 0,
				topEndPercent = 50,
				bottomEndPercent = 50,
				bottomStartPercent = 0
			)
		) {
			Text(
				text = stringResource(Res.string.label_evaluation_no_date),
				maxLines = 1,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}
