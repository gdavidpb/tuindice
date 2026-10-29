package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One term plan: its term, its subjects and credits, and the link to the record of that term.
data class PlanRowItem(
	val termLabel: String,
	val summaryText: UiText,
	val link: CardLinkItem
)
