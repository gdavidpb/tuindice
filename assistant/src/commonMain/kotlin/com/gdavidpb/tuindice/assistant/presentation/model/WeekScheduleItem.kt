package com.gdavidpb.tuindice.assistant.presentation.model

// horario.semana/1: the classes of the week as the figure, then each day with its class blocks.
data class WeekScheduleItem(
	override val id: String,
	override val opensSheet: Boolean,
	val classesFigure: CardFigureItem,
	val days: List<ScheduleDayItem>
) : RecordComponentItem
