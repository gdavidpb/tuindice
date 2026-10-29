package com.gdavidpb.tuindice.assistant.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

// informe.meta/1: the term index the goal needs as the figure, then the cumulative goal with its chip, and the
// credits of the term the goal counts.
@Immutable
data class IndexGoalItem(
	override val id: String,
	override val opensSheet: Boolean,
	val requiredFigure: CardFigureItem?,
	val feasibilityChip: CardChipItem,
	val targetText: String,
	val creditsText: UiText
) : RecordComponentItem
