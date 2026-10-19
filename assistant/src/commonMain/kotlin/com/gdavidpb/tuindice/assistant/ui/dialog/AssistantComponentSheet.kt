package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.view.ChatComponentView
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem

// One component of an agent message, on a sheet of its own. It shows the component as the message shows it, and closes
// with a swipe down or a tap outside.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantComponentSheet(
	messageId: String,
	item: ChatComponentItem,
	onAction: (AssistantChat.Action) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ModalBottomSheet(
		modifier = Modifier.exposeTestTagsAsResourceId(),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		ChatComponentView(
			messageId = messageId,
			item = item,
			onAction = onAction,
			// The sheet already is the detail: its row would only open the same sheet again.
			showsDetailRow = false,
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					start = TuIndiceSpacing.Dialog,
					end = TuIndiceSpacing.Dialog,
					bottom = TuIndiceSpacing.Dialog
				)
		)
	}
}
