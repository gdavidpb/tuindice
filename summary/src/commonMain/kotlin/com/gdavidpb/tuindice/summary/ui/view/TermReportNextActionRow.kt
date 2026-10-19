package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem

@Composable
fun TermReportNextActionRow(
	action: TermReportActionItem,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	// The click is inside the card, so its ripple is clipped to the card's rounded corners.
	SummaryHubCardView(modifier = modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.clickable(role = Role.Button, onClick = onClick)
				.padding(horizontal = TuIndiceSpacing.Screen, vertical = TuIndiceSpacing.Large),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			Icon(
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				imageVector = action.icon.imageVector(),
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface
			)

			Text(
				modifier = Modifier.weight(1f),
				text = action.label,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface
			)

			Icon(
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Muted)
			)
		}
	}
}
