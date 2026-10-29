package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One class block of a day: its subject, its blocks and hours, and the chip of the evaluation that falls in it, if one
// does.
data class ScheduleBlockItem(
	val code: String,
	// The name of the subject, and the room after it when the block has one.
	val nameText: UiText,
	val evaluationChip: CardChipItem?,
	// The blocks, then the hours after them: "1-2 · 7:30–9:30 a. m.", the way the app writes a class everywhere.
	val scheduleText: UiText,
	// The hours alone, which the card draws in the muted tone at the end of the schedule.
	val hoursText: UiText
)
