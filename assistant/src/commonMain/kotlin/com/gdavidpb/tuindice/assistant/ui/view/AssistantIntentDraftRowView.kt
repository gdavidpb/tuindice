package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.IntentDraftItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One row of a batch intent: a checkbox the student unmarks before confirming the batch.
@Composable
fun AssistantIntentDraftRowView(
	intentId: String,
	draft: IntentDraftItem,
	enabled: Boolean,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.heightIn(min = TuIndiceComponentSizes.IconButton)
			.testTag(AssistantUiTags.Intent.draft(intentId, draft.mutationId))
			.toggleable(
				value = draft.selected,
				enabled = enabled,
				role = Role.Checkbox,
				onValueChange = { selected ->
					onAction(AssistantChat.Action.ToggleIntentDraft(intentId, draft.mutationId, selected))
				}
			),
		verticalAlignment = Alignment.CenterVertically
	) {
		Checkbox(
			checked = draft.selected,
			onCheckedChange = null,
			enabled = enabled
		)
		Column(modifier = Modifier.padding(start = TuIndiceSpacing.Medium)) {
			Text(
				text = draft.label,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			Text(
				text = draft.detail,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
