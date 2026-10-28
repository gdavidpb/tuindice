package com.gdavidpb.tuindice.assistant.presentation.resource

// The texts of the parts of an answer that cannot be shown in full. Its own port keeps the chat texts small.
interface AssistantChatPartTexts {
	// The notice that a part of an answer could not be shown: it carries a value this build does not know.
	suspend fun partNotShown(): String
}
