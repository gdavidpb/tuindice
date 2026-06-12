package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.create_term_hide_taken_subjects
import tuindice.record.generated.resources.create_term_show_taken_subjects

@Composable
fun AlreadyTakenSearchResultsToggle(
	count: Int,
	isExpanded: Boolean,
	onClick: () -> Unit
) {
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = 40.dp)
			.clickable(role = Role.Button, onClick = onClick)
			.testTag(RecordUiTags.CreateSyntheticTermTakenSubjectsToggle),
		shape = MaterialTheme.shapes.medium,
		color = MaterialTheme.colorScheme.surfaceContainerLow,
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(PaddingValues(horizontal = 12.dp, vertical = 8.dp)),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier.weight(1f),
				text = stringResource(
					if (isExpanded)
						Res.string.create_term_hide_taken_subjects
					else
						Res.string.create_term_show_taken_subjects,
					count
				),
				style = MaterialTheme.typography.labelLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			Icon(
				imageVector = if (isExpanded) {
					Icons.Outlined.KeyboardArrowUp
				} else {
					Icons.Outlined.KeyboardArrowDown
				},
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
