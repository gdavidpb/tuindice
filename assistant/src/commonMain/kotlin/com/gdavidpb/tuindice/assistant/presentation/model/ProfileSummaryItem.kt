package com.gdavidpb.tuindice.assistant.presentation.model

// perfil.resumen/1: the cumulative index with the approved credits beside it, then the career, the pensum and the
// current term.
data class ProfileSummaryItem(
	override val id: String,
	override val opensSheet: Boolean,
	val careerCode: String,
	val careerName: String,
	val pensumLabel: String,
	val primaryFigure: CardFigureItem,
	// Null when the card has no index: the credits are the only figure then.
	val secondaryFigure: CardFigureItem?,
	val currentTermLabel: String?
) : RecordComponentItem
