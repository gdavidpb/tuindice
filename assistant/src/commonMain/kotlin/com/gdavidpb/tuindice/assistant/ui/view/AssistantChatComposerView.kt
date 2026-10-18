package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import tuindice.assistant.generated.resources.assistant_chat_composer_placeholder

private const val COMPOSER_MAX_LINES = 4

// The text field, with the photo clip inside it at its end, and the send button (AssistantChatSendButtonView). The
// field is off while a restriction holds, and with it the clip; the buttons follow what the presentation says they can
// do. While a turn can be stopped, the stop button takes the place of the send button.
//
// The send button sits in a box as tall as a one-line field, at the bottom of the row: with one line it is centred on
// the field, and when the field grows it stays by the last line, where the text is being written, instead of moving
// to the middle of the field at every new line.
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
		OutlinedTextField(
			value = composer.text,
			onValueChange = { text -> onAction(AssistantChat.Action.ChangeText(text)) },
			modifier = Modifier.weight(1f).testTag(AssistantUiTags.Composer),
			enabled = composer.isEnabled,
			placeholder = { Text(text = stringResource(Res.string.assistant_chat_composer_placeholder)) },
			// The clip is not there while the field is off, so a restriction hides it together with the field.
			trailingIcon = if (composer.isEnabled) {
				{
					AssistantChatPhotoButtonView(
						isEnabled = composer.isPhotoEnabled,
						isCameraAvailable = isCameraAvailable,
						onAction = onAction
					)
				}
			} else {
				null
			},
			shape = RoundedCornerShape(TuIndiceRadius.Large),
			maxLines = COMPOSER_MAX_LINES
		)
		Box(
			modifier = Modifier.height(OutlinedTextFieldDefaults.MinHeight),
			contentAlignment = Alignment.Center
		) {
			AssistantChatSendButtonView(composer = composer, onAction = onAction)
		}
	}
}
