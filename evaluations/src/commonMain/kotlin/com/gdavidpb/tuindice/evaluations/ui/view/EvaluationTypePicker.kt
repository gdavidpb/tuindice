package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asString
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationTypePicker(
	selectedType: EvaluationType? = null,
	onTypeChange: (EvaluationType) -> Unit
) {
	val selectedTypeState = remember(selectedType) {
		mutableStateOf(selectedType)
	}

	FlowRow(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationTypePickerRow)
			.padding(top = 8.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		EvaluationType.entries.forEach { type ->
			FilterChip(
				modifier = Modifier.testTag(EvaluationsUiTags.evaluationTypeChip(type.name)),
				selected = (type == selectedTypeState.value),
				onClick = {
					selectedTypeState.value = type
					onTypeChange(type)
				},
				leadingIcon = {
					Icon(
						imageVector = type.asIcon(),
						tint = MaterialTheme.colorScheme.outline,
						contentDescription = null
					)
				},
				label = {
					Text(
						text = type.asString(),
						maxLines = 1
					)
				}
			)
		}
	}
}
