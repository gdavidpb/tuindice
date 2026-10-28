package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// evaluaciones.lista/1: a row per evaluation. The card links to the record it names.
@Immutable
data class EvaluationListItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<EvaluationRowItem>,
	val recordLink: CardLinkItem?,
	override val seeAllText: UiText,
	// The weeks the sheet is grouped by; null when the app has no current term, and then the sheet lists every row.
	val weeks: EvaluationWeeksItem? = null
) : AcademicComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
