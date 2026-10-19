package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.ui.icon.ShareIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_next_title

/**
 * "Qué sigue": the actions the report chose, each a full-width row with its icon, its label and a chevron. A row leads
 * to the screen its target names, the way the same action leads from the chat.
 */
@Composable
fun TermReportNextActionsView(
	actions: List<TermReportActionItem>,
	onActionClick: (TermReportActionItem) -> Unit,
	modifier: Modifier = Modifier
) {
	if (actions.isEmpty()) return

	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.TermReportNextActions),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		Text(
			text = stringResource(Res.string.term_report_next_title),
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)

		actions.forEachIndexed { index, action ->
			TermReportNextActionRow(
				action = action,
				onClick = { onActionClick(action) },
				modifier = Modifier.testTag(TermReportUiTags.termReportNextAction(index))
			)
		}
	}
}

@Composable
private fun TermReportNextActionRow(
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
				imageVector = action.target.icon(),
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

// The mark of each screen an action can open, the same screens the chat's actions open.
private fun AssistantNavTarget.icon(): ImageVector {
	return when (this) {
		AssistantNavTarget.RECORD_PROJECTION -> Icons.AutoMirrored.Outlined.TrendingUp
		AssistantNavTarget.RECORD_SCHEDULE, AssistantNavTarget.SUBJECTS_SEARCH -> Icons.Outlined.AccountTree
		AssistantNavTarget.SHARE_TERM -> ShareIcon
		AssistantNavTarget.STATISTICS -> Icons.Outlined.BarChart
		AssistantNavTarget.PENSUM, AssistantNavTarget.SUBJECTS_DETAIL -> Icons.Outlined.School
		AssistantNavTarget.RECORD, AssistantNavTarget.RECORD_CREATE_TERM -> Icons.Outlined.TableChart
		AssistantNavTarget.EVALUATIONS, AssistantNavTarget.EVALUATIONS_ADD, AssistantNavTarget.EVALUATIONS_DETAIL ->
			Icons.AutoMirrored.Outlined.Assignment
		AssistantNavTarget.ENROLLMENT_PROOF -> Icons.Outlined.FindInPage
		AssistantNavTarget.SUMMARY -> Icons.Outlined.Dashboard
		AssistantNavTarget.ABOUT -> Icons.Outlined.Info
		AssistantNavTarget.SIGN_IN, AssistantNavTarget.UPDATE_PASSWORD -> Icons.Outlined.Lock
	}
}
