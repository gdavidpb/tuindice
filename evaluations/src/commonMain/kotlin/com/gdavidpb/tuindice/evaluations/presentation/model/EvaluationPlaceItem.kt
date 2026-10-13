package com.gdavidpb.tuindice.evaluations.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * Where an evaluation takes place, inherited from the class its subject has that day: the blocks and
 * the room ("Bloques 1-2 · MYS-116") and, after them and always secondary, the clock time of those
 * blocks ("7:30–9:30"), absent when the blocks do not fit a day.
 */
data class EvaluationPlaceItem(
	val placeText: UiText,
	val timeText: String?
)
