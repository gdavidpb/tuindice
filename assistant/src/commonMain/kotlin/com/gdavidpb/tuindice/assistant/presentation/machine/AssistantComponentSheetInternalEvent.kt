package com.gdavidpb.tuindice.assistant.presentation.machine

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem

// Internal machine inputs of the component sheet: what the conversation store says about the component it shows.
sealed interface AssistantComponentSheetInternalEvent {
	// The component as the store holds it now, as its item. It is told again each time the store changes it.
	data class ComponentObserved(
		val messageId: String,
		val item: ChatComponentItem
	) : AssistantComponentSheetInternalEvent

	// The component is not in the conversation: it left it, or it never was.
	data object ComponentMissing : AssistantComponentSheetInternalEvent
}
