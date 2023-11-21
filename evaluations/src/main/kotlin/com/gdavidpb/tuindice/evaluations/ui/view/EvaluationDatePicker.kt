package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.mapper.toLocalTimeZone
import com.gdavidpb.tuindice.base.presentation.mapper.toUTCTimeZone
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsShortDayOfWeekAndDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationDatePicker(
	modifier: Modifier = Modifier,
	selectedDate: Long?,
	onDateChange: (date: Long?) -> Unit
) {
	val datePickerState = rememberDatePickerState(
		initialSelectedDateMillis = selectedDate?.toUTCTimeZone()
	)

	val isPickerDialogOpen = remember {
		mutableStateOf(false)
	}

	val isDateSelected = remember {
		derivedStateOf {
			datePickerState.selectedDateMillis != null
		}
	}

	if (isPickerDialogOpen.value) {
		DatePickerDialog(
			onDismissRequest = {
				isPickerDialogOpen.value = false

				datePickerState.setSelection(selectedDate?.toUTCTimeZone())
			},
			confirmButton = {
				TextButton(
					onClick = {
						isPickerDialogOpen.value = false

						val date = datePickerState
							.selectedDateMillis
							?.toLocalTimeZone()

						onDateChange(date)
					},
					enabled = isDateSelected.value
				) {
					Text(
						text = stringResource(id = R.string.accept)
					)
				}
			},
			dismissButton = {
				TextButton(
					onClick = {
						isPickerDialogOpen.value = false

						datePickerState.setSelection(selectedDate?.toUTCTimeZone())
					}
				) {
					Text(
						text = stringResource(id = R.string.cancel)
					)
				}
			}
		) {
			DatePicker(
				state = datePickerState,
				showModeToggle = false
			)
		}
	}

	Row(
		modifier = modifier
	) {
		OutlinedButton(
			modifier = Modifier
				.offset(x = (.5f).dp)
				.weight(.5f),
			colors = if (isDateSelected.value)
				ButtonDefaults.filledTonalButtonColors()
			else
				ButtonDefaults.outlinedButtonColors(),
			onClick = {
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
					.offset(x = -dimensionResource(id = R.dimen.dp_8)),
				imageVector = Icons.Outlined.Event,
				tint = MaterialTheme.colorScheme.outline,
				contentDescription = null
			)

			Text(
				text = if (isDateSelected.value)
					datePickerState.selectedDateMillis
						?.toLocalTimeZone()
						?.formatAsShortDayOfWeekAndDate()
						?: stringResource(id = R.string.label_evaluation_date)
				else
					stringResource(id = R.string.label_evaluation_date),
				maxLines = 1,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		OutlinedButton(
			modifier = Modifier
				.offset(x = (-.5f).dp)
				.weight(.5f),
			colors = if (!isDateSelected.value)
				ButtonDefaults.filledTonalButtonColors()
			else
				ButtonDefaults.outlinedButtonColors(),
			onClick = {
				datePickerState.setSelection(null)
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
				text = stringResource(id = R.string.label_evaluation_no_date),
				maxLines = 1,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}