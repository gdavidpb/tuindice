package com.gdavidpb.tuindice.assistant.presentation.model

// horario.choques/1: how many clashes the schedule has, then one row per clash.
data class ClashesItem(
	override val id: String,
	override val opensSheet: Boolean,
	val countFigure: CardFigureItem,
	val rows: List<ClashRowItem>
) : RecordComponentItem
