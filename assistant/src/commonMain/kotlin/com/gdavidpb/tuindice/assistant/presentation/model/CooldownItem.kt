package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// sistema.enfriamiento/1: the assistant is paused for the student until a moment; they may appeal when allowed.
data class CooldownItem(
	override val id: String,
	override val opensSheet: Boolean,
	val reasonText: UiText,
	val untilText: UiText,
	val appealable: Boolean
) : SistemaComponentItem
