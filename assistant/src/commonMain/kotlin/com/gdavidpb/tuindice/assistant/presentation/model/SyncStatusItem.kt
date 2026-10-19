package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// perfil.estado_sync/1: the state of the sync as the headline, one row per source with its state, and the cause of an
// annulment when there is one.
data class SyncStatusItem(
	override val id: String,
	override val opensSheet: Boolean,
	// The label of the card: when the sync last ran, or the plain label when it never ran.
	val labelText: UiText,
	// Null when there are no sources to state a headline for.
	val headlineText: UiText?,
	val sources: List<SyncSourceItem>,
	val annulmentCause: String?
) : ChatComponentItem
