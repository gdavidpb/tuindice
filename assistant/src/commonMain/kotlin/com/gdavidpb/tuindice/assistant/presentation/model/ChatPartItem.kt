package com.gdavidpb.tuindice.assistant.presentation.model


// A piece of an agent message, in display order.
sealed interface ChatPartItem {
	data class Text(val text: String) : ChatPartItem

	// Drawn by its item's view; a component this build cannot render shows its fallback text.
	data class Component(val item: ChatComponentItem) : ChatPartItem

	data class IntentCard(val card: IntentCardItem) : ChatPartItem
}
