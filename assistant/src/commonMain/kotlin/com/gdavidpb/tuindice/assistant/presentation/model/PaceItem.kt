package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// informe.ritmo/1: the average of credits per term as the figure, then the credits of each term and the terms left.
@Immutable
data class PaceItem(
	override val id: String,
	override val opensSheet: Boolean,
	val averageFigure: CardFigureItem,
	val terms: List<CreditsRowItem>,
	// Null when the service does not estimate the terms left.
	val remainingTerms: String?
) : RecordComponentItem
