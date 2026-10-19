package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes

@Composable
internal fun ComposerCircleButton(
	icon: ImageVector,
	description: String,
	isOn: Boolean,
	onClick: () -> Unit,
	tag: String
) {
	val circle = if (isOn) {
		MaterialTheme.colorScheme.primary
	} else {
		MaterialTheme.colorScheme.onSurface.copy(alpha = OFF_CIRCLE_ALPHA)
	}
	val arrow = if (isOn) {
		MaterialTheme.colorScheme.onPrimary
	} else {
		MaterialTheme.colorScheme.onSurface.copy(alpha = OFF_ARROW_ALPHA)
	}

	IconButton(
		onClick = onClick,
		enabled = isOn,
		modifier = Modifier.testTag(tag)
	) {
		Box(
			modifier = Modifier.size(SEND_CIRCLE_SIZE).background(circle, CircleShape),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = icon,
				contentDescription = description,
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				tint = arrow
			)
		}
	}
}
