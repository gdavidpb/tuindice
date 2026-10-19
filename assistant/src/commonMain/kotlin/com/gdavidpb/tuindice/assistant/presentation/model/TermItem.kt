package com.gdavidpb.tuindice.assistant.presentation.model

// informe.trimestre/1: the index of the term, with the cumulative index beside it, then a row per subject.
data class TermItem(
	override val id: String,
	override val opensSheet: Boolean,
	val termLabel: String,
	// Null when the card brings neither index.
	val primaryFigure: CardFigureItem?,
	val secondaryFigure: CardFigureItem?,
	val rows: List<TermRowItem>,
	// The link to the record, when the card names its term.
	val recordLink: CardLinkItem?
) : RecordComponentItem
