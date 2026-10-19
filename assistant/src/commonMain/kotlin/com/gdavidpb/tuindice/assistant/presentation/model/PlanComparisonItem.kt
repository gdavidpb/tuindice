package com.gdavidpb.tuindice.assistant.presentation.model

// plan.comparacion/1: one block per candidate plan, in the order the service gives them.
data class PlanComparisonItem(
	override val id: String,
	override val opensSheet: Boolean,
	val options: List<PlanOptionItem>,
	val recordLink: CardLinkItem?
) : PensumPlanComponentItem
