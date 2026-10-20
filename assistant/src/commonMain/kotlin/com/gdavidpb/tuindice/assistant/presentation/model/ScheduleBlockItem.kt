package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One class block of a day: its subject, its room, its hours, and the chip of the evaluation that falls in it, if one
// does.
data class ScheduleBlockItem(
	val code: String,
	val room: String,
	val evaluationChip: CardChipItem?,
	val hoursText: UiText
)
