package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComposerItem

// The icons at the end of the field, in one row: the send button, or stop. The photo clip is not here; it is at the
// start of the field, where the text begins (ComposerStartIcon). A button added later, such as the microphone, goes
// in this row, next to the others.
@Composable
internal fun ComposerEndIcons(
	composer: ChatComposerItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	Row(verticalAlignment = Alignment.Bottom) {
		AssistantChatSendButtonView(composer = composer, onAction = onAction)
	}
}
