package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * The last term that is over: [nameText] is its period ("Abr - Jul 2026") and [stateText] says it is over
 * ("terminado"). Two rows follow: [indexLabel] with [indexText], its index with four decimals as Registro shows it,
 * and [creditsLabel] with [creditsText], the credits it approved. [termKey] is the key the share sheet opens it by, and
 * [isShareable] says whether the sheet has a card for it, which is what the share button of the term needs.
 */
@Immutable
data class SummaryFinishedTermItem(
	val nameText: UiText,
	val stateText: UiText,
	val indexLabel: UiText,
	val indexText: String,
	val creditsLabel: UiText,
	val creditsText: UiText,
	val termKey: String,
	val isShareable: Boolean
)
