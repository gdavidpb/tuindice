package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_send
import tuindice.assistant.generated.resources.a11y_assistant_stop

// The circle of the send button. Its touch area is the 48 dp of an icon button around it.
internal val SEND_CIRCLE_SIZE: Dp = 38.dp

// The composer's button, inside the field at its end: send, or stop while a turn can be stopped. On, it is a circle
// in the accent with the arrow in its on-colour; off, a circle in the neutral tone of the disabled controls, with the
// arrow faded. Both tones are the theme's. Send and stop swap with a 150 ms crossfade, and the tones ease.
@Composable
fun AssistantChatSendButtonView(
	composer: ChatComposerItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	if (LocalTuIndiceAnimationsEnabled.current) {
		Crossfade(
			targetState = composer.isStoppable,
			animationSpec = tween(AssistantChatMotion.ExitMillis),
			label = "AssistantSendStop"
		) { isStoppable ->
			SendOrStopButton(isStoppable = isStoppable, composer = composer, onAction = onAction)
		}
	} else {
		SendOrStopButton(isStoppable = composer.isStoppable, composer = composer, onAction = onAction)
	}
}

@Composable
private fun SendOrStopButton(
	isStoppable: Boolean,
	composer: ChatComposerItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	if (isStoppable) {
		ComposerCircleButton(
			icon = Icons.Outlined.Stop,
			description = stringResource(Res.string.a11y_assistant_stop),
			isOn = true,
			onClick = { onAction(AssistantChat.Action.CancelTurn) },
			tag = AssistantUiTags.StopButton
		)
	} else {
		ComposerCircleButton(
			icon = Icons.Rounded.ArrowUpward,
			description = stringResource(Res.string.a11y_assistant_send),
			isOn = composer.isSendEnabled,
			onClick = { onAction(AssistantChat.Action.Send) },
			tag = AssistantUiTags.SendButton
		)
	}
}

// The disabled tones of the Material controls: a faded surface and a faded content colour.
internal const val OFF_CIRCLE_ALPHA = 0.12f
internal const val OFF_ARROW_ALPHA = 0.38f
