package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.ChatItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.style.chatEntrance
import com.gdavidpb.tuindice.base.ui.style.TuIndiceChatColors
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_user_failed

private const val BUBBLE_MAX_WIDTH_FRACTION = 0.8f

// The student's message on the right, at most 80 % of the available width. A failed turn adds its error line below.
// When [animateEntrance] is set (it was sent while the sheet was open) it fades in while rising.
@Composable
fun AssistantUserBubbleView(
	item: ChatItem.User,
	modifier: Modifier = Modifier,
	animateEntrance: Boolean = false
) {
	Box(modifier = modifier.fillMaxWidth()) {
		val shape = RoundedCornerShape(TuIndiceRadius.Large)

		Column(
			modifier = Modifier
				.align(Alignment.CenterEnd)
				.chatEntrance(animateEntrance)
				.bubbleMaxWidth(),
			horizontalAlignment = Alignment.End,
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
		) {
			Text(
				text = item.text,
				modifier = Modifier
					.testTag(AssistantUiTags.Message.bubble(item.id))
					.background(TuIndiceChatColors.chatUserBubble(), shape)
					.border(TuIndiceSpacing.Hairline, TuIndiceChatColors.chatUserBubbleOutline(), shape)
					.padding(horizontal = TuIndiceSpacing.XLarge, vertical = TuIndiceSpacing.Large),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface
			)
			if (item.isFailed) {
				Text(
					text = stringResource(Res.string.assistant_chat_user_failed),
					modifier = Modifier.testTag(AssistantUiTags.Message.bubbleError(item.id)),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.error
				)
			}
		}
	}
}

// The bubble takes at most its share of the width of the box it sits in, which is the width of the whole row.
private fun Modifier.bubbleMaxWidth(): Modifier = layout { measurable, constraints ->
	val maxWidth = minOf(constraints.maxWidth, (constraints.maxWidth.toDp() * BUBBLE_MAX_WIDTH_FRACTION).roundToPx())
	val placeable = measurable.measure(
		constraints.copy(minWidth = minOf(constraints.minWidth, maxWidth), maxWidth = maxWidth)
	)

	layout(placeable.width, placeable.height) {
		placeable.placeRelative(0, 0)
	}
}
