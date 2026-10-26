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
	override val seeAllText: UiText
) : AcademicComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
