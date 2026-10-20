package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// materias.recomendadas/1: a row per recommended elective, with the short reason the service gives for it.
data class RecommendedElectivesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<ElectiveRowItem>,
	val recordLink: CardLinkItem?,
	override val seeAllText: UiText
) : AcademicComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
