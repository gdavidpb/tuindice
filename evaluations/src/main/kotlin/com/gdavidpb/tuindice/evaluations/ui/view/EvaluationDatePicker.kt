package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsDayOfWeekAndDate
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationDatePicker(
	modifier: Modifier = Modifier,
	selectedDate: Date,
	onDateChanged: (date: Date) -> Unit,
	error: String? = null
) {
	val isPickerDialogOpen = remember {
		mutableStateOf(false)
	}

	val isDateSelected = remember {
		mutableStateOf(selectedDate.time != 0L)
	}

	val supportingText = remember {
		mutableStateOf(error)
	}

	val datePickerState = rememberDatePickerState(
		initialSelectedDateMillis = if (selectedDate.time != 0L)
			selectedDate.time
		else
			Date().time
	)

	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()

	LaunchedEffect(isPressed) {
		if (isPressed) isPickerDialogOpen.value = true
	}

	if (isPickerDialogOpen.value) {
		val isConfirmEnabled = remember {
			derivedStateOf { datePickerState.selectedDateMillis != null }
		}

		DatePickerDialog(
			onDismissRequest = {
				isPickerDialogOpen.value = false
			},
			confirmButton = {
				TextButton(
					onClick = {
						isDateSelected.value = true
						isPickerDialogOpen.value = false

						val date = datePickerState.selectedDateMillis
							?.let(::Date)

						if (date != null) onDateChanged(date)
					},
					enabled = isConfirmEnabled.value
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

	val selectedDateText =
		(if (isDateSelected.value) datePickerState.selectedDateMillis else 0L)
			?.let(::Date)
			?.formatAsDayOfWeekAndDate()
			.orEmpty()

	OutlinedTextField(
		modifier = modifier
			.fillMaxWidth(),
		interactionSource = interactionSource,
		value = selectedDateText,
		onValueChange = {},
		isError = supportingText.value != null,
		supportingText = {
			val text = supportingText.value

			if (text != null) Text(text)
		},
		leadingIcon = {
			Icon(
				imageVector = Icons.Outlined.CalendarMonth,
				contentDescription = null
			)
		},
		trailingIcon = {
			Switch(
				modifier = Modifier
					.padding(end = dimensionResource(id = R.dimen.dp_8)),
				checked = isDateSelected.value,
				onCheckedChange = { isChecked ->
					if (isChecked)
						isPickerDialogOpen.value = true
					else
						isDateSelected.value = false
				}
			)
		},
		readOnly = true,
		singleLine = true
	)
}