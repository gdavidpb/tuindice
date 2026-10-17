package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.AssistantChatContent
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

private const val CHAT_SHEET_HEIGHT_FRACTION = 0.6f

// The chat sheet: no title and no name of the assistant inside. It opens at medium height and expands on drag.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantChatSheet(
	state: AssistantChat.State,
	onAction: (AssistantChat.Action) -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

	ModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(AssistantUiTags.ChatSheet),
		sheetState = sheetState,
		onDismissRequest = { onAction(AssistantChat.Action.Dismiss) }
	) {
		AssistantChatContent(
			state = state,
			onAction = onAction,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(CHAT_SHEET_HEIGHT_FRACTION)
				.padding(
					start = TuIndiceSpacing.Dialog,
					end = TuIndiceSpacing.Dialog,
					bottom = TuIndiceSpacing.Medium
				)
		)
	}
}
