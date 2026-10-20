package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// horario.choques/1: one row per clash.
data class ClashesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<ClashRowItem>,
	override val seeAllText: UiText
) : RecordComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
