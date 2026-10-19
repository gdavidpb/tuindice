package com.gdavidpb.tuindice.assistant.presentation.model

// evaluaciones.lista/1: how many evaluations there are, then a row per evaluation. The card links to the record it names.
data class EvaluationListItem(
	override val id: String,
	override val opensSheet: Boolean,
	val countFigure: CardFigureItem,
	val rows: List<EvaluationRowItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
