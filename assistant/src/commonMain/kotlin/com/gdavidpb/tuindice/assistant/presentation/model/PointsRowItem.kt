package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One subject of the points: its points earned over the points evaluated, the part of its total that is earned and the
// part that is evaluated so far, and the sentence a screen reader says for the row.
data class PointsRowItem(
	val code: String,
	val name: String,
	val pointsText: UiText,
	val earnedFraction: Float,
	val evaluatedFraction: Float,
	val description: UiText
)
