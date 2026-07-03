package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.evaluations.presentation.utils.*
import com.gdavidpb.tuindice.evaluations.presentation.mapper.formatAsShortDayOfWeekAndDate
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.evaluations.generated.resources.*

@Composable
fun EvaluationDatePicker(
	modifier: Modifier = Modifier,
	selectedScheduleMode: EvaluationScheduleMode,
	selectedDate: Long?,
	onDateChange: (date: Long?) -> Unit
) {
	val committedDate = selectedDate?.toEvaluationLocalDate()
	val isPickerDialogOpen = remember { mutableStateOf(false) }
	val draftSelectedDate = remember(selectedDate) { mutableStateOf(committedDate) }
	val displayedMonth = remember(selectedDate) {
		mutableStateOf((committedDate ?: currentEvaluationLocalDate()).monthStart())
	}
	val pickerContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
	val selectedPickerContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)

	fun resetDialogState() {
		draftSelectedDate.value = committedDate
		displayedMonth.value = (committedDate ?: currentEvaluationLocalDate()).monthStart()
	}

	if (isPickerDialogOpen.value) {
		AlertDialog(
			modifier = Modifier.exposeTestTagsAsResourceId(),
			onDismissRequest = {
				resetDialogState()
				isPickerDialogOpen.value = false
			},
			title = {
				Text(
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDateDialogTitle),
					text = stringResource(Res.string.label_add_evaluation_date)
				)
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
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDateDialogAcceptButton),
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
					modifier = Modifier.testTag(EvaluationsUiTags.EvaluationDateDialogCancelButton),
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

	Row(
		modifier = modifier
			.testTag(EvaluationsUiTags.EvaluationDatePicker)
	) {
		OutlinedButton(
			modifier = Modifier
				.testTag(EvaluationsUiTags.EvaluationDateSelectButton)
				.offset(x = 0.5.dp)
				.weight(0.5f)
				.defaultMinSize(minHeight = 56.dp),
			colors = ButtonDefaults.outlinedButtonColors(
				containerColor = if (selectedScheduleMode == EvaluationScheduleMode.DATED) {
					selectedPickerContainerColor
				} else {
					pickerContainerColor
				},
				contentColor = MaterialTheme.colorScheme.onSurface
			),
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
				tint = if (selectedDate != null)
					MaterialTheme.colorScheme.primary
				else
					MaterialTheme.colorScheme.outline,
				contentDescription = null
			)

			Text(
				modifier = Modifier.fillMaxWidth(),
				text = selectedDate?.formatAsShortDayOfWeekAndDate()
					?: stringResource(Res.string.label_evaluation_date),
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.onSurface
			)
		}

		OutlinedButton(
			modifier = Modifier
				.testTag(EvaluationsUiTags.EvaluationDateNoDateButton)
				.offset(x = (-0.5).dp)
				.weight(0.5f)
				.defaultMinSize(minHeight = 56.dp),
			colors = ButtonDefaults.outlinedButtonColors(
				containerColor = if (selectedScheduleMode == EvaluationScheduleMode.CONTINUOUS) {
					selectedPickerContainerColor
				} else {
					pickerContainerColor
				},
				contentColor = MaterialTheme.colorScheme.onSurface
			),
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
				modifier = Modifier.fillMaxWidth(),
				text = stringResource(Res.string.label_evaluation_no_date),
				maxLines = 2,
				overflow = TextOverflow.Ellipsis,
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}
