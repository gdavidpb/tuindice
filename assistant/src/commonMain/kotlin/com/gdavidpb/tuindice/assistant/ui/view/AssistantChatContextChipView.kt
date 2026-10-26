package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatContextItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.ui.style.TuIndiceComponentSizes
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_remove_context
import tuindice.assistant.generated.resources.assistant_chat_context_caption

// The screen the sheet was opened from, as a card the student can remove. A caption above it («Pregúntame sobre»)
// and the screen's icon inside it say what the card is: the screen the assistant answers about, not a filter. It has
// the container of the suggestions (AssistantChatSuggestionsView) and their full width, so it reads as part of the chat
// and not as a message of the student, whose bubbles are rounded and sit at the end.
@Composable
fun AssistantChatContextChipView(
	item: ChatContextItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Card)

	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Small)
	) {
		Text(
			text = stringResource(Res.string.assistant_chat_context_caption),
			style = MaterialTheme.typography.labelMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.padding(start = TuIndiceSpacing.XSmall)
		)
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(AssistantUiTags.ContextChip)
				.background(MaterialTheme.colorScheme.surfaceVariant, shape)
				.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
				// The icon and the label sit as the sparkle and the text of a suggestion: 12 dp in and 12 dp apart. The
				// close button keeps its 48 dp at the end, where a suggestion has its chevron.
				.padding(start = TuIndiceSpacing.XLarge),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = item.context.screen.icon(),
				contentDescription = null,
				modifier = Modifier.size(TuIndiceComponentSizes.IconMedium),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
			Text(
				text = item.label,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.weight(1f)
			)
			IconButton(
				onClick = { onAction(AssistantChat.Action.RemoveContext) },
				modifier = Modifier.testTag(AssistantUiTags.ContextRemove)
			) {
				Icon(
					imageVector = Icons.Outlined.Close,
					contentDescription = stringResource(Res.string.a11y_assistant_remove_context),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		}
	}
}

// The icons of the bottom bar for its four tabs, and the closest outlined icon for the other screens.
private fun AssistantScreen.icon(): ImageVector = when (this) {
	AssistantScreen.SUMMARY -> Icons.Outlined.BookmarkBorder
	AssistantScreen.STATISTICS -> Icons.Outlined.Insights
	AssistantScreen.RECORD -> Icons.Outlined.Book
	AssistantScreen.PENSUM -> Icons.Outlined.AccountTree
	AssistantScreen.EVALUATIONS, AssistantScreen.EVALUATION_DETAIL -> Icons.AutoMirrored.Outlined.Assignment
	AssistantScreen.SUBJECT_DETAIL -> Icons.Outlined.Class
	AssistantScreen.SUBJECT_SEARCH -> Icons.Outlined.Search
	AssistantScreen.ABOUT -> Icons.Outlined.Info
	AssistantScreen.TERM_REPORT -> Icons.Outlined.Description
}
