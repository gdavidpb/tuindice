package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// A component this build does not know: its message shows the text the backend sent with it.
@Immutable
data class ChatFallbackItem(
	override val id: String,
	override val opensSheet: Boolean,
	val fallbackText: String
) : ChatComponentItem
