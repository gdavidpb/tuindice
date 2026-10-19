package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One source of the sync: its name, with the detail it has when it has one, and the chip with its state, when it has
// one.
data class SyncSourceItem(
	val name: UiText,
	val stateText: UiText?
)
