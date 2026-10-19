package com.gdavidpb.tuindice.assistant.presentation.model

// plan.lista/1: how many plans there are, then a row per term plan.
data class PlanListItem(
	override val id: String,
	override val opensSheet: Boolean,
	val countFigure: CardFigureItem,
	val rows: List<PlanRowItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
