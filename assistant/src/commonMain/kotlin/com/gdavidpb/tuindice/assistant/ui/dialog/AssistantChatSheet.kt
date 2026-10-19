package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.AssistantChatContent
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import kotlinx.coroutines.flow.first

private const val CHAT_SHEET_HEIGHT_FRACTION = 0.6f

// The chat sheet: the conversation carries the title «Asistente» after the sparkle, without the name of the app; the
// consent carries its own, with the sparkle too. It opens showing its whole body, composer included. Its notices show
// inside it, through [snackbarHostState]: the host of the screen under the sheet is covered by the sheet.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantChatSheet(
	state: AssistantChat.State,
	onAction: (AssistantChat.Action) -> Unit,
	isCameraAvailable: Boolean = false,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
	// The body takes 60% of the window, more than the half a partly expanded sheet shows, so the first time the sheet
	// settles there it is lifted to show the composer. The half-open state itself stays: on iOS a sheet built without it
	// (skipPartiallyExpanded = true) ignores a tap on the scrim, measured on the simulator; afterwards the student can
	// drag the sheet as any other.
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

	LaunchedEffect(sheetState) {
		snapshotFlow { sheetState.currentValue }.first { value -> value == SheetValue.PartiallyExpanded }
		sheetState.expand()
	}

	ModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(AssistantUiTags.ChatSheet),
		sheetState = sheetState,
		onDismissRequest = { onAction(AssistantChat.Action.Dismiss) }
	) {
		AssistantChatContent(
			state = state,
			isCameraAvailable = isCameraAvailable,
			onAction = onAction,
			snackbarHostState = snackbarHostState,
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
