package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// resumen.atencion/1: one row per item that needs attention.
@Immutable
data class AttentionCardItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<AttentionRowItem>,
	override val seeAllText: UiText
) : RecordComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
