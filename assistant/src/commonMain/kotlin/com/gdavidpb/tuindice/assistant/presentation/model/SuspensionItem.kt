package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// sistema.suspension/1: the assistant is off for the student, until a moment when the service sets one.
data class SuspensionItem(
	override val id: String,
	override val opensSheet: Boolean,
	val reasonText: UiText,
	// The moment it ends, or that it has no end.
	val untilText: UiText,
	val appealable: Boolean
) : SistemaComponentItem
