package com.gdavidpb.tuindice.assistant.presentation.model

// materias.lista/1: how many subjects there are, then a row per subject, with the chip of its status and a link to it.
data class SubjectListItem(
	override val id: String,
	override val opensSheet: Boolean,
	val countFigure: CardFigureItem,
	val rows: List<SubjectRowItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
