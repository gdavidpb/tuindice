package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.evaluations.R

@OptIn(
	ExperimentalLayoutApi::class,
	ExperimentalMaterial3Api::class,
	ExperimentalAnimationApi::class
)
@Composable
fun FilterView(
	name: String,
	icon: ImageVector,
	entries: Map<String, MutableState<Boolean>>,
	onEntrySelectChange: (key: String, isSelected: Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
	) {
		Icon(
			modifier = Modifier
				.align(Alignment.CenterVertically)
				.size(dimensionResource(id = R.dimen.dp_24)),
			imageVector = icon,
			tint = MaterialTheme.colorScheme.outline,
			contentDescription = null
		)

		Text(
			modifier = Modifier
				.padding(start = dimensionResource(id = R.dimen.dp_8))
				.align(Alignment.CenterVertically),
			text = name,
			color = MaterialTheme.colorScheme.outline,
			style = MaterialTheme.typography.titleSmall
		)
	}

	FlowRow(
		modifier = Modifier
			.padding(top = dimensionResource(id = R.dimen.dp_8))
			.fillMaxWidth(),
		horizontalArrangement = Arrangement
			.spacedBy(
				space = dimensionResource(id = R.dimen.dp_8)
			),
		verticalAlignment = Alignment.CenterVertically
	) {
		entries.forEach { (key, isSelected) ->
			FilterChip(
				selected = isSelected.value,
				onClick = {
					onEntrySelectChange(key, !isSelected.value)
				},
				leadingIcon = {
					AnimatedContent(targetState = isSelected.value) { targetState ->
						if (targetState)
							Icon(
								imageVector = Icons.Outlined.Check,
								contentDescription = null
							)
					}
				},
				label = {
					Text(
						text = key,
						maxLines = 1
					)
				}
			)
		}
	}
}