package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// informe.estadisticas/1: the approved subjects as the figure, the other counts as rows, and the grade histogram.
data class RecordStatsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val approvedFigure: CardFigureItem,
	val counts: List<CountRowItem>,
	val histogram: List<HistogramBarItem>
) : RecordComponentItem

// One count of the record, with its label.
data class CountRowItem(
	val label: UiText,
	val value: String
)
