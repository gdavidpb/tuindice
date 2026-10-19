package com.gdavidpb.tuindice.assistant.presentation.model

// sistema.apoyo/1: where to reach support, each line with its contact.
data class SupportItem(
	override val id: String,
	override val opensSheet: Boolean,
	val lines: List<SupportLineItem>
) : ChatComponentItem
