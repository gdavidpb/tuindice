package com.gdavidpb.tuindice.assistant.presentation.model

// resumen.atencion/1: how many items need attention, then one row per item.
data class AttentionCardItem(
	override val id: String,
	override val opensSheet: Boolean,
	val count: CardFigureItem,
	val rows: List<AttentionRowItem>
) : ChatComponentItem
