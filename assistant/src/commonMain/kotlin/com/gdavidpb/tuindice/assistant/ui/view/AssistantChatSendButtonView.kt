package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
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
			icon = StopSquare,
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

// Stop: a filled square with softened corners. Material's stop square takes half of its box and read small next to the
// send arrow, so this one takes two thirds of the same 20 dp box: about 13 dp, as tall as the arrow.
private const val STOP_VIEWPORT = 24f
private const val STOP_EDGE_LOW = 4f
private const val STOP_EDGE_HIGH = 20f
private const val STOP_CORNER = 2f

private val StopSquare: ImageVector = ImageVector.Builder(
	name = "AssistantStopSquare",
	defaultWidth = STOP_VIEWPORT.dp,
	defaultHeight = STOP_VIEWPORT.dp,
	viewportWidth = STOP_VIEWPORT,
	viewportHeight = STOP_VIEWPORT
).apply {
	path(fill = SolidColor(Color.Black)) {
		moveTo(STOP_EDGE_LOW + STOP_CORNER, STOP_EDGE_LOW)
		horizontalLineTo(STOP_EDGE_HIGH - STOP_CORNER)
		arcToRelative(STOP_CORNER, STOP_CORNER, 0f, isMoreThanHalf = false, isPositiveArc = true, STOP_CORNER, STOP_CORNER)
		verticalLineTo(STOP_EDGE_HIGH - STOP_CORNER)
		arcToRelative(STOP_CORNER, STOP_CORNER, 0f, isMoreThanHalf = false, isPositiveArc = true, -STOP_CORNER, STOP_CORNER)
		horizontalLineTo(STOP_EDGE_LOW + STOP_CORNER)
		arcToRelative(STOP_CORNER, STOP_CORNER, 0f, isMoreThanHalf = false, isPositiveArc = true, -STOP_CORNER, -STOP_CORNER)
		verticalLineTo(STOP_EDGE_LOW + STOP_CORNER)
		arcToRelative(STOP_CORNER, STOP_CORNER, 0f, isMoreThanHalf = false, isPositiveArc = true, STOP_CORNER, -STOP_CORNER)
		close()
	}
}.build()

// The off tones of the send button: the accent and its content colour, faded (owner's choice over Material's grey).
internal const val OFF_CIRCLE_ALPHA = 0.35f
internal const val OFF_ARROW_ALPHA = 0.55f
