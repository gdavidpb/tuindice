package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// evaluaciones.meta/1: the points the target grade still asks for, with the points left beside them, and whether the
// target is reachable.
data class RequiredGradeItem(
	override val id: String,
	override val opensSheet: Boolean,
	val primaryFigure: CardFigureItem,
	val secondaryFigure: CardFigureItem?,
	val subjectCode: String,
	val subjectName: String,
	val targetText: UiText,
	val feasibleText: UiText,
	val recordLink: CardLinkItem?
) : AcademicComponentItem
