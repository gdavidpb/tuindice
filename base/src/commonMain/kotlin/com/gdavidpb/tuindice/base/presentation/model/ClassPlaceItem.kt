package com.gdavidpb.tuindice.base.presentation.model

/**
 * Where a class meets, said the way the university speaks of it: the blocks and the room ("Bloques
 * 1-2 · MYS-116") and, after them and always secondary, the clock time of those blocks ("7:30–9:30
 * a. m."), absent when the blocks do not fit a day.
 */
data class ClassPlaceItem(
	val placeText: UiText,
	val timeText: UiText?
)
