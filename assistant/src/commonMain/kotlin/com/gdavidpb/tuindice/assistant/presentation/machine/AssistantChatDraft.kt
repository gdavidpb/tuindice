package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.domain.model.AssistantAttachment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * What the student is writing: the text and the attachments of the message being written. They are the only input
 * registers of the chat. They are written only from the machine's rows and commands, so every change is an input symbol
 * of the table; the view model never reads or writes them. The attachments live as long as the text does.
 *
 * [setText] is the student's own edit. [replace] is the chat's: the send clears the draft and a refused send gives the
 * text back. Each replacement moves [revision], so the field adopts that text even when the student typed it before
 * (EditableTextFieldState ignores a text it reported, which is how it tells an echo from a change).
 */
class AssistantChatDraft {
	private val mutableText = MutableStateFlow("")
	private val mutableAttachments = MutableStateFlow<List<AssistantAttachment>>(emptyList())

	val text: StateFlow<String> = mutableText.asStateFlow()

	// The attachments of the message being written, in the order they were added.
	val attachments: StateFlow<List<AssistantAttachment>> = mutableAttachments.asStateFlow()

	var revision: Int = 0
		private set

	fun setText(text: String) {
		mutableText.value = text
	}

	fun replace(text: String) {
		revision++
		mutableText.value = text
	}

	fun setAttachments(attachments: List<AssistantAttachment>) {
		mutableAttachments.value = attachments
	}

	// Changes the attachment [localId] with [change]. An attachment the draft does not hold is left as it is.
	fun updateAttachment(localId: String, change: (AssistantAttachment) -> AssistantAttachment) {
		mutableAttachments.value = mutableAttachments.value.map { attachment ->
			if (attachment.localId == localId) change(attachment) else attachment
		}
	}

	fun removeAttachment(localId: String) {
		mutableAttachments.value = mutableAttachments.value.filterNot { attachment -> attachment.localId == localId }
	}
}
