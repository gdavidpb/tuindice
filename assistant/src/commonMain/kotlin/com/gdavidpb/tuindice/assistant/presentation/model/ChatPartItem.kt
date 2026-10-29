package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// A piece of an agent message, in display order.
@Immutable
sealed interface ChatPartItem {
	@Immutable
	data class Text(val text: String) : ChatPartItem

	// Drawn by its item's view; a component this build cannot render shows its fallback text.
	@Immutable
	data class Component(val item: ChatComponentItem) : ChatPartItem

	@Immutable
	data class IntentCard(val card: IntentCardItem) : ChatPartItem
}
