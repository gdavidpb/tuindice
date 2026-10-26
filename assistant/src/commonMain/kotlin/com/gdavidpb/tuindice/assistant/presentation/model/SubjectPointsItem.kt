package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// evaluaciones.puntos/1: a row per subject with its points earned over the points evaluated so far.
@Immutable
data class SubjectPointsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<PointsRowItem>,
	val recordLink: CardLinkItem?,
	override val seeAllText: UiText
) : AcademicComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
