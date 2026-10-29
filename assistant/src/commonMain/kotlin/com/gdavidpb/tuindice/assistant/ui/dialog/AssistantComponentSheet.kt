package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.ui.view.ChatComponentView
import com.gdavidpb.tuindice.base.ui.dialog.TuIndiceModalBottomSheet
import com.gdavidpb.tuindice.base.ui.dialog.rememberTuIndiceSheetState
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// One component of an agent message, on a sheet of its own. It shows the component as the message shows it, and closes
// with a swipe down or a tap outside.
@Composable
fun AssistantComponentSheet(
	messageId: String,
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberTuIndiceSheetState()

	TuIndiceModalBottomSheet(
		modifier = Modifier.exposeTestTagsAsResourceId(),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		ChatComponentView(
			messageId = messageId,
			item = item,
			onAction = onAction,
			// The sheet already is the detail: it draws every row, and its link would only open the same sheet again.
			onSheet = true,
			// A long list scrolls within the sheet: the rows past the first ones are reached by scrolling.
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.padding(
					start = TuIndiceSpacing.Dialog,
					end = TuIndiceSpacing.Dialog,
					bottom = TuIndiceSpacing.Dialog
				)
		)
	}
}
