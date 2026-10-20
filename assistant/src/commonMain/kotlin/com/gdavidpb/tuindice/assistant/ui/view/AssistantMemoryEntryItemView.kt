package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_delete_memory
import tuindice.assistant.generated.resources.assistant_memory_still_applies_no
import tuindice.assistant.generated.resources.assistant_memory_still_applies_yes
import tuindice.assistant.generated.resources.assistant_still_applies_title

// One thing the assistant remembers, as a row of its group: its text, when it was saved, its anchor, and the delete
// button. When it needs confirmation, «¿Sigue aplicando?» goes under it, with its two answers.
@Composable
fun AssistantMemoryEntryItemView(
	entry: MemoryEntryItem,
	onAction: (AssistantMemory.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier
			.fillMaxWidth()
			.testTag(AssistantUiTags.Memory.entry(entry.id))
			.padding(
				start = TuIndiceSpacing.Dialog,
				top = TuIndiceSpacing.Medium,
				end = TuIndiceSpacing.XLarge,
				bottom = TuIndiceSpacing.Medium
			)
	) {
		Row(verticalAlignment = Alignment.Top) {
			EntryTexts(entry = entry, modifier = Modifier.weight(1f))

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
			StillAppliesRow(memoryId = entry.id, onAction = onAction)
		}
	}
}

@Composable
private fun EntryTexts(
	entry: MemoryEntryItem,
	modifier: Modifier
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two)
	) {
		Text(
			text = entry.text,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = entry.sinceText.asString(),
			style = MaterialTheme.typography.bodyMedium,
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
}

// The question and its two answers on one line: «Sí» in the accent of the theme, «Ya no» in the muted tone.
@Composable
private fun StillAppliesRow(
	memoryId: String,
	onAction: (AssistantMemory.Action) -> Unit
) {
	Row(verticalAlignment = Alignment.CenterVertically) {
		Text(
			text = stringResource(Res.string.assistant_still_applies_title),
			style = MaterialTheme.typography.bodyLarge,
			fontWeight = FontWeight.SemiBold,
			color = MaterialTheme.colorScheme.onSurface
		)
		TextButton(
			onClick = { onAction(AssistantMemory.Action.AnswerStillApplies(memoryId, applies = true)) },
			modifier = Modifier.testTag(AssistantUiTags.Memory.stillAppliesYes(memoryId))
		) {
			Text(text = stringResource(Res.string.assistant_memory_still_applies_yes))
		}
		TextButton(
			onClick = { onAction(AssistantMemory.Action.AnswerStillApplies(memoryId, applies = false)) },
			modifier = Modifier.testTag(AssistantUiTags.Memory.stillAppliesNo(memoryId)),
			colors = ButtonDefaults.textButtonColors(
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant
			)
		) {
			Text(text = stringResource(Res.string.assistant_memory_still_applies_no))
		}
	}
}
