package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay

// One component of an agent message, as its view draws it. The mapper builds it from the wire component, so the view
// only paints what the item holds.
sealed interface ChatComponentItem {
	val id: String

	// The component opens on its own sheet: its message shows the «Ver detalle» row for it.
	val opensSheet: Boolean

	// A component whose catalog has no item yet: it is drawn by the wire renderer of its catalog. It goes when the last
	// catalog has its items.
	data class Unmigrated(val component: ChatComponent) : ChatComponentItem {
		override val id: String get() = component.id

		override val opensSheet: Boolean get() = component.display == ComponentDisplay.SHEET
	}
}
