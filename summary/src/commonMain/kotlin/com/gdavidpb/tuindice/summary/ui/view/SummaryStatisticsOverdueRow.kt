package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsCurrentTermItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.model.SummaryHubActions

// The evaluations that went by without a grade, in one line with its warning and a rule above it ("2 vencidas sin
// nota"). The line leads to the week of the oldest one when the hub has it, and then it shows an arrow.
@Composable
fun SummaryStatisticsOverdueRow(
	overdue: SummaryStatisticsCurrentTermItem.Overdue,
	actions: SummaryHubActions
) {
	val target = overdue.target

	HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = TuIndiceComponentSizes.IconButton)
			.testTag(SummaryUiTags.StatisticsOverdue)
			.then(
				if (target == null) {
					Modifier
				} else {
					Modifier.clickable(role = Role.Button, onClick = { actions.onTargetClick(target) })
				}
			),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			modifier = Modifier.size(TuIndiceComponentSizes.Icon),
			imageVector = Icons.Outlined.WarningAmber,
			contentDescription = null,
			tint = AcademicStatusColors.warning()
		)

		Text(
			modifier = Modifier.weight(1f),
			text = overdue.text.asString(),
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)

		if (target != null) {
			Icon(
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Muted)
			)
		}
	}
}
