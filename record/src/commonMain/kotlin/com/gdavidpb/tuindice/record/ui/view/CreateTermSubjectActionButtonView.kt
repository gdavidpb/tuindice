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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.record.ui.model.CreateTermSubjectCardAction
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.a11y_add_subject
import tuindice.record.generated.resources.a11y_remove_subject

@Composable
fun CreateTermSubjectActionButton(
	action: CreateTermSubjectCardAction,
	enabled: Boolean,
	onClick: () -> Unit,
	testTag: String? = null
) {
	val buttonModifier = if (testTag == null) {
		Modifier.size(38.dp)
	} else {
		Modifier
			.size(38.dp)
			.testTag(testTag)
	}

	Surface(
		shape = RoundedCornerShape(TuIndiceRadius.Medium),
		color = MaterialTheme.colorScheme.surface.copy(alpha = 0.48f),
		border = BorderStroke(
			width = 1.dp,
			color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = TuIndiceAlpha.Muted)
		)
	) {
		IconButton(
			modifier = buttonModifier,
			enabled = enabled,
			onClick = onClick
		) {
			Icon(
				imageVector = when (action) {
					CreateTermSubjectCardAction.Add -> Icons.Outlined.Add
					CreateTermSubjectCardAction.Remove -> Icons.Outlined.DeleteOutline
				},
				contentDescription = when (action) {
					CreateTermSubjectCardAction.Add -> stringResource(Res.string.a11y_add_subject)
					CreateTermSubjectCardAction.Remove -> stringResource(Res.string.a11y_remove_subject)
				},
				tint = if (enabled)
					MaterialTheme.colorScheme.onSurface
				else
					MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
			)
		}
	}
}
