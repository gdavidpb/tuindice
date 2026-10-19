package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes

@Composable
internal fun ComposerCircleButton(
	icon: ImageVector,
	description: String,
	isOn: Boolean,
	onClick: () -> Unit,
	tag: String
) {
	val circle = composerTone(
		if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = OFF_CIRCLE_ALPHA)
	)
	val arrow = composerTone(
		if (isOn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = OFF_ARROW_ALPHA)
	)

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

// The tone the button is drawn in: it eases to [target] in 150 ms when the button turns on or off, and with animations
// off it is [target] at once.
@Composable
private fun composerTone(target: Color): Color =
	if (LocalTuIndiceAnimationsEnabled.current) {
		animateColorAsState(
			targetValue = target,
			animationSpec = tween(AssistantChatMotion.ExitMillis),
			label = "AssistantComposerTone"
		).value
	} else {
		target
	}
