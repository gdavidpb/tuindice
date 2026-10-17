package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_send
import tuindice.assistant.generated.resources.assistant_chat_composer_placeholder

private const val COMPOSER_MAX_LINES = 4

// The photo button, the text field and the send button in the accent. The field is off while a restriction holds, and
// with it the photo button; the buttons follow what the presentation says they can do.
@Composable
fun AssistantChatComposerView(
	composer: ChatComposerItem,
	isCameraAvailable: Boolean,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier.fillMaxWidth(),
		verticalAlignment = Alignment.Bottom,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		// The photo button is not there while the field is off, so a restriction hides it together with the field.
		if (composer.isEnabled) {
			AssistantChatPhotoButtonView(
				isEnabled = composer.isPhotoEnabled,
				isCameraAvailable = isCameraAvailable,
				onAction = onAction
			)
		}
		OutlinedTextField(
			value = composer.text,
			onValueChange = { text -> onAction(AssistantChat.Action.ChangeText(text)) },
			modifier = Modifier.weight(1f).testTag(AssistantUiTags.Composer),
			enabled = composer.isEnabled,
			placeholder = { Text(text = stringResource(Res.string.assistant_chat_composer_placeholder)) },
			shape = RoundedCornerShape(TuIndiceRadius.Large),
			maxLines = COMPOSER_MAX_LINES
		)
		FilledIconButton(
			onClick = { onAction(AssistantChat.Action.Send) },
			enabled = composer.isSendEnabled,
			colors = IconButtonDefaults.filledIconButtonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.onPrimary
			),
			modifier = Modifier.testTag(AssistantUiTags.SendButton)
		) {
			Icon(
				imageVector = Icons.AutoMirrored.Outlined.Send,
				contentDescription = stringResource(Res.string.a11y_assistant_send)
			)
		}
	}
}
