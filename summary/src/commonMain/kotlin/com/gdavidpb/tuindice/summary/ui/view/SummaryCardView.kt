package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

/**
 * The card of the hub that says what a section lacks, from its [item]: an icon, a title and a message (either of the
 * two may be absent), the button it offers and the screen the whole card leads to. A card with a target is one button,
 * labelled for a screen reader by its target label, and wears an arrow. A card with no target and no button is
 * informative and reads as a single element. [footer] sits under the text, for a card that needs a line of its own, as
 * the career's empty bar does.
 */
@Composable
fun SummaryCardView(
	item: SummaryCardItem,
	actions: SummaryHubActions,
	modifier: Modifier = Modifier,
	footer: (@Composable () -> Unit)? = null
) {
	val target = item.target
	val cardModifier = when {
		target != null -> modifier.clickable(
			onClickLabel = item.targetLabel?.asString(),
			role = Role.Button,
			onClick = { actions.onTargetClick(target) }
		)

		item.action == null -> modifier.semantics(mergeDescendants = true) { }

		else -> modifier
	}

	SummaryHubCardView(modifier = cardModifier) {
		Column(
			modifier = Modifier.padding(TuIndiceSpacing.Screen),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			SummaryCardRowView(item = item, actions = actions, hasArrow = target != null)

			footer?.invoke()
		}
	}
}
