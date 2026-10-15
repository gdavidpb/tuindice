package com.gdavidpb.tuindice.summary.presentation.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.base.presentation.model.UiText

/**
 * A card of the hub that says something without rows of data, under its [icon], which the mapper picks: an invitation
 * to what the student has not done yet, a status, or what a section lacks. [titleText] and [messageText] are what it says, and either may be absent. [action]
 * is the button it offers, if any. [target] makes the whole card tappable, with [targetLabel] for a screen reader: a
 * card with no target is informative, has no arrow and tells itself as a single element.
 */
@Immutable
data class SummaryCardItem(
	val icon: ImageVector,
	val titleText: UiText?,
	val messageText: UiText?,
	val action: Action? = null,
	val target: SummaryHubTarget? = null,
	val targetLabel: UiText? = null
) {
	/** The button of a card: its [text] and the screen it leads to. */
	@Immutable
	data class Action(
		val text: UiText,
		val target: SummaryHubTarget
	)
}
