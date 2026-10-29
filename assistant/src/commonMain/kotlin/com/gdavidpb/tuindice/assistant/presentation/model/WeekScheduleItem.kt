package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// horario.semana/1: each day of the week with its class blocks.
@Immutable
data class WeekScheduleItem(
	override val id: String,
	override val opensSheet: Boolean,
	val days: List<ScheduleDayItem>
) : RecordComponentItem
