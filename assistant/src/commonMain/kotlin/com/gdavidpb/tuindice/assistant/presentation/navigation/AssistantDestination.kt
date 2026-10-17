package com.gdavidpb.tuindice.assistant.presentation.navigation

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.navigation.DialogDestination
import kotlinx.serialization.Serializable

@Serializable
sealed class AssistantDestination : Destination() {
	// The chat sheet, opened from a screen with what that screen declared, or null.
	@Serializable
	data class Chat(val context: AssistantScreenContext?) : AssistantDestination(), DialogDestination

	// One component of a message, on its own sheet.
	@Serializable
	data class ComponentSheet(
		val messageId: String,
		val componentId: String
	) : AssistantDestination(), DialogDestination

	// What the assistant remembers about the student.
	@Serializable
	data object Memory : AssistantDestination()

	@Serializable
	data object ClearMemoryConfirmationDialog : AssistantDestination(), DialogDestination
}
