package com.gdavidpb.tuindice.assistant.presentation.model

// informe.ritmo/1: the average of credits per term as the figure, then the credits of each term and the terms left.
data class PaceItem(
	override val id: String,
	override val opensSheet: Boolean,
	val averageFigure: CardFigureItem,
	val terms: List<CreditsRowItem>,
	// Null when the service does not estimate the terms left.
	val remainingTerms: String?
) : RecordComponentItem
