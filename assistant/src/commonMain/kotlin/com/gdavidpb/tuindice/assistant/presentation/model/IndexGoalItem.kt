package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// informe.meta/1: the term index the goal needs as the figure, then the cumulative goal with its chip, and the credits.
data class IndexGoalItem(
	override val id: String,
	override val opensSheet: Boolean,
	val requiredFigure: CardFigureItem?,
	val feasibilityText: UiText,
	val targetText: String,
	val creditsText: String
) : ChatComponentItem
