package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
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
