package com.gdavidpb.tuindice.assistant.presentation.model

// horario.semana/1: each day of the week with its class blocks.
data class WeekScheduleItem(
	override val id: String,
	override val opensSheet: Boolean,
	val days: List<ScheduleDayItem>
) : RecordComponentItem
