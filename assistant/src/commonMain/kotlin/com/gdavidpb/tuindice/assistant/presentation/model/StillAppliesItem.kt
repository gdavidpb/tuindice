package com.gdavidpb.tuindice.assistant.presentation.model

// sistema.sigue_aplicando/1: a remembered statement and the two answers to it.
data class StillAppliesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val memoryId: String,
	val statement: String,
	// The answer the student gave, once the service saved it: the question keeps its outcome on the sheet.
	val answer: Boolean? = null,
	// The answer is being saved: the card's buttons wait for it.
	val isPending: Boolean = false
) : SistemaComponentItem
