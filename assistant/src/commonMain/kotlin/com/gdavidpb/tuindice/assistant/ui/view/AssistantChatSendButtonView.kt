package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_send
import tuindice.assistant.generated.resources.a11y_assistant_stop

// The composer's button in the accent: send, or stop while a turn can be stopped.
@Composable
fun AssistantChatSendButtonView(
	composer: ChatComposerItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val colors = IconButtonDefaults.filledIconButtonColors(
		containerColor = MaterialTheme.colorScheme.primary,
		contentColor = MaterialTheme.colorScheme.onPrimary
	)

	if (composer.isStoppable) {
		FilledIconButton(
			onClick = { onAction(AssistantChat.Action.CancelTurn) },
			colors = colors,
			modifier = modifier.testTag(AssistantUiTags.StopButton)
		) {
			Icon(
				imageVector = Icons.Outlined.Stop,
				contentDescription = stringResource(Res.string.a11y_assistant_stop)
			)
		}
	} else {
		FilledIconButton(
			onClick = { onAction(AssistantChat.Action.Send) },
			enabled = composer.isSendEnabled,
			colors = colors,
			modifier = modifier.testTag(AssistantUiTags.SendButton)
		) {
			Icon(
				imageVector = Icons.AutoMirrored.Outlined.Send,
				contentDescription = stringResource(Res.string.a11y_assistant_send)
			)
		}
	}
}
