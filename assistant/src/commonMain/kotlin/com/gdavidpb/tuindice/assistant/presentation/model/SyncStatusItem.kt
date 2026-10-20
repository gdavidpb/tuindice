package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// perfil.estado_sync/1: the state of the sync as the headline, when it last ran, one row per source with its state, and
// the cause of an annulment when there is one.
data class SyncStatusItem(
	override val id: String,
	override val opensSheet: Boolean,
	// The moment the sync last ran, as the row of that name shows it; null when it never ran.
	val lastSyncText: UiText?,
	// Null when there are no sources to state a headline for.
	val headlineText: UiText?,
	val sources: List<SyncSourceItem>,
	val annulmentCause: String?
) : RecordComponentItem
