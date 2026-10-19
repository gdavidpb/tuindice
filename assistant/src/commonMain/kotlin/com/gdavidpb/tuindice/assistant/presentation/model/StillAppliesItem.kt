package com.gdavidpb.tuindice.assistant.presentation.model

// sistema.sigue_aplicando/1: a remembered statement and the two answers to it.
data class StillAppliesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val memoryId: String,
	val statement: String
) : SistemaComponentItem
