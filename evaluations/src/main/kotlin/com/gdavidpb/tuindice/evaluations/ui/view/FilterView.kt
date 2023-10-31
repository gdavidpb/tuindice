package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter

@OptIn(
	ExperimentalMaterial3Api::class
)
@Composable
fun FilterView(
	modifier: Modifier = Modifier,
	entries: Map<EvaluationFilter, Boolean>,
	onCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit
) {
	LazyRow(
		modifier = modifier
			.fillMaxWidth(),
		horizontalArrangement = Arrangement
			.spacedBy(
				space = dimensionResource(id = R.dimen.dp_6)
			)
	) {
		items(
			items = entries.toList(),
			key = { entry -> entry.first.getLabel() }
		) { (filter, isChecked) ->
			FilterChip(
				selected = isChecked,
				onClick = {
					onCheckedChange(filter, !isChecked)
				},
				leadingIcon = {
					AnimatedContent(
						targetState = isChecked,
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