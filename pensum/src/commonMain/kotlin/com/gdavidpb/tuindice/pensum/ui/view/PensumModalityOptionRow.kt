package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem

@Composable
fun PensumModalityOptionRow(
	modifier: Modifier = Modifier,
	modality: PensumModalityItem,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	val graphColors = pensumGraphColors()
	val selectedBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
	val selectedBorder = MaterialTheme.colorScheme.primary

	Surface(
		modifier = modifier
			.fillMaxWidth()
			.selectable(
				selected = isSelected,
				role = Role.RadioButton,
				onClick = onClick
		),
		shape = PensumElementShape,
		color = if (isSelected) selectedBackground else graphColors.panelBackground,
		border = BorderStroke(
			width = 1.dp,
			color = if (isSelected) selectedBorder else MaterialTheme.colorScheme.outlineVariant
		)
	) {
		Row(
			modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			Text(
				modifier = Modifier.weight(1f),
				text = modality.name,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
			if (isSelected) {
				Icon(
					modifier = Modifier.size(20.dp),
					imageVector = Icons.Filled.Check,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}
