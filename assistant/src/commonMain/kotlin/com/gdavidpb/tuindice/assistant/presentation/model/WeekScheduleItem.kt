package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// horario.semana/1: the classes of the week as the figure, then each day with its class blocks.
data class WeekScheduleItem(
	override val id: String,
	override val opensSheet: Boolean,
	val classesFigure: CardFigureItem,
	val days: List<ScheduleDayItem>
) : RecordComponentItem

// One day with classes: its name, and its blocks.
data class ScheduleDayItem(
	val name: UiText,
	val blocks: List<ScheduleBlockItem>
)

// One class block of a day: its subject, its room, its hours, and whether an evaluation falls in it.
data class ScheduleBlockItem(
	val code: String,
	val room: String,
	val hasEvaluation: Boolean,
	val hoursText: UiText
)
