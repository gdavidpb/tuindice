package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.NoteAdd
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardIcon
import com.gdavidpb.tuindice.summary.presentation.model.SummaryCardItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

/**
 * The card of the hub that says what a section lacks, from its [item]: an icon, a title and a message (either of the
 * two may be absent), the button it offers and the screen the whole card leads to. A card with a target is one button,
 * labelled for a screen reader by its target label, and wears an arrow. A card with no target and no button is
 * informative and reads as a single element. [footer] sits under the text, for a card that needs a line of its own,
 * as the career's empty bar does.
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
			CardRow(item = item, actions = actions, hasArrow = target != null)

			footer?.invoke()
		}
	}
}

// The icon and the text of the card, its button under the text, and the arrow of a card that leads somewhere.
@Composable
private fun CardRow(
	item: SummaryCardItem,
	actions: SummaryHubActions,
	hasArrow: Boolean
) {
	Row(
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.Icon),
			imageVector = item.icon.toImageVector(),
			contentDescription = null,
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Column(
			modifier = Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(
				if (item.action != null) TuIndiceSpacing.Medium else TuIndiceSpacing.Two
			)
		) {
			item.titleText?.let { title ->
				Text(
					text = title.asString(),
					style = MaterialTheme.typography.bodyLarge,
					fontWeight = FontWeight.SemiBold
				)
			}

			item.messageText?.let { message ->
				Text(
					text = message.asString(),
					style = MaterialTheme.typography.bodyMedium,
					color = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
				)
			}

			item.action?.let { action ->
				OutlinedButton(
					modifier = Modifier.testTag(SummaryUiTags.CardAction),
					onClick = { actions.onTargetClick(action.target) }
				) {
					Text(text = action.text.asString())
				}
			}
		}

		if (hasArrow) {
			Icon(
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Muted)
			)
		}
	}
}

private fun SummaryCardIcon.toImageVector(): ImageVector = when (this) {
	SummaryCardIcon.Evaluations -> Icons.AutoMirrored.Outlined.Assignment
	SummaryCardIcon.Done -> Icons.Outlined.CheckCircle
	SummaryCardIcon.Calendar -> Icons.Outlined.CalendarMonth
	SummaryCardIcon.NewTerm -> Icons.AutoMirrored.Outlined.NoteAdd
	SummaryCardIcon.Career -> Icons.Outlined.AccountTree
	SummaryCardIcon.Statistics -> Icons.Outlined.BarChart
}
