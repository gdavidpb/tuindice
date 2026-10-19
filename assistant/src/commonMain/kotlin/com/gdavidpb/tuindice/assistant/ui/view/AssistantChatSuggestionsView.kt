package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.assistant.ui.style.chatEntrance
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The replies the student can tap instead of typing. When [animateEntrance] is set they come in one after another,
// 50 ms apart, starting [leadMillis] after the row is composed (so after the answer they follow).
@Composable
fun AssistantChatSuggestionsView(
	suggestions: List<ChatSuggestionItem>,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	animateEntrance: Boolean = false,
	leadMillis: Int = 0
) {
	// The chips that already entered, so one that scrolls back into the row does not enter again. The set lives as long
	// as the row is composed, which is until the suggestions go away.
	val entered = remember { mutableSetOf<String>() }

	LazyRow(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		itemsIndexed(items = suggestions, key = { _, suggestion -> suggestion.id }) { index, suggestion ->
			val isFirstTime = remember(suggestion.id) { entered.add(suggestion.id) }
			val animate = animateEntrance && isFirstTime

			SuggestionChip(
				onClick = { onAction(AssistantChat.Action.PickSuggestion(suggestion.id)) },
				label = { Text(text = suggestion.label) },
				modifier = Modifier
					.chatEntrance(
						enabled = animate,
						delayMillis = leadMillis + index * AssistantChatMotion.SuggestionStaggerMillis,
						durationMillis = AssistantChatMotion.SmallEnterMillis,
						rise = AssistantChatMotion.SuggestionRise
					)
					.testTag(AssistantUiTags.Chat.suggestion(suggestion.id)),
				colors = SuggestionChipDefaults.suggestionChipColors(
					containerColor = MaterialTheme.colorScheme.surfaceVariant
				),
				border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant)
			)
		}
	}
}
