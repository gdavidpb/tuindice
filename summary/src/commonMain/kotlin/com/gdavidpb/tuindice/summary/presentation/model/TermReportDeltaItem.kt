package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A change as the chip shows it: its figure with the arrow, the tone that colours it, and the words a screen reader
 * says for it, which name the direction in words rather than by the arrow. [reference] is the term the change is
 * measured from, written under the figure; it is null when the chip says the change alone.
 */
@Immutable
data class TermReportDeltaItem(
	val text: UiText,
	val description: UiText,
	val tone: TermReportDeltaTone,
	val reference: UiText? = null
)
