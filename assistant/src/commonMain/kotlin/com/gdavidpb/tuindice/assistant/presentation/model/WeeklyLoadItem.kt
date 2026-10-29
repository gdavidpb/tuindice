package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// evaluaciones.carga/1: how many weeks are heavy, then a row per week, with a chip on a heavy week.
@Immutable
data class WeeklyLoadItem(
	override val id: String,
	override val opensSheet: Boolean,
	val heavyFigure: CardFigureItem,
	val weeks: List<WeekLoadRowItem>,
	val recordLink: CardLinkItem?
) : AcademicComponentItem
