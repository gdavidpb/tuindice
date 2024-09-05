package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asIcon
import com.gdavidpb.tuindice.evaluations.presentation.mapper.asString

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvaluationTypePicker(
	selectedType: EvaluationType? = null,
	onTypeChange: (EvaluationType) -> Unit
) {
	val selectedTypeState = remember { mutableStateOf(selectedType) }

	FlowRow(
		modifier = Modifier
			.padding(top = dimensionResource(id = R.dimen.dp_8))
			.fillMaxWidth(),
		horizontalArrangement = Arrangement
			.spacedBy(
				space = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		EvaluationType.entries
			.forEach { type ->
				FilterChip(
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
