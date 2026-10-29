package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// One component of an agent message, as its view draws it. The mapper builds it from the wire component, so the view
// only paints what the item holds.
@Immutable
sealed interface ChatComponentItem {
	val id: String

	// The component opens on its own sheet: its message shows the «Ver detalle» row for it.
	val opensSheet: Boolean
}
