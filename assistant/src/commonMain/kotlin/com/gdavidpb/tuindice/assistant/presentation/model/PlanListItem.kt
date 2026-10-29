package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// plan.lista/1: a row per term plan.
@Immutable
data class PlanListItem(
	override val id: String,
	override val opensSheet: Boolean,
	val rows: List<PlanRowItem>,
	val recordLink: CardLinkItem?,
	override val seeAllText: UiText
) : PensumPlanComponentItem, ChatRowsItem {
	override val rowCount: Int get() = rows.size
}
