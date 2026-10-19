package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_send
import tuindice.assistant.generated.resources.a11y_assistant_stop

// The circle of the send button. Its touch area is the 48 dp of an icon button around it.
private val SEND_CIRCLE_SIZE: Dp = 38.dp

// The composer's button, inside the field at its end: send, or stop while a turn can be stopped. On, it is a circle
// in the accent with the arrow in its on-colour; off, a circle in the neutral tone of the disabled controls, with the
// arrow faded. Both tones are the theme's.
@Composable
fun AssistantChatSendButtonView(
	composer: ChatComposerItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	if (composer.isStoppable) {
		ComposerCircleButton(
			icon = Icons.Outlined.Stop,
			description = stringResource(Res.string.a11y_assistant_stop),
			isOn = true,
			onClick = { onAction(AssistantChat.Action.CancelTurn) },
			tag = AssistantUiTags.StopButton
		)
	} else {
		ComposerCircleButton(
			icon = Icons.AutoMirrored.Outlined.Send,
			description = stringResource(Res.string.a11y_assistant_send),
			isOn = composer.isSendEnabled,
			onClick = { onAction(AssistantChat.Action.Send) },
			tag = AssistantUiTags.SendButton
		)
	}
}

@Composable
private fun ComposerCircleButton(
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

// The disabled tones of the Material controls: a faded surface and a faded content colour.
private const val OFF_CIRCLE_ALPHA = 0.12f
private const val OFF_ARROW_ALPHA = 0.38f
