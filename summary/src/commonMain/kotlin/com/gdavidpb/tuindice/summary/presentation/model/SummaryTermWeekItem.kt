package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * The line "Semana 3 de 12" and the track of twelve segments under it. [valueText] and [restText]
 * are the two parts of the line (the figure and what is around it, lighter); [description] says it
 * whole for a screen reader.
 */
@Immutable
data class SummaryTermWeekItem(
	val week: Int,
	val totalWeeks: Int,
	val valueText: UiText,
	val restText: UiText,
	val description: UiText
)
