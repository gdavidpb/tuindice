package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.view.AssistantChatContent
import com.gdavidpb.tuindice.base.ui.dialog.TuIndiceModalBottomSheet
import com.gdavidpb.tuindice.base.ui.dialog.rememberTuIndiceSheetState
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The conversation takes the whole height the sheet has, with or without the keyboard, so the answers get all the room
// of the screen (it took 90% until 6.9.0, and 60% before that) and the sheet does not jump when the keyboard opens.
// It also opens at this height while the status is read, so it does not resize when the conversation arrives.
private const val CHAT_SHEET_HEIGHT_FRACTION = 1f

// The consent keeps 60%: its text and its buttons fill it, and a taller sheet would only add blank room below them.
private const val CONSENT_SHEET_HEIGHT_FRACTION = 0.6f

// The chat sheet: the conversation carries the title «Asistente» after the sparkle, without the name of the app; the
// consent carries its own, with the sparkle too. It opens showing its whole body, composer included. Its notices show
// inside it, through [snackbarHostState]: the host of the screen under the sheet is covered by the sheet.
@Composable
fun AssistantChatSheet(
	state: AssistantChat.State,
	onAction: (AssistantChat.Action) -> Unit,
	isCameraAvailable: Boolean = false,
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
	// The sheet opens fully, in one motion, with no half state to rest at: the body takes the whole window, so a half
	// open sheet would put the composer below the screen.
	val sheetState = rememberTuIndiceSheetState(skipPartiallyExpanded = true)

	TuIndiceModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(AssistantUiTags.ChatSheet),
		sheetState = sheetState,
		// The sheet pads nothing: the body pads the bottom itself, with the keyboard or the home indicator, so the composer
		// sits right above the keyboard, and the sheet never reaches the status bar, so a top inset would only add a gap
		// under the handle.
		contentWindowInsets = { WindowInsets(0) },
		onDismissRequest = { onAction(AssistantChat.Action.Dismiss) }
	) {
		// The sheet is as tall as the conversation while the status is read, so it keeps its height when the
		// conversation arrives. Only the consent is shorter.
		val heightFraction = when (state) {
			AssistantChat.State.Idle, is AssistantChat.State.Content -> CHAT_SHEET_HEIGHT_FRACTION
			else -> CONSENT_SHEET_HEIGHT_FRACTION
		}

		AssistantChatContent(
			state = state,
			isCameraAvailable = isCameraAvailable,
			onAction = onAction,
			snackbarHostState = snackbarHostState,
			modifier = Modifier
				.fillMaxWidth()
				.fillMaxHeight(heightFraction)
				.padding(
					start = TuIndiceSpacing.Dialog,
					end = TuIndiceSpacing.Dialog,
					bottom = TuIndiceSpacing.Medium
				)
		)
	}
}
