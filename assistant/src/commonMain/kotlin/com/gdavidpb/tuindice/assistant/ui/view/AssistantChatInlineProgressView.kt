package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_sending
import tuindice.assistant.generated.resources.assistant_photo_reading

// A turn's status belongs with its student's message, rather than beside the composer.
@Composable
fun AssistantChatInlineProgressView(label: String?) {
	val shownLabel = rememberLastNonNull(label)

	AssistantChatSlotView(visible = label != null) {
		shownLabel?.let { AssistantProgressView(label = it) }
	}
}

// The view supplies a label while a photo is read or a send has not yet received its first progress event.
@Composable
internal fun assistantProgressLabel(content: AssistantChat.State.Content): String? {
	val reading = if (content.isExtracting) stringResource(Res.string.assistant_photo_reading) else null
	val waiting = content.isSending || content.composer.isStoppable
	val sending = if (waiting) stringResource(Res.string.assistant_chat_sending) else null
	return content.progressLabel ?: reading ?: sending
}

// A text turn follows the last question; photo reading has no question, so it follows the latest conversation item.
// A send can start just before its optimistic question is stored: until then, keep the status at the end.
internal fun assistantProgressAnchor(content: AssistantChat.State.Content): String? {
	val last = content.items.lastOrNull()
	if (content.isExtracting || (content.isSending && last is ChatItem.Agent)) return last?.id
	return content.items.lastOrNull { it is ChatItem.User }?.id ?: last?.id
}
