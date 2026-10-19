package com.gdavidpb.tuindice.assistant.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.UiText

// materias.estadisticas/1: the subject is the label, the average is the figure with the pass rate beside it, then the
// difficulty, the median and the grade histogram.
data class SubjectStatsItem(
	override val id: String,
	override val opensSheet: Boolean,
	val subjectName: String,
	// Null when the service brings neither an average nor a pass rate.
	val primaryFigure: CardFigureItem?,
	val secondaryFigure: CardFigureItem?,
	val difficultyText: UiText?,
	val medianText: String?,
	val histogram: List<HistogramBarItem>,
	val recordLink: CardLinkItem?
) : ChatComponentItem
