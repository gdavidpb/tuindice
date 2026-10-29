package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// sistema.apoyo/1: where to reach support, each line with its contact.
@Immutable
data class SupportItem(
	override val id: String,
	override val opensSheet: Boolean,
	val lines: List<SupportLineItem>
) : SistemaComponentItem
