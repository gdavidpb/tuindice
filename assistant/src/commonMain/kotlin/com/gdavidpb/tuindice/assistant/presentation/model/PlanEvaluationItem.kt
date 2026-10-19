package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// plan.evaluacion/1: the load band of the plan is the figure, with its load index beside it when the service brings
// one. Then the credits, the requirements still pending, the warnings of the slots and the clashes.
data class PlanEvaluationItem(
	override val id: String,
	override val opensSheet: Boolean,
	val termLabel: String,
	// Null when the card brings neither a load band nor an index.
	val primaryFigure: CardFigureItem?,
	val secondaryFigure: CardFigureItem?,
	val creditsText: UiText,
	val pendingRequirements: List<String>,
	val slotWarnings: List<String>,
	val clashesName: UiText,
	// Null when the clashes are not counted yet, so the row shows only its name.
	val clashesValueText: String?,
	val recordLink: CardLinkItem?
) : ChatComponentItem
