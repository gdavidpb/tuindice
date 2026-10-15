package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
 * The row of a card of the hub: its icon, its title and message with the button under them, and the arrow of a card
 * that leads somewhere ([hasArrow]). The icon is decorative, so the text of the row is what a screen reader reads.
 */
@Composable
fun SummaryCardRowView(
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
