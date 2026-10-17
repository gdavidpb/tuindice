package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The replies the student can tap instead of typing.
@Composable
fun AssistantChatSuggestionsView(
	suggestions: List<ChatSuggestionItem>,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	LazyRow(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		items(items = suggestions, key = ChatSuggestionItem::id) { suggestion ->
			SuggestionChip(
				onClick = { onAction(AssistantChat.Action.PickSuggestion(suggestion.id)) },
				label = { Text(text = suggestion.label) },
				modifier = Modifier.testTag(AssistantUiTags.Chat.suggestion(suggestion.id)),
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant
				),
				border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant)
			)
		}
	}
}
