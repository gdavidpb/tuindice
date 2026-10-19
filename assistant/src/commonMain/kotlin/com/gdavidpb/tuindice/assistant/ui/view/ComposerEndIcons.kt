package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem

// The icons at the end of the field, in one row: the photo clip, when the field is on, then the send button. A button
// added later, such as the microphone, goes in this row, next to the others.
@Composable
internal fun ComposerEndIcons(
	composer: ChatComposerItem,
	isCameraAvailable: Boolean,
	onAction: (AssistantChat.Action) -> Unit
) {
	Row(verticalAlignment = Alignment.Bottom) {
		if (composer.isEnabled) {
			AssistantChatPhotoButtonView(
				isEnabled = composer.isPhotoEnabled,
				isCameraAvailable = isCameraAvailable,
				onAction = onAction
			)
		}
		AssistantChatSendButtonView(composer = composer, onAction = onAction)
	}
}
