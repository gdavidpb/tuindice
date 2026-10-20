package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantMemoryCategoryKey
import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SettingsActionRow
import com.gdavidpb.tuindice.base.ui.view.TuIndiceSectionHeader
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_clear_all
import tuindice.assistant.generated.resources.assistant_memory_explanation
import tuindice.assistant.generated.resources.assistant_memory_group_career_goal
import tuindice.assistant.generated.resources.assistant_memory_group_dated_restriction
import tuindice.assistant.generated.resources.assistant_memory_group_interest
import tuindice.assistant.generated.resources.assistant_memory_group_other
import tuindice.assistant.generated.resources.assistant_memory_group_preference
import tuindice.assistant.generated.resources.assistant_memory_group_subject
import tuindice.assistant.generated.resources.assistant_memory_group_term

// What the assistant remembers, in groups by category in the order of the categories, and the destructive row to clear
// it all at the end, as the settings have their own.
@Composable
fun AssistantMemoryContentView(
	entries: List<MemoryEntryItem>,
	onAction: (AssistantMemory.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyColumn(
		modifier = modifier.fillMaxSize().testTag(AssistantUiTags.MemoryContent),
		contentPadding = PaddingValues(top = TuIndiceSpacing.Screen, bottom = TuIndiceSpacing.Dialog)
	) {
		item(key = AssistantUiTags.MemoryExplanation) {
			Text(
				text = stringResource(Res.string.assistant_memory_explanation),
				modifier = Modifier
					.testTag(AssistantUiTags.MemoryExplanation)
					.padding(horizontal = TuIndiceSpacing.Dialog),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}

		AssistantMemoryCategoryKey.entries.forEach { category ->
			val group = entries.filter { entry -> entry.category == category }

			if (group.isNotEmpty()) {
				item(key = "group-${category.name}") {
					TuIndiceSectionHeader(
						text = category.titleText(),
						modifier = Modifier.testTag(AssistantUiTags.MemorySectionTitle)
					)
				}

				items(items = group, key = MemoryEntryItem::id) { entry ->
					AssistantMemoryEntryItemView(entry = entry, onAction = onAction)
				}
			}
		}

		item(key = AssistantUiTags.MemoryClearAll) {
			Column(modifier = Modifier.padding(top = TuIndiceSpacing.Dialog)) {
				SettingsActionRow(
					icon = rememberVectorPainter(Icons.Outlined.DeleteOutline),
					text = stringResource(Res.string.assistant_memory_clear_all),
					testTag = AssistantUiTags.MemoryClearAll,
					isDestructive = true,
					onClick = { onAction(AssistantMemory.Action.ClearAll) }
				)
			}
		}
	}
}

// The name of a group on the screen, in the plural, because a group holds more than one memory.
@Composable
private fun AssistantMemoryCategoryKey.titleText(): String = stringResource(
	when (this) {
		AssistantMemoryCategoryKey.PREFERENCE -> Res.string.assistant_memory_group_preference
		AssistantMemoryCategoryKey.INTEREST -> Res.string.assistant_memory_group_interest
		AssistantMemoryCategoryKey.CAREER_GOAL -> Res.string.assistant_memory_group_career_goal
		AssistantMemoryCategoryKey.TERM -> Res.string.assistant_memory_group_term
		AssistantMemoryCategoryKey.SUBJECT -> Res.string.assistant_memory_group_subject
		AssistantMemoryCategoryKey.DATED_RESTRICTION -> Res.string.assistant_memory_group_dated_restriction
		AssistantMemoryCategoryKey.OTHER -> Res.string.assistant_memory_group_other
	}
)
