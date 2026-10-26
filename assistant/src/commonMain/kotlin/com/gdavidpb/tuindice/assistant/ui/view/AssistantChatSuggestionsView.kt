package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatSuggestionItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.assistant.ui.style.chatEntrance
import com.gdavidpb.tuindice.base.ui.icon.assistantSparkleTint
import com.gdavidpb.tuindice.base.ui.icon.rememberAssistantSparkleIcon
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_suggestions_intro

// The replies the student can tap instead of typing, one under the other and whole, so each reads complete (a row
// showed one and a half of the three of a screen). [showsIntro] puts «Puedes preguntarme, por ejemplo:» above them,
// for the chat that has no messages yet. With [entranceLeadMillis] they come in one after another, 50 ms apart,
// starting that long after the list is composed (so after the answer they follow); without it they simply are.
@Composable
fun AssistantChatSuggestionsView(
	suggestions: List<ChatSuggestionItem>,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	showsIntro: Boolean = false,
	entranceLeadMillis: Int? = null
) {
	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		if (showsIntro) {
			Text(
				text = stringResource(Res.string.assistant_chat_suggestions_intro),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.padding(start = TuIndiceSpacing.XSmall)
			)
		}
		suggestions.forEachIndexed { index, suggestion ->
			// Keyed by id, so a new set of replies enters as new, and one kept from the last set stays as it was.
			key(suggestion.id) {
				AssistantChatSuggestionView(
					suggestion = suggestion,
					onAction = onAction,
					modifier = Modifier.chatEntrance(
						enabled = entranceLeadMillis != null,
						delayMillis = (entranceLeadMillis ?: 0) + index * AssistantChatMotion.SuggestionStaggerMillis,
						durationMillis = AssistantChatMotion.SmallEnterMillis,
						rise = AssistantChatMotion.SuggestionRise
					)
				)
			}
		}
	}
}

// One reply: the sparkle in the accent, the text in up to two lines and a chevron, at least 48 dp tall.
@Composable
private fun AssistantChatSuggestionView(
	suggestion: ChatSuggestionItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val sparkle = rememberAssistantSparkleIcon()
	val shape = remember { RoundedCornerShape(TuIndiceRadius.Card) }

	Surface(
		onClick = { onAction(AssistantChat.Action.PickSuggestion(suggestion.id)) },
		modifier = modifier
			.fillMaxWidth()
			.testTag(AssistantUiTags.Chat.suggestion(suggestion.id)),
		shape = shape,
		color = MaterialTheme.colorScheme.surfaceVariant,
		border = BorderStroke(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant)
	) {
		Row(
			modifier = Modifier
				.defaultMinSize(minHeight = TuIndiceComponentSizes.IconButton)
				.padding(horizontal = TuIndiceSpacing.XLarge, vertical = TuIndiceSpacing.Large),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = sparkle,
				contentDescription = null,
				modifier = Modifier.size(TuIndiceComponentSizes.IconSmall),
				tint = assistantSparkleTint()
			)
			Text(
				text = suggestion.label,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				maxLines = 2,
				modifier = Modifier.weight(1f)
			)
			Icon(
				imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
				contentDescription = null,
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
