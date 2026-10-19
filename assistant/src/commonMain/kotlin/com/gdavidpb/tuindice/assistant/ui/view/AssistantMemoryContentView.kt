package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_clear_all

// What the assistant remembers, one row per entry, and «Eliminar todo» at the end, as the destructive action.
@Composable
fun AssistantMemoryContentView(
	entries: List<MemoryEntryItem>,
	onAction: (AssistantMemory.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier.fillMaxSize().testTag(AssistantUiTags.MemoryContent),
		contentPadding = PaddingValues(all = TuIndiceSpacing.Screen),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		items(items = entries, key = MemoryEntryItem::id) { entry ->
			AssistantMemoryEntryItemView(entry = entry, onAction = onAction)
		}
		item(key = AssistantUiTags.MemoryClearAll) {
			OutlinedButton(
				onClick = { onAction(AssistantMemory.Action.ClearAll) },
				modifier = Modifier.fillMaxWidth().testTag(AssistantUiTags.MemoryClearAll),
				colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
				border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.error)
			) {
				Text(text = stringResource(Res.string.assistant_memory_clear_all))
			}
		}
	}
}
