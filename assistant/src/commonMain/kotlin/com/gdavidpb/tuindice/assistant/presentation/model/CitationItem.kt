package com.gdavidpb.tuindice.assistant.presentation.model

// conocimiento.cita/1: one entry per cited source, and the link to the record the card names, when it names one.
data class CitationItem(
	override val id: String,
	override val opensSheet: Boolean,
	val sources: List<CitationSourceItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
