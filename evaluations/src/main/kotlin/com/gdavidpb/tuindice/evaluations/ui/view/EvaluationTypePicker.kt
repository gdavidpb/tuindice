package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.presentation.mapper.iconRes
import com.gdavidpb.tuindice.evaluations.presentation.mapper.stringRes

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EvaluationTypePicker(
	selectedType: EvaluationType? = null,
	onTypeChanged: (EvaluationType) -> Unit
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
		EvaluationType.values()
			.forEach { type ->
				FilterChip(
					selected = (type == selectedTypeState.value),
					onClick = {
						selectedTypeState.value = type
						onTypeChanged(type)
					},
					leadingIcon = {
						Icon(
							imageVector = type.iconRes(),
							tint = MaterialTheme.colorScheme.outline,
							contentDescription = null
						)
					},
					label = {
						Text(
							text = stringResource(id = type.stringRes()),
							maxLines = 1
						)
					}
				)
			}
	}
}
