package com.gdavidpb.tuindice.assistant.presentation.resource

// The texts of the progress the chat shows while it waits. Its own port keeps the chat texts small.
interface AssistantChatProgressTexts {
	// The label of the progress while a photo of the plan is read.
	suspend fun photoReading(): String

	// The label of the progress while a message is sent and its first event has not come yet.
	suspend fun sending(): String
}
