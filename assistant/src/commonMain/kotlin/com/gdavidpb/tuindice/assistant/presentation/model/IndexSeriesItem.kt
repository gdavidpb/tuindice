package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// informe.serie/1: the cumulative index of the latest term as the figure, then a row per term, from the oldest to the
// newest.
@Immutable
data class IndexSeriesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val currentFigure: CardFigureItem?,
	val points: List<IndexPointItem>,
	override val seeAllText: UiText
) : RecordComponentItem, ChatRowsItem {
	override val rowCount: Int get() = points.size
}
