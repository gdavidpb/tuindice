package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// informe.estadisticas/1: the approved subjects as the figure, the other counts as rows, and the grade histogram.
@Immutable
data class RecordStatsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val approvedFigure: CardFigureItem,
	val counts: List<CountRowItem>,
	val histogram: List<HistogramBarItem>
) : RecordComponentItem
