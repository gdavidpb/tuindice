package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// sistema.sigue_aplicando/1: a remembered statement and the two answers to it.
@Immutable
data class StillAppliesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val memoryId: String,
	val statement: String,
	// The answer given on this sheet, or the one the state of the memory on the server gives: ACTIVE is yes, and a memory
	// the list does not have is no. Null while the question still asks.
	val answer: Boolean? = null,
	// The answer is being saved, or the read that covers the card has not come back: the card's buttons wait.
	val isPending: Boolean = false
) : SistemaComponentItem
