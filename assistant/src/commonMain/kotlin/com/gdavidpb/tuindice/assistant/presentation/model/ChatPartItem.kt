package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent

// A piece of an agent message, in display order.
sealed interface ChatPartItem {
	data class Text(val text: String) : ChatPartItem

	// Rendered by its catalog's renderer; a component this build cannot render shows its fallbackText.
	data class Component(val component: ChatComponent) : ChatPartItem

	data class IntentCard(val card: IntentCardItem) : ChatPartItem
}
