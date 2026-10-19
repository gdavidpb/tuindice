package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One subject of the points: its points, and the part of its total that is graded so far.
data class PointsRowItem(
	val code: String,
	val name: String,
	val pointsText: UiText,
	val progress: Float
)
