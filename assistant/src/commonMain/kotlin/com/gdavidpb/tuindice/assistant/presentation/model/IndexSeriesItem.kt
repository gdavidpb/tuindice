package com.gdavidpb.tuindice.assistant.presentation.model

// informe.serie/1: the cumulative index of the latest term as the figure, then a row per term.
data class IndexSeriesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val currentFigure: CardFigureItem?,
	val points: List<IndexPointItem>
) : RecordComponentItem
