package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/** "Carrera": the progress of the pensum, or the invitation to open it when it was never calculated. */
sealed interface SummaryCareerItem {
	/**
	 * "156 de 240 créditos" ([valueText] and [restText]) and "65 %" ([percentText]); [fraction] fills
	 * the bar, and [description] says all of it for a screen reader.
	 */
	@Immutable
	data class Progress(
		val valueText: String,
		val restText: UiText,
		val percentText: UiText,
		val fraction: Float,
		val description: UiText
	) : SummaryCareerItem

	/** No pensum in the cache: the card leads to Pensum, which is what calculates it. */
	data object Invite : SummaryCareerItem
}
