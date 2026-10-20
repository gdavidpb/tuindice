package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// materias.lista/1: a row per subject, with the chip of its status and a link to it.
data class SubjectListItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<SubjectRowItem>,
	val recordLink: CardLinkItem?,
	override val seeAllText: UiText
) : AcademicComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
