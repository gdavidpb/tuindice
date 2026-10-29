package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable

// pensum.comparacion/1: the credits the two pensums share are the figure, then the career and the pensum compared, the
// subjects still missing, and the notes the service gives.
@Immutable
data class PensumComparisonItem(
	override val id: String,
	override val opensSheet: Boolean,
	val sharedFigure: CardFigureItem,
	val otherCareerName: String,
	val otherPensumLabel: String,
	val missingSubjects: List<SubjectRowItem>,
	val notes: List<String>,
	val recordLink: CardLinkItem?
) : PensumPlanComponentItem
