package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_delete_memory
import tuindice.assistant.generated.resources.assistant_still_applies_title

// One thing the assistant remembers: its text, category and anchor, a delete button, and «¿Sigue aplicando?» with its
// two answers when the entry needs confirmation.
@Composable
fun AssistantMemoryEntryItemView(
	entry: MemoryEntryItem,
	onAction: (AssistantMemory.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Memory.entry(entry.id))) {
		Row(verticalAlignment = Alignment.Top) {
			Column(
				modifier = Modifier.weight(1f),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
			) {
				Text(
					text = entry.text,
					style = MaterialTheme.typography.bodyLarge,
					color = MaterialTheme.colorScheme.onSurface
				)
				Text(
					text = entry.categoryLabel,
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
				entry.anchorLabel?.let { anchor ->
					Text(
						text = anchor,
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			IconButton(
				onClick = { onAction(AssistantMemory.Action.DeleteEntry(entry.id)) },
				modifier = Modifier.testTag(AssistantUiTags.Memory.delete(entry.id))
			) {
				Icon(
					imageVector = Icons.Outlined.DeleteOutline,
					contentDescription = stringResource(Res.string.a11y_assistant_delete_memory),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
		if (entry.needsConfirmation) {
			Text(
				text = stringResource(Res.string.assistant_still_applies_title),
				style = MaterialTheme.typography.titleMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
			AssistantStillAppliesButtonsView(
				memoryId = entry.id,
				onAnswer = { applies ->
					onAction(AssistantMemory.Action.AnswerStillApplies(entry.id, applies))
				}
			)
		}
	}
}
