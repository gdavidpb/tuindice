package com.gdavidpb.tuindice.assistant.presentation.machine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The text the student is writing: the only input register of the chat. It is written only from the machine's rows and
 * commands, so every change is an input symbol of the table; the view model never reads or writes it.
 */
class AssistantChatDraft {
	private val mutableText = MutableStateFlow("")

	val text: StateFlow<String> = mutableText.asStateFlow()

	fun setText(text: String) {
		mutableText.value = text
	}
}
