package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

@OptIn(
	ExperimentalLayoutApi::class,
	ExperimentalMaterial3Api::class
)
@Composable
fun FilterView(
	modifier: Modifier = Modifier,
	entries: Map<EvaluationFilter, MutableState<Boolean>>,
	onEntrySelect: (filter: EvaluationFilter, isSelected: Boolean) -> Unit
) {
	FlowRow(
		modifier = modifier
			.fillMaxWidth(),
		horizontalArrangement = Arrangement
			.spacedBy(
				space = dimensionResource(id = R.dimen.dp_8)
			)
	) {
		entries.forEach { (filter, isSelected) ->
			FilterChip(
				selected = isSelected.value,
				onClick = {
					onEntrySelect(filter, !isSelected.value)
				},
				leadingIcon = {
					AnimatedContent(
						targetState = isSelected.value,
						label = "FilterChipAnimatedContent"
					) { targetState ->
						if (targetState)
							Icon(
								imageVector = Icons.Outlined.Check,
								contentDescription = null
							)
					}
				},
				label = {
					Text(
						text = filter.getLabel(),
						maxLines = 1
					)
				}
			)
		}
	}
}