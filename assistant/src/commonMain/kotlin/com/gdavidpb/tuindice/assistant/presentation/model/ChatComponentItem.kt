package com.gdavidpb.tuindice.assistant.presentation.model

// One component of an agent message, as its view draws it. The mapper builds it from the wire component, so the view
// only paints what the item holds.
sealed interface ChatComponentItem {
	val id: String

	// The component opens on its own sheet: its message shows the «Ver detalle» row for it.
	val opensSheet: Boolean
}
