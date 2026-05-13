package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction

@Composable
internal fun CreateTermSubjectActionButton(
	action: CreateTermSubjectCardAction,
	enabled: Boolean,
	onClick: () -> Unit
) {
	Surface(
		shape = RoundedCornerShape(10.dp),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
		)
	) {
		IconButton(
			modifier = Modifier.size(38.dp),
			enabled = enabled,
			onClick = onClick
		) {
			Icon(
				imageVector = when (action) {
					CreateTermSubjectCardAction.Add -> Icons.Outlined.Add
					CreateTermSubjectCardAction.Remove -> Icons.Outlined.DeleteOutline
				},
				contentDescription = null,
				tint = if (enabled)
					MaterialTheme.colorScheme.onSurface
				else
					MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
			)
		}
	}
}
