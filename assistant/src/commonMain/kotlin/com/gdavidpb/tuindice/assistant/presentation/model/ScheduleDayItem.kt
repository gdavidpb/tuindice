package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// One day with classes: its name, and its blocks.
data class ScheduleDayItem(
	val name: UiText,
	val blocks: List<ScheduleBlockItem>
)
