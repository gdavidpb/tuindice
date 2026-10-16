package com.gdavidpb.tuindice.base.presentation.model

/**
 * Where a class meets, in the two parts the rows give it. The [room] ("MYS-116") is kept apart, so a row can give it
 * a line of its own, and it is null when the class names none. The [scheduleText] is the blocks and, after them, the
 * clock hours ("1-2 · 7:30–9:30 a. m."), or the blocks alone ("1-2") when those blocks do not fall in the day.
 */
data class ClassPlaceItem(
	val room: String?,
	val scheduleText: UiText
)
