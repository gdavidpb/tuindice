package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One class block of a day: its subject, its room, its hours, and whether an evaluation falls in it.
data class ScheduleBlockItem(
	val code: String,
	val room: String,
	val hasEvaluation: Boolean,
	val hoursText: UiText
)
