package com.gdavidpb.tuindice.assistant.presentation.model

// A component this build does not know: its message shows the text the backend sent with it.
data class ChatFallbackItem(
	override val id: String,
	override val opensSheet: Boolean,
	val fallbackText: String
) : ChatComponentItem
