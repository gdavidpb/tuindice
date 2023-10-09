package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationDatePicker(
	modifier: Modifier = Modifier,
	selectedDate: Long?,
	onDateChange: (date: Long) -> Unit
) {
	val isDateSet = remember {
		mutableStateOf(true)
	}

	Row(
		modifier = modifier
			.fillMaxWidth()
	) {
		OutlinedButton(
			modifier = Modifier
				.offset(x = (.5f).dp)
				.weight(.5f),
			colors = if (isDateSet.value)
				ButtonDefaults.filledTonalButtonColors()
			else
				ButtonDefaults.outlinedButtonColors(),
			onClick = {
				isDateSet.value = true
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
					.padding(end = 8.dp),
				imageVector = Icons.Outlined.CalendarToday,
				tint = MaterialTheme.colorScheme.onPrimaryContainer,
				contentDescription = ""
			)

			Text(
				text = if (isDateSet.value)
					"Mié ─ 31/08/23"
				else
					stringResource(id = R.string.label_evaluation_date),
				maxLines = 1,
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}

		OutlinedButton(
			modifier = Modifier
				.offset(x = (-.5f).dp)
				.weight(.5f),
			colors = if (!isDateSet.value)
				ButtonDefaults.filledTonalButtonColors()
			else
				ButtonDefaults.outlinedButtonColors(),
			onClick = {
				isDateSet.value = false
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
				color = MaterialTheme.colorScheme.onPrimaryContainer
			)
		}
	}
}