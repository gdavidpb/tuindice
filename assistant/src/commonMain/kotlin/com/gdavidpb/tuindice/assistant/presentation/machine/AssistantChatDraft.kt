package com.gdavidpb.tuindice.assistant.presentation.machine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The text the student is writing: the only input register of the chat. It is written only from the machine's rows and
 * commands, so every change is an input symbol of the table; the view model never reads or writes it.
 *
 * [setText] is the student's own edit. [replace] is the chat's: the send clears the draft and a refused send gives the
 * text back. Each replacement moves [revision], so the field adopts that text even when the student typed it before
 * (EditableTextFieldState ignores a text it reported, which is how it tells an echo from a change).
 */
class AssistantChatDraft {
	private val mutableText = MutableStateFlow("")

	val text: StateFlow<String> = mutableText.asStateFlow()

	var revision: Int = 0
		private set

	fun setText(text: String) {
		mutableText.value = text
	}

	fun replace(text: String) {
		revision++
		mutableText.value = text
	}
}
