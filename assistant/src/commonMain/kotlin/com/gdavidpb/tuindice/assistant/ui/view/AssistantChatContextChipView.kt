package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatContextItem
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.a11y_assistant_remove_context

// The screen the sheet was opened from, as a chip the student can remove.
@Composable
fun AssistantChatContextChipView(
	item: ChatContextItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val shape = RoundedCornerShape(TuIndiceRadius.Full)

	Row(
		modifier = modifier
			.testTag(AssistantUiTags.ContextChip)
			.background(MaterialTheme.colorScheme.surfaceVariant, shape)
			.border(TuIndiceSpacing.Hairline, MaterialTheme.colorScheme.outlineVariant, shape)
			// The close button is 48 dp around a 24 dp icon whose cross is drawn 5 dp inside it, so the cross ends about
			// 17 dp from the chip's end; the label starts 16 dp in, as in a Material 3 assist chip, and both sides read
			// the same.
			.padding(start = TuIndiceSpacing.Section),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = item.label,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurface
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
